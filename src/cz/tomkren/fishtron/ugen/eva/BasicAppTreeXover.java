package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.types.TMap;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.TODO;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Created by tom on 15. 2. 2017.*/

// TODO kontrolovat zda se fakt strom změnil (třeba na 5 pokusů)

public class BasicAppTreeXover implements Operator<AppTreeIndiv> {

    private double operatorProbability;
    private final Random rand;
    private final int maxTreeSize;
    private JSONObject opts;

    public BasicAppTreeXover(JSONObject opts, Random rand) {
        this.opts = opts;
        this.operatorProbability = opts.getDouble("probability");
        this.maxTreeSize = opts.getInt("maxTreeSize");
        this.rand = rand;
    }

    @Override public JSONObject getOperatorInfo() {return opts;}
    @Override public double getWeight() {return operatorProbability;}
    @Override public int getNumInputs() {return 2;}

    @Override
    public List<AppTreeIndiv> operate(List<AppTreeIndiv> parents) {
        AppTreeIndiv mum = parents.get(0);
        AppTreeIndiv dad = parents.get(1);
        EvalLib lib = mum.getLib();

        AA<AppTree> children = xover(mum.getTree(), dad.getTree());

        AppTreeIndiv child1 = new AppTreeIndiv(children._1(), lib);
        AppTreeIndiv child2 = new AppTreeIndiv(children._2(), lib);
        return Arrays.asList(child1,child2);
    }


    private AA<AppTree> xover(AppTree mum, AppTree dad) {

        TMap<SubtreePos> mumPoses = mum.getAllSubtreePoses_byTypes();

        // TODO !!!
        throw new TODO();
    }


}
