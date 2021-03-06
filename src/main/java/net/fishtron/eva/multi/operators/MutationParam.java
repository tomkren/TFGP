package net.fishtron.eva.multi.operators;

import net.fishtron.trees.AppTree;
import net.fishtron.trees.params.Params;
import net.fishtron.utils.AB;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**Created by tom on 09.03.2017.*/

public class MutationParam extends MutationAppTreeMI {

    private final Random rand;
    private final List<AB<Integer,Double>> shiftsWithProbabilities;
    private final JSONObject opts;

    MutationParam(JSONObject opts, Random rand) {
        super(opts.getDouble("probability"));
        this.shiftsWithProbabilities = Params.parseShifts(opts.getJSONArray("shiftsWithProbabilities"));
        this.rand = rand;
        this.opts = opts;
    }

    @Override public JSONObject getOperatorInfo() {return opts;}

    @Override
    public AppTree mutate(AppTree tree) {
        return AppTree.mutate_param(tree, shiftsWithProbabilities, rand);
    }

}
