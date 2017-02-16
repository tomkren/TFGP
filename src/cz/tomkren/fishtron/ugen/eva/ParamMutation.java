package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** One Parameter Mutation
 * Created by user on 16. 2. 2017. */

public class ParamMutation extends AppTreeMutation {

    private final Random rand;
    private final List<AB<Integer,Double>> shiftsWithProbabilities;
    private final JSONObject opts;

    public ParamMutation(JSONObject opts, Random rand) {
        super(opts.getDouble("probability"));
        this.shiftsWithProbabilities = parseShifts(opts.getJSONArray("shiftsWithProbabilities"));
        this.rand = rand;
        this.opts = opts;
    }

    @Override public JSONObject getOperatorInfo() {return opts;}

    private static List<AB<Integer,Double>> parseShifts(JSONArray jsonArray) {
        List<AB<Integer,Double>> ret = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray pair = jsonArray.getJSONArray(i);
            ret.add(new AB<>(pair.getInt(0), pair.getDouble(1)));
        }
        return ret;
    }

    @Override
    public AppTree mutate(AppTree tree) {
        List<SubtreePos> posesWithParams = tree.getAllSubtreePosesWhere(AppTree::hasParams);
        if (posesWithParams.isEmpty()) {return tree;}

        SubtreePos subtreePos = F.randomElement(posesWithParams, rand);
        AppTree selectedSubtree = tree.getSubtree(subtreePos);

        if (selectedSubtree instanceof AppTree.Leaf) {
            AppTree.Leaf selectedLeaf = (AppTree.Leaf) selectedSubtree;
            AppTree.Leaf newLeaf = selectedLeaf.randomlyShiftOneParam(rand, shiftsWithProbabilities);
            return tree.changeSubtree(subtreePos, newLeaf);
        } else {
            throw new Error("Selected subtree must be leaf, should be unreachable.");
        }
    }

}
