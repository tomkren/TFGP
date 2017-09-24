package cz.tomkren.fishtron.mains;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import net.fishtron.types.TMap;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.Checker;
import net.fishtron.utils.F;
import cz.tomkren.utils.Log;
import cz.tomkren.fishtron.operators.RandomParamsPolyTreeGenerator;
import cz.tomkren.fishtron.reusegen.QuerySolver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/** Created by tom on 9.5.2016. */

public class DagEvaTester {

    public static final JSONObject testParamsInfo = new JSONObject("{\"DT\": {\"min_samples_split\": [1, 2, 5, 10, 20], \"criterion\": [\"gini\", \"entropy\"], \"max_features\": [0.05, 0.1, 0.25, 0.5, 0.75, 1], \"min_samples_leaf\": [1, 2, 5, 10, 20], \"max_depth\": [1, 2, 5, 10, 15, 25, 50, 100]}, \"gaussianNB\": {}, \"SVC\": {\"gamma\": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"union\": {}, \"copy\": {}, \"PCA\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1], \"whiten\": [false, true]}, \"logR\": {\"penalty\": [\"l1\", \"l2\"], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"kMeans\": {}, \"kBest\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, \"vote\": {}}");

    private JSONObject testInfo;

    public static void main(String[] args) {
        DagEvaTester t = new DagEvaTester();
        t.runTest();
    }

    public DagEvaTester() {
        testInfo = F.obj("status","beforeStart");
    }

    public synchronized JSONObject getTestInfo() {
        return testInfo;
    }
    private synchronized void updateTestInfo(Function<JSONObject,JSONObject> updateFun) {
        testInfo = updateFun.apply(testInfo);
    }


    public void runTest() {

        updateTestInfo(info -> info.put("status","running"));

        //testInfo = F.obj("status","running");

        //String jsonConfigFilename = "configs/dageva/config.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stacking.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stacking2.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stackAndBoo.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stackAndBoo2.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stackAndBoo3.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stackAndBoo4.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stacking_ListAnot.json" ;
        String jsonConfigFilename = "configs/dageva/config_stacking_ListAnot2.json" ;
        //String jsonConfigFilename = "configs/dageva/config_stackAndBoo_ListAnot.json" ;

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



            String classPrefix = "cz.tomkren.fishtron.workflows.";
            SmartLibrary lib = SmartLibrary.mk(classPrefix, testParamsInfo, config.getJSONArray("lib"));
            String goalTypeStr = config.getString("goalType");
            Type goalType = Types.parse(goalTypeStr);
            QuerySolver querySolver = new QuerySolver(lib, rand);

            IndivGenerator<PolyTree> generator = new RandomParamsPolyTreeGenerator(goalType, config.getInt("generatingMaxTreeSize"), querySolver);


            /*int upToTreeSize = 20;
            TMap<PolyTree> treeTMap = querySolver.generateAllUpTo(goalTypeStr, upToTreeSize);
            List<PolyTree> trees = treeTMap.get(goalType);*/

            // todo #LeDEBUG
            //TMap<PolyTree> treesTMap = querySolver.generateAll("((V LD (S (S 0)) Disj) => LD)", 3);

            TMap<PolyTree> treesTMap = querySolver.generateAllUpTo(goalTypeStr, 20);
            Log.it(treesTMap);





            List<PolyTree> trees = generator.generate(config.getInt("populationSize"));


            logList("trees", trees);

            List<Object> objs = F.map(trees, PolyTree::computeValue);
            List<TypedDag> dags = F.map(objs, o -> (TypedDag)o);
            List<String> jsonTrees = F.map(dags, dag -> dag.toJson());
            List<JSONArray> kutilJsonTrees = F.map(dags, dag -> dag.toKutilJson(0,0));


            logJsonTrees("json", jsonTrees);
            logList("trees", trees);
            logList("kutil-json", kutilJsonTrees);



            updateTestInfo(info -> info.put("kutil-dags", prettyDags(dags) ));



            Log.it("num trees: "+ trees.size());

            //KutilMain.showDags(dags);

            //new VisualisationClient("127.0.0.1", 4223).showDags(dags);
            //new VisualisationClient("192.168.0.12", 4223).showDags(dags);

            updateTestInfo(info -> info.put("status","finished"));



        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (JSONException e) {
            Log.itln("JSON error: " + e.getMessage());
        } /*catch (XmlRpcException e) {
            Log.it("Dag-evaluate server error: Server is probably not running (or it is starting right now). Start the server and try again, please.");
        }*/


    }

    private JSONArray prettyDags(List<TypedDag> dags) {

        JSONArray ret = new JSONArray();

        int width = (int)( 18000 / Math.sqrt(1000) * Math.sqrt(dags.size()) ) ; //16000;
        int okraj = 20;
        int init  = 3*okraj;

        int x = init;
        int y = init;

        int maxHeight = 0;

        //try {

            //sendCmd("clearInside $main");

            for (TypedDag dag : dags) {

                ret.put( dag.toKutilJson(x,y) );

                x += dag.getPxWidth() + okraj;

                if (dag.getPxHeight() > maxHeight) {
                    maxHeight = dag.getPxHeight();
                }

                if (x > width) {
                    x = init;
                    y += maxHeight + okraj;
                }
            }

        /*} catch (Exception e) {
            Log.err("(!!!) VisualisationClint exception : " + e.getMessage());
        }*/

        return ret;
    }

    private static JSONArray mkJsonArr(List<JSONArray> xss) {
        JSONArray ret = new JSONArray();
        for (JSONArray xs : xss) {
            ret.put(xs);
        }
        return ret;
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
                sb.append(",\n");
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



}
