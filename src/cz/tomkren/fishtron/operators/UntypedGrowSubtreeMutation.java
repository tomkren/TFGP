package cz.tomkren.fishtron.operators;


import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.fishtron.terms.SmartSymbol;
import cz.tomkren.utils.F;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/** Created by tom on 29.1.2016.*/

// TODO udělaná narychlo aby se otestovalo jestli neni libovolná mutace hustá, snažil jsem se udělat tu kozovskou

public class UntypedGrowSubtreeMutation extends PolyTreeMutation {


    private final Random rand;

    public static final int MAX_TRIES = 10;
    public int numFails;

    private final List<SmartSymbol> all;
    private final List<SmartSymbol> terminals;
    private final List<SmartSymbol> functions;



    public UntypedGrowSubtreeMutation(double operatorProbability, SmartLibrary lib, Random rand) {
        super(operatorProbability);
        this.rand = rand;

        all = lib.getSyms();
        F.Partition<SmartSymbol> p = new F.Partition<>( all , SmartSymbol::isTerminal);
        terminals = p.getOK();
        functions = p.getKO();

        numFails = 0;
    }

    private static final double pSelectInner = 0.9;
    private static final int maxDepth = 17;


    public PolyTree mutate_oneTry(PolyTree tree) {

        TreePath subtreePath = kozaSelectPath(tree);

        PolyTree newSubtree = generateOne(0, 1+rand.nextInt(5));

        PolyTree kid = TreePath.changeSubtree(tree, subtreePath, newSubtree);

        if (kid.depth() > maxDepth) { kid = tree; }

        return kid;
    }

    @Override
    public PolyTree mutate(PolyTree tree) {

        for (int i = 0; i < MAX_TRIES; i++) {
            PolyTree mutant = mutate_oneTry(tree);
            if (!mutant.toString().equals(tree.toString())) {
                //Log.it("Bond here! ");
                return mutant;
            }
        }

        numFails ++;
        return tree;
    }

    public PolyTree generateOne(int depth, int currentMaxDepth) {
        List<SmartSymbol> candidates = growSelector(depth,currentMaxDepth);
        SmartSymbol selectedSymbol = F.randomElement( candidates , rand);

        int numSons = selectedSymbol.getArity();
        List<PolyTree> sons = numSons > 0 ? new ArrayList<>(numSons) : Collections.emptyList();

        for (int i = 0; i < numSons; i++) {
            sons.add( generateOne(depth+1,currentMaxDepth) );
        }

        return new PolyTree(selectedSymbol, selectedSymbol.getOutputType(), sons);
    }

    public List<SmartSymbol> growSelector(int depth, int currentMaxDepth) {
        if (depth == 0) {return functions;}
        else if (depth < currentMaxDepth) {return all;}
        else {return terminals;}
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

    /*private static int getPathDepth(TreePath p) {
        if (p == null) {
            return 0;
        } else {
            return getPathDepth(p.getTail());
        }
    }*/

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
