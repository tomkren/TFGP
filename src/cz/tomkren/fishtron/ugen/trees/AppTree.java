package cz.tomkren.fishtron.ugen.trees;

import cz.tomkren.fishtron.terms.SubtreePos;
import net.fishtron.types.Sub;
import net.fishtron.types.TMap;
import net.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/** Created by user on 27. 7. 2016.*/

// TODO opakujou se tu kody, předelat na abstract dědičnost asi...

public interface AppTree {

    static AppTree mk(String sym, Type type) {
        return new Leaf(sym, type);
    }

    static AppTree mk(AppTree funTree, AppTree argTree, Type type) {
        return new App(funTree, argTree, type);
    }

    static void writeErrorTreeToFile(JSONObject typeTrace) {
        F.writeJsonAsJsFile("www/data/lastErrTree.js", "mkLastErrTree", typeTrace);
    }

    Sexpr toSexpr();

    AppTree randomizeParams(JSONObject allParamsInfo, Random rand);
    boolean hasParams();

    Type getType();
    Type getOriginalType();
    int size();
    void deskolemize(Set<Integer> ids);
    void applySub(Sub sub);

    AppTree applySub_new(Sub sub);


    void applyTypeTransform(Function<Type,Type> tt);
    String toRawString();
    String toShortString();
    String toStringWithTypes();

    AB<Boolean,Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId);
    JSONObject getTypeTrace();

    List<SubtreePos> getAllSubtreePosesWhere(Predicate<AppTree> isTrue);

    AppTree getSubtree(SubtreePos pos);
    AppTree changeSubtree(SubtreePos pos, AppTree newSubtree);

    static AppTree mutate_sss(AppTree tree, Gen gen, int maxSubtreeSize, JSONObject allParamsInfo, Random rand) {
        // select subtree
        SubtreePos subtreePos;
        AppTree subTree;
        do {
            subtreePos = tree.getRandomSubtreePos(rand);
            subTree = tree.getSubtree(subtreePos);
        } while (subTree.size() > maxSubtreeSize);

        // generate new subtree with same size and type
        Type goalType = subTree.getType();
        int treeSize = subTree.size();
        AppTree newSubtree = gen.genOne(treeSize, goalType);
        AppTree newSubtreeWithParams = newSubtree.randomizeParams(allParamsInfo, rand);

        // create new tree with that subtree
        return tree.changeSubtree(subtreePos, newSubtreeWithParams);
    }

    static AA<AppTree> xover(AppTree mum, AppTree dad, SubtreePos mumPos, SubtreePos dadPos) {
        AppTree child1 = mum.changeSubtree(mumPos, dad.getSubtree(dadPos));
        AppTree child2 = dad.changeSubtree(dadPos, mum.getSubtree(mumPos));
        return new AA<>(child1,child2);
    }

    static AppTree mutate_param(AppTree tree, List<AB<Integer,Double>> shiftsWithProbabilities, Random rand) {
        List<SubtreePos> posesWithParams = tree.getAllSubtreePosesWhere(AppTree::hasParams);
        if (posesWithParams.isEmpty()) {return tree;}

        SubtreePos subtreePos = F.randomElement(posesWithParams, rand);
        AppTree selectedSubtree = tree.getSubtree(subtreePos);

        if (selectedSubtree instanceof Leaf) {
            Leaf selectedLeaf = (Leaf) selectedSubtree;
            Leaf newLeaf = selectedLeaf.randomlyShiftOneParam(rand, shiftsWithProbabilities);
            return tree.changeSubtree(subtreePos, newLeaf);
        } else {
            throw new Error("Selected subtree must be leaf, should be unreachable.");
        }
    }

    static AA<AppTree> xover(AppTree mum, AppTree dad, int maxTreeSize, Random rand) {

        TMap<SubtreePos> mumPoses = mum.getAllSubtreePoses_byTypes();
        TMap<SubtreePos> dadPoses = dad.getAllSubtreePoses_byTypes();

        Map<Type,AA<List<SubtreePos>>> intersection = TMap.intersection(mumPoses, dadPoses);

        int numPossiblePairs = getNumPossibleXoverPairs(intersection);
        int ball = rand.nextInt(numPossiblePairs);
        AA<SubtreePos> selectedPoses = selectXoverPoses(ball, intersection, rand);
        SubtreePos mumPos = selectedPoses._1();
        SubtreePos dadPos = selectedPoses._2();

        AA<AppTree> children = AppTree.xover(mum, dad, mumPos, dadPos);

        return new AA<>(
                children._1().size() <= maxTreeSize ? children._1() : mum ,
                children._2().size() <= maxTreeSize ? children._2() : dad
        );
    }

    static int getNumPossibleXoverPairs(Map<Type, AA<List<SubtreePos>>> intersection) {
        int sum = 0;
        for (Map.Entry<Type,AA<List<SubtreePos>>> e : intersection.entrySet()) {
            List<SubtreePos> mumList = e.getValue()._1();
            List<SubtreePos> dadList = e.getValue()._2();
            sum += mumList.size() * dadList.size();
        }
        return sum;
    }

    static AA<SubtreePos> selectXoverPoses(int ball, Map<Type,AA<List<SubtreePos>>> intersection, Random rand) {
        int sum = 0;
        for (Map.Entry<Type,AA<List<SubtreePos>>> e : intersection.entrySet()) {
            AA<List<SubtreePos>> pair = e.getValue();
            List<SubtreePos> mumList = pair._1();
            List<SubtreePos> dadList = pair._2();

            sum += mumList.size() * dadList.size();
            if (sum > ball) {
                SubtreePos mumPos = F.randomElement(mumList,rand);
                SubtreePos dadPos = F.randomElement(dadList,rand);
                return new AA<>(mumPos, dadPos);
            }
        }
        throw new Error("Unreachable!");
    }

    default SubtreePos getRandomSubtreePos(Random rand) {
        return F.randomElement(getAllSubtreePoses(), rand);
    }

    default List<SubtreePos> getAllSubtreePoses() {
        return getAllSubtreePosesWhere(t->true);
    }

    default TMap<SubtreePos> getAllSubtreePoses_byTypes() {
        return new TMap<>(getAllSubtreePoses(), SubtreePos::getType);
    }

    default boolean isStrictlyWellTyped(Gamma gamma) {
        Map<String,Type> gammaMap = new HashMap<>();
        for(AB<String,Type> p : gamma.getSymbols()) {
            gammaMap.put(p._1(),p._2());
        }
        return isStrictlyWellTyped(gammaMap, 0)._1();
    }

    void updateDebugInfo(Function<JSONObject,JSONObject> updateFun);

    Comparator<String> compareStrs = (s1, s2) -> {
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == len2) {return s1.compareTo(s2);}
        return Integer.compare(len1, len2);
    };

    Comparator<AppTree> compareTrees = (t1, t2) -> {
        int size1 = t1.size();
        int size2 = t2.size();
        if (size1 == size2) {return compareStrs.compare(t1.toString(), t2.toString());}
        return Integer.compare(size1, size2);
    };


}
