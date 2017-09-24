package cz.tomkren.fishtron.ugen.multi.operators;

import cz.tomkren.fishtron.eva.Operator;
import net.fishtron.trees.AppTree;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import net.fishtron.utils.AA;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**Created by tom on 09.03.2017.*/

public class XoverAppTreeMI implements Operator<AppTreeMI> {
    private final double operatorProbability;
    private final Random rand;
    private final int maxTreeSize;
    private final JSONObject opts;

    XoverAppTreeMI(JSONObject opts, Random rand) {
        this.opts = opts;
        this.operatorProbability = opts.getDouble("probability");
        this.maxTreeSize = opts.getInt("maxTreeSize");
        this.rand = rand;
    }

    @Override public JSONObject getOperatorInfo() {return opts;}
    @Override public double getWeight() {return operatorProbability;}
    @Override public int getNumInputs() {return 2;}

    @Override
    public List<AppTreeMI> operate(List<AppTreeMI> parents) {
        AppTreeMI mum = parents.get(0);
        AppTreeMI dad = parents.get(1);

        AA<AppTree> children = AppTree.xover(mum.getTree(), dad.getTree(), maxTreeSize, rand);

        AppTreeMI child1 = new AppTreeMI(children._1());
        AppTreeMI child2 = new AppTreeMI(children._2());
        return Arrays.asList(child1,child2);
    }

}
