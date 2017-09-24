package cz.tomkren.fishtron.ugen.multi.operators;

import cz.tomkren.fishtron.eva.Operator;
import net.fishtron.trees.AppTree;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import net.fishtron.utils.F;

import java.util.List;

/**Created by tom on 09.03.2017.*/

public abstract class MutationAppTreeMI implements Operator<AppTreeMI> {
    public abstract AppTree mutate(AppTree tree);

    private double operatorProbability;

    MutationAppTreeMI(double operatorProbability) {
        this.operatorProbability = operatorProbability;
    }

    @Override public double getWeight() {return operatorProbability;}
    @Override public int getNumInputs() {return 1;}

    @Override
    public List<AppTreeMI> operate(List<AppTreeMI> parents) {
        AppTreeMI parent = parents.get(0);
        AppTree child = mutate(parent.getTree());
        return F.singleton(new AppTreeMI(child));
    }
}
