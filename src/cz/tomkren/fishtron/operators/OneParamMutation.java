package cz.tomkren.fishtron.operators;

import cz.tomkren.fishtron.reusegen.QuerySolver;
import cz.tomkren.fishtron.terms.*;
import cz.tomkren.fishtron.workflows.DataScientistLibs;
import cz.tomkren.fishtron.workflows.TypedDag;

import net.fishtron.trees.SubtreePos;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Created by tom on 6.7.2015. */

public class OneParamMutation extends PolyTreeMutation {

    private final Random rand;
    private List<AB<Integer,Double>> shiftsWithProbabilities;


    public OneParamMutation(double operatorProbability, Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {
        super(operatorProbability);
        this.shiftsWithProbabilities = shiftsWithProbabilities;
        this.rand = rand;
    }

    public OneParamMutation(double operatorProbability, Random rand, JSONArray shiftsWithProbabilities_json) {
        this(operatorProbability, rand, parseShiftsWithProbabilities(shiftsWithProbabilities_json));
    }

    // todo odpreparovat..
    @Deprecated
    public OneParamMutation(JSONObject config,Random rand) {
        this(config.getJSONObject("oneParamMutation").getDouble("probability"),rand, config.getJSONObject("oneParamMutation").getJSONArray("shiftsWithProbabilities"));
    }


    private JSONObject opts;
    @Override public JSONObject getOperatorInfo() {return opts;}

    public OneParamMutation(Random rand, JSONObject opts) {
        this(opts.getDouble("probability"),rand, opts.getJSONArray("shiftsWithProbabilities"));
        this.opts = opts;
    }



    @Override
    public PolyTree mutate(PolyTree tree) {
        List<SubtreePos> posesWithParams = tree.getAllSubtreePosesWhere(OneParamMutation::hasCodeNodeWithMoreThanZeroParams);
        if (posesWithParams.isEmpty()) {return tree;}

        SubtreePos subtreePos = F.randomElement(posesWithParams, rand);
        PolyTree selectedSubtree = tree.getSubtree(subtreePos);

        PolyTree newSubtree = selectedSubtree.randomlyShiftOneParam(rand, shiftsWithProbabilities);
        return tree.changeSubtree(subtreePos, newSubtree);
    }

    private static boolean hasCodeNodeWithMoreThanZeroParams(PolyTree tree) {
        return tree.getSymbol() instanceof SmartSymbolWithParams && ((SmartSymbolWithParams) tree.getSymbol()).numParams() > 0;
    }



    public static List<AB<Integer,Double>> parseShiftsWithProbabilities(JSONArray jsonArray) {
        List<AB<Integer,Double>> ret = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray pair = jsonArray.getJSONArray(i);
            ret.add(new AB<>(pair.getInt(0), pair.getDouble(1)));
        }
        return ret;
    }

    public static void main(String[] args) {

        Checker ch = new Checker(); // seed : 7268261639444262123L

        SmartLibrary lib = DataScientistLibs.DATA_SCIENTIST_WITH_PARAMS_01;

        QuerySolver qs = new QuerySolver(lib, ch.getRandom());

        JSONArray testJsonShifts = new JSONArray("[[-2, 0.1], [-1, 0.4], [1, 0.4], [2, 0.1]]");

        ch.eqStr(parseShiftsWithProbabilities(testJsonShifts), Arrays.asList(AB.mk(-2, 0.1), AB.mk(-1, 0.4), AB.mk(1, 0.4), AB.mk(2, 0.1)));


        OneParamMutation oneParamMutation = new OneParamMutation(1.0, ch.getRandom(), Arrays.asList(
                AB.mk(-2,0.1),
                AB.mk(-1,0.4),
                AB.mk( 1,0.4),
                AB.mk( 2,0.1)
        ));



        List<PolyTree> trees = qs.uniformGenerateWithRandomizedParams("D => LD", 20, 100);

        for (PolyTree tree : trees) {
            Log.it("-----------------------------------------------------------------");

            Log.it(tree);
            PolyTree mutant = oneParamMutation.mutate(tree);
            //ch.eqStrSilent(tree, mutant); //  předělat vypisování aby todle neplatilo -- hotovo

            ch.it(((TypedDag) tree.computeValue()).toJson());
            ch.it( ((TypedDag)mutant.computeValue()).toJson() );

        }


        ch.results();
    }
}
