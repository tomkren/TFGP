package cz.tomkren.fishtron.operators;

import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.terms.PolyTree;
import net.fishtron.utils.F;

import java.util.List;

/** Created by tom on 6.7.2015. */

public abstract class PolyTreeMutation implements Operator<PolyTree> {

    public abstract PolyTree mutate(PolyTree tree);

    private double operatorProbability;

    public PolyTreeMutation(double operatorProbability) {
        this.operatorProbability = operatorProbability;
    }

    @Override
    public double getWeight() {
        return operatorProbability;
    }

    @Override
    public int getNumInputs() {
        return 1;
    }

    @Override
    public List<PolyTree> operate(List<PolyTree> parents) {
        return F.singleton(mutate(parents.get(0)));
    }
}
