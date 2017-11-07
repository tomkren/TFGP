package cz.tomkren.fishtron.operators;

import net.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.reusegen.QuerySolver;
import cz.tomkren.fishtron.terms.PolyTree;
import net.fishtron.types.Type;
import net.fishtron.types.Types;

import java.util.List;

/** Created by tom on 6.7.2015. */

public class RandomParamsPolyTreeGenerator implements IndivGenerator<PolyTree> {

    private final Type goalType;
    private final int maxTreeSize;
    private final QuerySolver querySolver;

    public RandomParamsPolyTreeGenerator(Type goalType, int maxTreeSize, QuerySolver querySolver) {
        this.goalType = goalType;
        this.maxTreeSize = maxTreeSize;
        this.querySolver = querySolver;
    }

    public RandomParamsPolyTreeGenerator(String goalType, int maxTreeSize, QuerySolver querySolver) {
        this(Types.parse(goalType), maxTreeSize, querySolver);
    }

    @Override
    public List<PolyTree> generate(int numIndivs) {
        return querySolver.uniformGenerateWithRandomizedParams(goalType, maxTreeSize, numIndivs);
    }
}
