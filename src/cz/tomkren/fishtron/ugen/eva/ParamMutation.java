package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.params.Params;
import cz.tomkren.utils.AB;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/** One Parameter Mutation
 * Created by user on 16. 2. 2017. */

public class ParamMutation extends AppTreeMutation {

    private final Random rand;
    private final List<AB<Integer,Double>> shiftsWithProbabilities;
    private final JSONObject opts;

    ParamMutation(JSONObject opts, Random rand) {
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
