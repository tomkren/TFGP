package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.AppTree;
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

    public SSSMutation(JSONObject opts, JSONObject allParamsInfo, Gen gen) {
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
            mutant = mutate_oneTry(tree);
            if (!mutant.toString().equals(tree.toString())) {
                return mutant;
            }
        }
        return mutant;
    }

    private AppTree mutate_oneTry(AppTree tree) {

        // select subtree
        SubtreePos subtreePos;
        AppTree subTree;
        do {
            subtreePos = tree.getRandomSubtreePos(rand);
            subTree = tree.getSubtree(subtreePos);
        } while (subTree.size() > maxSubtreeSize);

        // generate new subtree with same size and type
        Type goalType = subTree.getType();
        int treeSize = subTree.size();
        AppTree newSubtree = gen.genOne(treeSize, goalType);
        AppTree newSubtreeWithParams = newSubtree.randomizeParams(allParamsInfo, rand);

        //Log.it("sub: "+subTree);
        //Log.it("new: "+newSubtreeWithParams);

        // create new tree with that subtree
        return tree.changeSubtree(subtreePos, newSubtreeWithParams);
    }

}
