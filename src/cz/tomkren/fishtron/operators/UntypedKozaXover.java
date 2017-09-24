package cz.tomkren.fishtron.operators;

/** Created by tom on 24.1.2016.*/

import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.terms.PolyTree;
import net.fishtron.utils.AA;
import net.fishtron.utils.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;


// TODO !!! Narychlo předěláno z package cz.tomkren.oldfish.cec.KozaXover a obsahuje static třídu TreePath , kterou využívala netypováná veze,
// TODO |   a to proto, že TreePos kterou požívá PolyTree je typovaná, a tak se mi to nechtělo komplikovat, ale je to taKLE NECHUTNÝ


// Zmeny oproti cz.tomkren.oldfish.cec.KozaXover :
// pravá Koza Xover má pravděpodobnost výběru rootu 0, v původním jí nezohledňujeme
// potřeba hlídat možnost, že není žádný vnitřní vrchol, když ho cheme vzít. Pak je třeba vzít terminál, který je vždy.

public class UntypedKozaXover implements Operator<PolyTree> {

    private double operatorProbability;
    private final Random rand;

    private final double pSelectInner;
    private final int maxDepth;

    public UntypedKozaXover(double operatorProbability, double pSelectInner, int maxDepth, Random rand) {
        this.operatorProbability = operatorProbability;
        this.pSelectInner = pSelectInner;
        this.maxDepth = maxDepth;
        this.rand = rand;
    }

    public UntypedKozaXover(double operatorProbability, Random rand) {
        this(operatorProbability, 0.9, 17, rand);
    }

    @Override
    public List<PolyTree> operate(List<PolyTree> parents) {
        AA<PolyTree> children = xover(parents.get(0),parents.get(1));
        return Arrays.asList(children._1(),children._2());
    }

    @Override public int getNumInputs() {return 2;}
    @Override public double getWeight() {return operatorProbability;}


    public AA<PolyTree> xover(PolyTree mum, PolyTree dad) {

        TreePath mumPath = kozaSelectPath(mum);
        TreePath dadPath = kozaSelectPath(dad);

        PolyTree kid1 = TreePath.changeSubtree(mum, mumPath, TreePath.getSubtree(dad, dadPath));
        PolyTree kid2 = TreePath.changeSubtree(dad, dadPath, TreePath.getSubtree(mum, mumPath));

        if (kid1.depth() > maxDepth) { kid1 = mum; }
        if (kid2.depth() > maxDepth) { kid2 = dad; }

        return new AA<>(kid1, kid2);
    }



    private TreePath kozaSelectPath(PolyTree parent) {

        if (parent.isTerminal()) {
            return null;
        }

        Function<PolyTree,Boolean> subtreeSelector = pSelectInner < rand.nextDouble() ? PolyTree::isFunction : PolyTree::isTerminal;

        List<TreePath> paths = TreePath.mkXoverPaths(parent, subtreeSelector, false /*root zařadíme pouze pokud je celý strom terminál, což dělá první if této funkce*/ );

        if (paths.isEmpty()) {
            // K tomuto by mělo dojít pouze pokud byl vybrán selektor isFunction na strom co má jedinou funkci v kořeni a zbytek jsou terminály
            paths = TreePath.mkXoverPaths(parent, PolyTree::isTerminal, false);
        }

        if (paths.isEmpty()) {
            // should be unreachable
            throw new Error("kozaSelectPath ERROR: should be unreachable: There should always be at least one subtree!");
        }

        return F.randomElement(paths, rand);
    }


    public static class TreePath {

        private int sonIndex;
        private TreePath tail;

        public TreePath(int sonIndex, TreePath tail) {
            this.sonIndex = sonIndex;
            this.tail = tail;
        }

        public int getSonIndex() {return sonIndex;}
        public TreePath getTail() {return tail;}

        @Override
        public String toString() {
            return "[ "+ toString_() +" ]";
        }

        private String toString_() {
            return sonIndex + (tail == null ? "" : " "+tail.toString_());
        }

        public static List<TreePath> mkXoverPaths(PolyTree tree, Function<PolyTree,Boolean> predicate, boolean isRootIncluded) {
            List<TreePath> paths = new ArrayList<>();
            if (isRootIncluded && predicate.apply(tree)) {
                paths.add(null);
            }
            int i = 0;
            for (PolyTree son : tree.getSons()) {
                for (TreePath subPath : mkXoverPaths(son, predicate,true /*podstromy už zařazujeme všechny*/ )) {
                    TreePath newPath = new TreePath(i,subPath);
                    paths.add(newPath);
                }
                i++;
            }
            return paths;
        }

        public static PolyTree changeSubtree(PolyTree tree, TreePath path, PolyTree newSubtree) {
            if (path == null) {
                return newSubtree;
            } else {
                List<PolyTree> sons = tree.getSons();
                List<PolyTree> newSons = new ArrayList<>(sons.size());
                int sonIndex = path.getSonIndex();
                int i = 0;
                for (PolyTree son : sons) {
                    newSons.add( i == sonIndex ? changeSubtree(son, path.getTail(), newSubtree) : son );
                    i++;
                }
                return new PolyTree(tree.getSymbol(),tree.getType(),newSons); //mkTree(getNamedComb(), newSons);
            }
        }

        public static PolyTree getSubtree(PolyTree tree, TreePath path) {
            if (path == null) {
                return tree;
            } else {
                return getSubtree(tree.getSons().get(path.getSonIndex()), path.getTail());
            }
        }

    }


}
