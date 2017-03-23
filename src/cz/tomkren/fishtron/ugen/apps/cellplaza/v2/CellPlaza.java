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

public class CellPlaza {

    public static final String BASE_DIR = "cellplaza";
    public static final String CONFIG_PATH = BASE_DIR + "/config.json";

    public static void main(String[] args) {
        Checker ch = new Checker();
        JSONObject config = F.loadJson(CONFIG_PATH).getJSONObject("cellPlaza");
        run(config, ch);
        ch.results();
    }

    private static void run(JSONObject config, Checker ch) {
        JSONArray plazasToRun = config.getJSONArray("run");
        JSONObject plazaConfigs = config.getJSONObject("plazas");
        for (String plazaDir : F.map(plazasToRun, x->(String)x)) {
            runPlaza(plazaDir, plazaConfigs.getJSONObject(plazaDir), ch);
        }
    }

    private static void runPlaza(String plazaDir, JSONObject plazaConfig, Checker ch) {
        Random rand = ch.getRandom();
        int numStates = plazaConfig.getInt("numStates");
        JSONArray pixelSizes = plazaConfig.getJSONArray("pixelSizes");

        CellOpts opts = new CellOpts(numStates, plazaDir, pixelSizes, false);

        EvalLib lib = Libs.mkLib(opts);
        JSONObject allParamsInfo = Libs.mkAllParamsInfo(opts, ch);

        AB<AppTree,Rule> rule = genBitRule(lib, allParamsInfo, ch);

        JSONArray coreNames = allParamsInfo.getJSONObject("seedImg").getJSONArray("filename");
        String coreName = (String) F.randomElement(coreNames, rand);

        CellWorld w = new CellWorld(opts, coreName, rule._2(), false);

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

    private static AB<AppTree,Rule> genBitRule(EvalLib lib, JSONObject allParamsInfo, Checker ch) {
        Gen gen = new Gen(Libs.gamma, ch);
        AppTree tree = gen.genOne(1, Libs.ruleType);
        tree = tree.randomizeParams(allParamsInfo, ch.getRandom());
        Rule rule = (Rule) lib.eval(tree);
        return AB.mk(tree,rule);
    }


}
