package cz.tomkren.fishtron.ugen.apps.workflows;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eva.AppTreeIndiv;
import cz.tomkren.fishtron.ugen.eva.AppTreeIndivGenerator;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/** Created by tom on 17. 2. 2017. */

public class GPMLTester {

    public static void main(String[] args) {
        GPMLTester t = new GPMLTester();
        t.runTest();
    }

    private static final JSONObject test_allParamsInfo = new JSONObject("{\"DT\": {\"min_samples_split\": [1, 2, 5, 10, 20], \"criterion\": [\"gini\", \"entropy\"], \"max_features\": [0.05, 0.1, 0.25, 0.5, 0.75, 1], \"min_samples_leaf\": [1, 2, 5, 10, 20], \"max_depth\": [1, 2, 5, 10, 15, 25, 50, 100]}, \"gaussianNB\": {}, \"SVC\": {\"gamma\": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"union\": {}, \"copy\": {}, \"PCA\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1], \"whiten\": [false, true]}, \"logR\": {\"penalty\": [\"l1\", \"l2\"], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"kMeans\": {}, \"kBest\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, \"vote\": {}}");

    private JSONObject testInfo;

    public GPMLTester() {
        testInfo = F.obj("status","beforeStart");
    }


    public void runTest() {

        updateTestInfo(info -> info.put("status","running"));

        String jsonConfigFilename = "configs/gpml/config.json";

        try {

            String configStr = Files.toString(new File(jsonConfigFilename), Charsets.UTF_8);
            Log.itln(jsonConfigFilename + " = " + configStr);
            JSONObject config = new JSONObject(configStr);

            Long seed = config.has("seed") ? config.getLong("seed") : null;
            Checker checker = new Checker(seed);
            Random rand = checker.getRandom();

            if (seed == null) {
                config.put("seed", checker.getSeed());
            }

            Type goal   = Workflows.goal;
            Gamma gamma = Workflows.gamma;
            EvalLib lib = Workflows.lib;

            Gen gen = new Gen(gamma, rand);

            IndivGenerator<AppTreeIndiv> generator = new AppTreeIndivGenerator(
                    goal, config.getInt("generatingMaxTreeSize"), gen, lib,test_allParamsInfo, false);


            int numToGen = config.getInt("numIndividualsToGenerate");

            List<AppTreeIndiv> trees = generator.generate(numToGen);

            logList("trees", trees);

            List<Object> objs = F.map(trees, AppTreeIndiv::computeValue);
            List<TypedDag> dags = F.map(objs, o -> (TypedDag)o);
            List<String> jsonTrees = F.map(dags, dag -> dag.toJson());
            List<JSONArray> kutilJsonTrees = F.map(dags, dag -> dag.toKutilJson(0,0));

            logJsonTrees("json", jsonTrees);
            logList("trees", trees);
            logList("kutil-json", kutilJsonTrees);

            updateTestInfo(info -> info.put("kutil-dags", prettyDags(dags) ));


        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (JSONException e) {
            Log.itln("JSON error: " + e.getMessage());
        }


    }


    private static void logList(String tag, List<?> list) {
        Log.it("<"+tag+" begin>");
        Log.list(list);
        Log.it("<"+tag+" end>\n");
    }

    private static void logJsonTrees(String tag, List<String> jsonTrees) {

        StringBuilder sb = new StringBuilder();

        int i = 1;
        sb.append("[\n");
        for (String s : jsonTrees) {

            sb.append(s);

            if (i<jsonTrees.size()) {
                sb.append(","+"\n");
            } else {
                sb.append("\n]\n");
            }

            i++;
        }

        String res = sb.toString();

        F.writeFile("dagz.json", res);

        Log.it("<"+tag+" begin>");
        Log.it(res);
        Log.it("<"+tag+" end>\n");
    }

    public synchronized JSONObject getTestInfo() {
        return testInfo;
    }

    private synchronized void updateTestInfo(Function<JSONObject,JSONObject> updateFun) {
        testInfo = updateFun.apply(testInfo);
    }

    private JSONArray prettyDags(List<TypedDag> dags) {

        JSONArray ret = new JSONArray();

        int okraj = 20;
        int width = (int)( 18000 / Math.sqrt(1000) * Math.sqrt(dags.size()) ) ; //16000;
        int init  = 3*okraj;

        int x = init;
        int y = init;

        int maxHeight = 0;

        for (TypedDag dag : dags) {

            ret.put( dag.toKutilJson(x,y) );

            x += dag.getPxWidth() + okraj;

            if (dag.getPxHeight() > maxHeight) {
                maxHeight = dag.getPxHeight();
            }

            if (x > width) {
                y += maxHeight + okraj;
                x = init;
            }
        }

        return ret;
    }


}
