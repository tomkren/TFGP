package cz.tomkren.fishtron.ugen.eva;

import net.fishtron.trees.AppTree;
import cz.tomkren.fishtron.ugen.Gen;
import org.json.JSONObject;

import java.util.Random;

/** Same Size Subtree Mutation
 * Created by tom on 16. 2. 2017. */

public class SSSMutation extends AppTreeMutation {

    private final Gen gen;
    private final int maxSubtreeSize;
    private final Random rand;
    private final JSONObject allParamsInfo;
    private final JSONObject opts;

    private static final int MAX_TRIES = 10;

    SSSMutation(JSONObject opts, JSONObject allParamsInfo, Gen gen) {
        super(opts.getDouble("probability"));
        this.maxSubtreeSize = opts.getInt("maxSubtreeSize");
        this.gen = gen;
        this.rand = gen.getRand();
        this.allParamsInfo = allParamsInfo;
        this.opts = opts;
    }

    @Override public JSONObject getOperatorInfo() {return opts;}

    public AppTree mutate(AppTree tree) {
        AppTree mutant = null;
        for (int i = 0; i < MAX_TRIES; i++) {
            mutant = AppTree.mutate_sss(tree, gen, maxSubtreeSize, allParamsInfo, rand);
            if (!mutant.toString().equals(tree.toString())) {
                return mutant;
            }
        }
        return mutant;
    }

}
