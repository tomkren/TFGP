package cz.tomkren.fishtron.terms;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.fishtron.reusegen.QuerySolver;
import net.fishtron.types.Sub;
import net.fishtron.types.TMap;
import net.fishtron.types.Type;
import cz.tomkren.fishtron.workflows.DataScientistLibs;
import cz.tomkren.utils.*;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/** Created by tom on 7.11.2015. */

public class PolyTree implements FitIndiv, Comparable<PolyTree> {

    private final SmartSymbol sym;
    private Type type;
    private final List<PolyTree> sons;

    private FitVal fitVal; // TODO | zbytečně i v podstromech, chtělo by udělat nějakej individual wrapper, kterej by měl PolyTree jako položku !!!!!!!!!!!
    // TODO | Ideálně asi jako Individual<Genotype, Phenotype> ...


    public PolyTree(SmartSymbol sym, Type type, List<PolyTree> sons) {
        this.sym  = sym;
        this.type = type;
        this.sons = sons;
        this.fitVal = null;
    }

    public int getSize() {
        int sum = 0;
        for (PolyTree son : sons) {sum += son.getSize();}
        return 1 + sum;
    }

    public boolean isTerminal() {return sons.isEmpty();}
    public boolean isFunction() {return !sons.isEmpty();}

    public int depth() {return isTerminal() ? 0 : 1 + F.list(getSons()).map(PolyTree::depth).max();}



    public String getName() {return sym.getName();}
    public String getNameWithParams() {return sym.getNameWithParams();}

    public Type getType() {return type;}
    public List<PolyTree> getSons() {return sons;}
    public Comb0 getCode() {return sym.getCode();}
    public SmartSymbol getSymbol() {return sym;}


    @Override public FitVal getFitVal() {
        return fitVal;
    }
    @Override public void setFitVal(FitVal fitVal) {
        this.fitVal = fitVal;
    }

    @Override
    public double getWeight() {
        if (fitVal == null) {throw new Error("fitVal must be not-null!");}
        return fitVal.getVal();
    }

    @Override
    public Object computeValue() {
        if (getCode() == null) {throw new Error("Null-code in computeValue().");}

        if (isTerminal()) {
            return getCode().compute(F.singleton(type));
        } else {
            return getCode().compute(F.map(sons, PolyTree::computeValue));
        }
    }



    public PolyTree randomizeAllParams(Random rand) {
        SmartSymbol rootSym = sym instanceof SmartSymbolWithParams ? ((SmartSymbolWithParams)sym).randomizeAllParams(rand) : sym;
        return new PolyTree(rootSym, type, F.map(sons, s->s.randomizeAllParams(rand)));
    }

    public PolyTree randomlyShiftOneParam(Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {
        if (sym instanceof SmartSymbolWithParams) {
            SmartSymbol newCodeNode = ((SmartSymbolWithParams) sym).randomlyShiftOneParam(rand, shiftsWithProbabilities);
            return new PolyTree(newCodeNode, type, sons);
        } else {
            throw new Error("Method randomizeOneParam can be applied only to tree with CodeNodeWithParams as codeNode.");
        }
    }

    public List<SubtreePos> getAllSubtreePosesWhere(Predicate<PolyTree> isTrue) {
        List<SubtreePos> ret = new ArrayList<>();

        if (isTrue.test(this)) {
            ret.add(SubtreePos.root(type));
        }

        int sonIndex = 0;
        for (PolyTree son : sons) {
            List<SubtreePos> sonSubtreePoses = son.getAllSubtreePosesWhere(isTrue);
            for (SubtreePos subtreePosInSon : sonSubtreePoses) {
                ret.add(SubtreePos.reverseStep(sonIndex, subtreePosInSon));
            }
            sonIndex++;
        }

        return ret;
    }

    private List<SubtreePos> getAllSubtreePosesWhere_isRootIncluded(Predicate<PolyTree> isTrue, boolean isRootIncluded) {
        List<SubtreePos> ret = new ArrayList<>();

        if (isRootIncluded && isTrue.test(this)) {
            ret.add(SubtreePos.root(type));
        }

        int sonIndex = 0;
        for (PolyTree son : sons) {
            List<SubtreePos> sonSubtreePoses = son.getAllSubtreePosesWhere_isRootIncluded(isTrue, true);
            for (SubtreePos subtreePosInSon : sonSubtreePoses) {
                ret.add(SubtreePos.reverseStep(sonIndex, subtreePosInSon));
            }
            sonIndex++;
        }

        return ret;
    }

    public List<SubtreePos> getAllSubtreePoses() {
        return getAllSubtreePosesWhere(t->true);
    }

    public TMap<SubtreePos> getAllSubtreePoses_byTypes() {
        return new TMap<>(getAllSubtreePoses(), SubtreePos::getType);
    }

    // TODO asi by bylo slušný udělat efektivnějc
    public SubtreePos getRandomSubtreePos(Random rand) {
        return F.randomElement(getAllSubtreePoses(), rand);
    }


    public SubtreePos kozaSelectPath(double pSelectInner, Random rand) {

        if (isTerminal()) {
            return SubtreePos.root(type);
        }

        Predicate<PolyTree> subtreeSelector = pSelectInner < rand.nextDouble() ? PolyTree::isFunction : PolyTree::isTerminal;

        List<SubtreePos> paths = getAllSubtreePosesWhere_isRootIncluded(subtreeSelector ,false /*root zařadíme pouze pokud je celý strom terminál, což dělá první if této funkce*/ );

        if (paths.isEmpty()) {
            // K tomuto by mělo dojít pouze pokud byl vybrán selektor isFunction na strom co má jedinou funkci v kořeni a zbytek jsou terminály
            paths = getAllSubtreePosesWhere_isRootIncluded(PolyTree::isTerminal , false);
        }

        if (paths.isEmpty()) {
            // should be unreachable
            throw new Error("kozaSelectPath ERROR: should be unreachable: There should always be at least one subtree!");
        }

        return F.randomElement(paths, rand);
    }



    public PolyTree getSubtree(SubtreePos pos) {
        if (pos.isRoot()) {
            return this;
        } else {
            return sons.get(pos.getSonIndex()).getSubtree(pos.getTail());
        }
    }

    public PolyTree changeSubtree(SubtreePos pos, PolyTree newSubtree) {
        if (pos.isRoot()) {
            return newSubtree;
        } else {
            List<PolyTree> newSons = new ArrayList<>(sons.size());
            int sonIndex = pos.getSonIndex();
            int i = 0;
            for (PolyTree son : sons) {
                newSons.add( i == sonIndex ? son.changeSubtree(pos.getTail(),newSubtree) : son );
                i++;
            }
            return  new PolyTree(sym,type,newSons);
        }
    }

    public static AA<PolyTree> xover(PolyTree mum, PolyTree dad, SubtreePos mumPos, SubtreePos dadPos) {
        PolyTree daughter = mum.changeSubtree(mumPos, dad.getSubtree(dadPos));
        PolyTree son      = dad.changeSubtree(dadPos, mum.getSubtree(mumPos));
        return new AA<>(daughter,son);
    }

    // TODO otázka zda to stojí za porušení immutability, ale slouží to k do-upřesnění typů při reusable generování
    public void applySub(Sub sub) {
        type = sub.apply(type);
        sons.forEach(s->s.applySub(sub));
    }

    @Override
    public String toString() {
        return isTerminal() ? getNameWithParams() : "("+ getNameWithParams() +" "+ Joiner.on(' ').join( sons ) +")";
    }

    public String toStringWithoutParams() {
        return isTerminal() ? getName() : "("+ getName() +" "+ Joiner.on(' ').join( F.map(sons, PolyTree::toStringWithoutParams) ) +")";
    }

    private String showHead() {
        return "<"+getName()+":"+type+">";
    }

    public String showWithTypes() {
        return isTerminal() ? showHead() : "("+ showHead() +" "+ Joiner.on(' ').join( F.map(sons, PolyTree::showWithTypes) ) +")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {return toString().hashCode();}


    @Override
    public int compareTo(PolyTree o) {return compareTrees.compare(this, o);}

    public static final Comparator<String> compareStrs = (s1, s2) -> {
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == len2) {return s1.compareTo(s2);}
        return Integer.compare(len1, len2);
    };

    public static final Comparator<PolyTree> compareTrees = (t1, t2) -> {
        int size1 = t1.getSize();
        int size2 = t2.getSize();
        if (size1 == size2) {return compareStrs.compare(t1.toString(), t2.toString());}
        return Integer.compare(size1, size2);
    };




    public static void main(String[] args) {
        Checker ch = new Checker();

        SmartLibrary lib = DataScientistLibs.DATA_SCIENTIST_01;

        List<PolyTree> trees = new QuerySolver(lib, ch.getRandom()).simpleUniformGenerate("D => LD", 20, 1000);

        for (PolyTree tree : trees) {
            List<SubtreePos> allPoses = tree.getAllSubtreePoses();
            ch.eq(tree.getSize(), allPoses.size());
            SubtreePos randomPos = F.randomElement(allPoses,ch.getRandom());
            Log.it(randomPos);
            ch.eqStr(tree.toString(), tree.changeSubtree(randomPos, tree.getSubtree(randomPos)));
        }


        ch.results();
    }



}
