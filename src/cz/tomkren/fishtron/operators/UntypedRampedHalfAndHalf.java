package cz.tomkren.fishtron.operators;

import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.fishtron.terms.SmartSymbol;
import cz.tomkren.utils.F;

import java.util.*;

/** Created by tom on 21.11.2015.*/

public class UntypedRampedHalfAndHalf implements IndivGenerator<PolyTree> {

    public static final int MAX_DEPTH = 6;

    private final Random rand;

    private final List<SmartSymbol> all;
    private final List<SmartSymbol> terminals;
    private final List<SmartSymbol> functions;

    private final boolean checkUnique;
    private Set<String> uniqueSet;

    private int miss;

    public UntypedRampedHalfAndHalf(SmartLibrary lib, Random rand, boolean checkUnique) {
        this.rand = rand;

        all = lib.getSyms();
        F.Partition<SmartSymbol> p = new F.Partition<>( all , SmartSymbol::isTerminal);
        terminals = p.getOK();
        functions = p.getKO();
        this.checkUnique = checkUnique;

        miss = 0;
        if (checkUnique) {
            uniqueSet = new LinkedHashSet<>(); //todo zvážit konstruktor
        }

        if (terminals.isEmpty()) {
            throw new Error("There must be at least one terminal in the library!");
        }
    }

    public int getNumMisses() {return miss;}

    public List<PolyTree> generate(int numTrees) {
        List<PolyTree> ret = new ArrayList<>(numTrees);
        for (int i = 0; i < numTrees; i++) {
            PolyTree newTree = generateOne();

            if (checkUnique) {
                String newTreeStr = newTree.toString();
                if (uniqueSet.contains(newTreeStr)) {
                    miss ++;
                    i--;
                } else {
                    ret.add(newTree);
                    uniqueSet.add(newTreeStr);
                }
            } else {
                ret.add(newTree);
            }
        }
        return ret;
    }

    public PolyTree generateOne() {
        int currentMaxDepth = 2 + rand.nextInt(MAX_DEPTH-1);
        return generateOne(0, currentMaxDepth);
    }

    public PolyTree generateOne(int depth, int currentMaxDepth) {
        List<SmartSymbol> candidates = rand.nextBoolean() ? fullSelector(depth,currentMaxDepth) : growSelector(depth,currentMaxDepth);
        SmartSymbol selectedSymbol = F.randomElement( candidates , rand);

        int numSons = selectedSymbol.getArity();
        List<PolyTree> sons = numSons > 0 ? new ArrayList<>(numSons) : Collections.emptyList();

        for (int i = 0; i < numSons; i++) {
            sons.add( generateOne(depth+1,currentMaxDepth) );
        }

        return new PolyTree(selectedSymbol, selectedSymbol.getOutputType(), sons);
    }

    public List<SmartSymbol> fullSelector(int depth, int currentMaxDepth) {
        return depth < currentMaxDepth ? functions : terminals;
    }

    public List<SmartSymbol> growSelector(int depth, int currentMaxDepth) {
        if (depth == 0) {return functions;}
        else if (depth < currentMaxDepth) {return all;}
        else {return terminals;}
    }



}
