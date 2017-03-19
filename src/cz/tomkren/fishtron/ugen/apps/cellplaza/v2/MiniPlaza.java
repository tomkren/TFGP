package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

/** Created by tom on 19.03.2017 */

public class MiniPlaza {

    private static final JSONObject config = F.obj(
        "numStates", 3,
        "plazaDir", "mini_10",
        "pixelSizes", F.arr(5,1)
    );

    public static void main(String[] args) {
        Checker ch = new Checker();

        main_1(config, ch);

        ch.results();
    }

    private static void main_1(JSONObject config, Checker ch) {
        Random rand = ch.getRandom();

        int numStates = config.getInt("numStates");
        String plazaDir = config.getString("plazaDir");
        JSONArray pixelSizes = config.getJSONArray("pixelSizes");

        EvalLib lib = Libs.mkLib(numStates, plazaDir, pixelSizes);
        JSONObject allParamsInfo = Libs.mkAllParamsInfo(numStates, plazaDir);

        AB<AppTree,Rule> rule = genBitRule(lib, allParamsInfo, rand);

        JSONArray coreNames = allParamsInfo.getJSONObject("seedImg").getJSONArray("filename");
        String coreName = (String) F.randomElement(coreNames, rand);

        CellWorld w = new CellWorld(numStates, plazaDir, coreName, rule._2(), pixelSizes);

        int numSteps = 100;
        w.writeState();
        for (int s = 0; s < numSteps; s++) {
            w.step();
            w.writeState();
        }

        Log.it();
        Log.it("ruleCode = "+rule._1());
        Log.it("coreName = "+coreName);
    }

    private static AB<AppTree,Rule> genBitRule(EvalLib lib, JSONObject allParamsInfo, Random rand) {
        Gen gen = new Gen(Libs.gamma, rand);
        AppTree tree = gen.genOne(1, Libs.ruleType);
        tree = tree.randomizeParams(allParamsInfo, rand);
        Rule rule = (Rule) lib.eval(tree);
        return AB.mk(tree,rule);
    }




}
