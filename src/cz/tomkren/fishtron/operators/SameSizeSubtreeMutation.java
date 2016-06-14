package cz.tomkren.fishtron.operators;

import cz.tomkren.fishtron.reusegen.QuerySolver;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.workflows.DataScientistLibs;

import cz.tomkren.utils.Checker;
import cz.tomkren.utils.Log;

import org.json.JSONObject;
import java.util.List;
import java.util.Random;

/** Created by tom on 30. 6. 2015. */

// TODO !!! přidat parametr maxSubtreeSize páč nechceme generovaním obřích stromů strávit mládí (IMHO)

// TODO kontrolovat zda se fakt strom změnil (třeba na 5 pokusů)

public class SameSizeSubtreeMutation extends PolyTreeMutation {

    private final QuerySolver querySolver;
    private final int maxSubtreeSize;
    private final Random rand;

    public static final int MAX_TRIES = 10;

    public SameSizeSubtreeMutation(double operatorProbability, QuerySolver querySolver, int maxSubtreeSize) {
        super(operatorProbability);
        this.querySolver = querySolver;
        this.maxSubtreeSize = maxSubtreeSize;
        rand = querySolver.getRand();
    }

    // TODO odpreparovat tento konstruktor...
    @Deprecated
    public SameSizeSubtreeMutation(JSONObject config, QuerySolver querySolver) {
        this(config.getJSONObject("sameSizeSubtreeMutation").getDouble("probability"), querySolver, config.getJSONObject("sameSizeSubtreeMutation").getInt("maxSubtreeSize"));
    }

    public SameSizeSubtreeMutation(QuerySolver querySolver, JSONObject opts) {
        this(opts.getDouble("probability"), querySolver, opts.getInt("maxSubtreeSize"));
    }

    public PolyTree mutate(PolyTree tree) {
        PolyTree mutant = null;
        for (int i = 0; i < MAX_TRIES; i++) {
            mutant = mutate_oneTry(tree);
            if (!mutant.toString().equals(tree.toString())) {
                return mutant;
            }
        }
        return mutant;
    }

    public PolyTree mutate_oneTry(PolyTree tree) {

        // select subtree
        SubtreePos subtreePos;
        PolyTree subTree;
        do {
            subtreePos = tree.getRandomSubtreePos(rand);
            subTree = tree.getSubtree(subtreePos);
        } while (subTree.getSize() > maxSubtreeSize);

        // generate new subtree with same size and type
        Type goalType = subTree.getType();
        int treeSize  = subTree.getSize();
        PolyTree newSubtree = querySolver.generateOneWithRandomizedParams(goalType, treeSize);

        //Log.it("sub: "+subTree);
        //Log.it("new: "+newSubtree);

        // create new tree with that subtree
        return tree.changeSubtree(subtreePos, newSubtree);
    }


    public static void main(String[] args) {
        Checker ch = new Checker();

        SmartLibrary lib = DataScientistLibs.DATA_SCIENTIST_01;
        QuerySolver querySolver = new QuerySolver(lib, ch.getRandom());
        List<PolyTree> trees = querySolver.simpleUniformGenerate("D => LD", 35, 100);

        SameSizeSubtreeMutation mut = new SameSizeSubtreeMutation(1.0, querySolver,35);

        for (PolyTree tree : trees) {
            PolyTree mutant = mut.mutate_oneTry(tree);
            Log.it(tree);
            Log.it(mutant);
            Log.it("------------------------------------------");
        }



        ch.results();
    }
}
