package cz.tomkren.fishtron.ugen.trees;

import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.TMap;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Fresh;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.Params;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import com.google.common.base.Joiner;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/** Created by user on 27. 7. 2016.*/

// TODO opakujou se tu kody, předelat na abstract dědičnost asi...

public interface AppTree {

    static AppTree mk(String sym, Type type) {
        return new AppTree.Leaf(sym, type);
    }

    static AppTree mk(AppTree funTree, AppTree argTree, Type type) {
        return new AppTree.App(funTree, argTree, type);
    }

    static void writeErrorTreeToFile(JSONObject typeTrace) {
        F.writeJsonAsJsFile("www/data/lastErrTree.js", "mkLastErrTree", typeTrace);
    }

    AppTree randomizeParams(JSONObject allParamsInfo, Random rand);
    boolean hasParams();

    Type getType();
    Type getOriginalType();
    int size();
    void deskolemize(Set<Integer> ids);
    void applySub(Sub sub);
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

        if (selectedSubtree instanceof AppTree.Leaf) {
            AppTree.Leaf selectedLeaf = (AppTree.Leaf) selectedSubtree;
            AppTree.Leaf newLeaf = selectedLeaf.randomlyShiftOneParam(rand, shiftsWithProbabilities);
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


    class Leaf implements AppTree {
        private String sym;
        private Type type;
        private Type originalType;
        private JSONObject debugInfo;
        private Params params;

        private Leaf(String sym, Type type) {
            this(sym, type, type, null, null);
        }

        private Leaf(String sym, Type type, Type originalType, JSONObject debugInfo, Params params) {
            this.sym = sym;
            this.type = type;
            this.originalType = originalType;
            this.debugInfo = debugInfo;
            this.params = params;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Leaf leaf = (Leaf) o;
            return sym.equals(leaf.sym) && (params != null ? params.equals(leaf.params) : leaf.params == null);
        }

        @Override
        public int hashCode() {
            int result = sym.hashCode();
            result = 31 * result + (params != null ? params.hashCode() : 0);
            return result;
        }

        @Override
        public boolean hasParams() {
            return params != null;
        }

        public Params getParams() {
            return params;
        }

        @Override
        public AppTree randomizeParams(JSONObject allParamsInfo, Random rand) {
            JSONObject paramsInfo = allParamsInfo.has(sym) ? allParamsInfo.getJSONObject(sym) : null;
            if (paramsInfo == null) {
                return this;
            } else {
                return new Leaf(sym, type, originalType, debugInfo, new Params(paramsInfo, rand));
            }
        }

        Leaf randomlyShiftOneParam(Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {
            Params newParams = params.randomlyShiftOneParam(rand, shiftsWithProbabilities);
            return new Leaf(sym, type, originalType, debugInfo, newParams);
        }

        @Override
        public AppTree getSubtree(SubtreePos pos) {
            return pos.isRoot() ? this : null;
        }

        @Override
        public AppTree changeSubtree(SubtreePos pos, AppTree newSubtree) {
            return pos.isRoot() ? newSubtree : null;
        }


        @Override
        public List<SubtreePos> getAllSubtreePosesWhere(Predicate<AppTree> isTrue) {
            if (isTrue.test(this)) {
                return Collections.singletonList(SubtreePos.root(type));
            } else {
                return Collections.emptyList();
            }
        }

        @Override public Type getOriginalType() {return originalType;}

        @Override public Type getType() {return type;}
        @Override public int size() {return 1;}

        @Override public void deskolemize(Set<Integer> ids) {type = type.deskolemize(ids);}
        @Override public void applySub(Sub sub) {type = sub.apply(type);}
        @Override public void applyTypeTransform(Function<Type, Type> tt) {type = tt.apply(type);}

        @Override public String toString() {
            if (params == null) {
                return sym;
            } else {
                return sym + params.toJson().toString();
            }
        }

        @Override
        public String toRawString() {
            return sym;
        }

        public String toShortString() {
            return sym;
        }

        public String getSym() {
            return sym;
        }

        @Override
        public String toStringWithTypes() {
            return "<"+sym+":"+type+">";
        }

        @Override public AB<Boolean,Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId) {
            Type t_s = gammaMap.get(sym);

            if (t_s == null) {
                return AB.mk(false, null);
            }

            Fresh freshRes = new Fresh(t_s,type,nextVarId);
            Type t_s_fresh     = freshRes.getFreshType();
            int  t_s_nextVarId = freshRes.getNextVarId();

            Sub mgu = Sub.mgu(type, t_s_fresh); // todo: Není úplně podle definice, ale pokud ex mgu, tak určitě i substituce ex (tzn silnější). Do-potvrdit si, že je to ok.

            return AB.mk(!mgu.isFail(), t_s_nextVarId);
        }

        @Override public JSONObject getTypeTrace() {
            JSONObject typeTrace = F.obj("node",sym, "type",type.toJson());
            if (debugInfo != null) {typeTrace.put("debugInfo", debugInfo);}
            return typeTrace;
        }

        @Override
        public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
            debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
        }
    }

    class App implements AppTree {

        private AppTree funTree;
        private AppTree argTree;
        private Type type;
        private Type originalType;
        private JSONObject debugInfo;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            App app = (App) o;
            return funTree.equals(app.funTree) && argTree.equals(app.argTree);
        }

        @Override
        public int hashCode() {
            int result = funTree.hashCode();
            result = 31 * result + argTree.hashCode();
            return result;
        }

        private App(AppTree funTree, AppTree argTree, Type type) {
            this(funTree, argTree, type, type, null);
        }

        private App(AppTree funTree, AppTree argTree, Type type, Type originalType, JSONObject debugInfo) {
            this.funTree = funTree;
            this.argTree = argTree;
            this.type = type;
            this.originalType = originalType;
            this.debugInfo = debugInfo;
        }

        @Override
        public boolean hasParams() {
            return false;
        }

        @Override
        public AppTree randomizeParams(JSONObject allParamsInfo, Random rand) {
            AppTree newFunTree = funTree.randomizeParams(allParamsInfo, rand);
            AppTree newArgTree = argTree.randomizeParams(allParamsInfo, rand);
            return new App(newFunTree, newArgTree, type, originalType, debugInfo);
        }

        @Override
        public AppTree getSubtree(SubtreePos pos) {
            if (pos.isRoot()) {
                return this;
            } else {
                AppTree subtree = (pos.getSonIndex() == 0 ? funTree : argTree);
                return subtree.getSubtree(pos.getTail());
            }
        }

        @Override
        public AppTree changeSubtree(SubtreePos pos, AppTree newSubtree) {
            if (pos.isRoot()) {
                return newSubtree;
            } else {
                SubtreePos tailPos = pos.getTail();
                if (pos.getSonIndex() == 0) {
                    AppTree newFunTree = funTree.changeSubtree(tailPos, newSubtree);
                    return new AppTree.App(newFunTree, argTree, type);
                } else {
                    AppTree newArgTree = argTree.changeSubtree(tailPos, newSubtree);
                    return new AppTree.App(funTree, newArgTree, type);
                }
            }
        }

        @Override
        public List<SubtreePos> getAllSubtreePosesWhere(Predicate<AppTree> isTrue) {
            List<SubtreePos> ret = new ArrayList<>();

            if (isTrue.test(this)) {
                ret.add(SubtreePos.root(type));
            }

            List<SubtreePos> funPoses = funTree.getAllSubtreePosesWhere(isTrue);
            ret.addAll(F.map(funPoses, pos -> SubtreePos.reverseStep(0,pos)));

            List<SubtreePos> argPoses = argTree.getAllSubtreePosesWhere(isTrue);
            ret.addAll(F.map(argPoses, pos -> SubtreePos.reverseStep(1,pos)));

            return ret;
        }

        public AppTree getFunTree() {return funTree;}
        public AppTree getArgTree() {return argTree;}

        @Override public Type getOriginalType() {return originalType;}


        @Override public Type getType() {return type;}

        @Override
        public int size() {
            return funTree.size() + argTree.size();
        }

        @Override
        public AB<Boolean,Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId) {

            if (isRootStrictlyWellTyped()) {
                AB<Boolean,Integer> funTreeRes = funTree.isStrictlyWellTyped(gammaMap, nextVarId);
                if (funTreeRes._1()) {
                    return argTree.isStrictlyWellTyped(gammaMap, funTreeRes._2());
                }
            }

            return AB.mk(false, null);

            /*boolean isRootSWT = isRootStrictlyWellTyped();
            AB<Boolean,Integer> funTreeRes = funTree.isStrictlyWellTyped(gammaMap, nextVarId);
            AB<Boolean,Integer> argTreeRes = argTree.isStrictlyWellTyped(gammaMap, funTreeRes._2());

            boolean isSWT = isRootSWT && funTreeRes._1() && argTreeRes._1();
            return AB.mk(isSWT, argTreeRes._2());*/
        }

        private boolean isRootStrictlyWellTyped() {
            Type funType = funTree.getType();
            Type argType = argTree.getType();
            AA<Type> fun = Types.splitFunType(funType);
            return Types.isSameType(fun._1(), argType) && Types.isSameType(fun._2(), type);
        }

        @Override
        public JSONObject getTypeTrace() {
            JSONObject typeTrace = F.obj(
                    "node","@",
                    "type",type.toJson(),
                    "fun",funTree.getTypeTrace(),
                    "arg",argTree.getTypeTrace()
            );

            if (!isRootStrictlyWellTyped()) {
                typeTrace.put("error",true);
            }

            if (debugInfo != null) {
                typeTrace.put("debugInfo", debugInfo);
            }

            return typeTrace;
        }

        /*private boolean isSameType(Type t1, Type t2) {
            return t1.toString().equals(t2.toString());
        }*/

        @Override
        public String toRawString() {
            return "("+funTree.toRawString()+" "+argTree.toRawString()+")";
        }

        @Override
        public String toString() {
            AB<AppTree.Leaf,List<AppTree>> p = getFunLeafWithArgs();
            return "("+p._1()+" "+ Joiner.on(' ').join(p._2())+")";
        }

        public String toShortString() {
            AB<AppTree.Leaf,List<AppTree>> p = getFunLeafWithArgs();
            String head = p._1().toShortString();
            List<String> tail = F.map(p._2(), AppTree::toShortString);
            return "("+head+" "+ Joiner.on(' ').join(tail)+")";
        }

        private AB<Leaf,List<AppTree>> getFunLeafWithArgs() {
            List<AppTree> argTrees = new ArrayList<>();

            AppTree acc = this;
            while (acc instanceof App) {
                App app = (App) acc;
                argTrees.add(app.argTree);
                acc = app.funTree;
            }

            Collections.reverse(argTrees);
            return new AB<>((Leaf)acc, argTrees);
        }

        @Override
        public String toStringWithTypes() {
            return "(<"+type+"> "+
                    funTree.toStringWithTypes()+" "+
                    argTree.toStringWithTypes()+")";
        }

        @Override
        public void deskolemize(Set<Integer> ids) {
            type = type.deskolemize(ids);
            funTree.deskolemize(ids);
            argTree.deskolemize(ids);
        }

        @Override
        public void applySub(Sub sub) {
            type = sub.apply(type);
            funTree.applySub(sub);
            argTree.applySub(sub);
        }

        @Override
        public void applyTypeTransform(Function<Type,Type> tt) {
            type = tt.apply(type);
            funTree.applyTypeTransform(tt);
            argTree.applyTypeTransform(tt);
        }

        @Override
        public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
            debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
        }
    }

}
