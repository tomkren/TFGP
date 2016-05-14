package cz.tomkren.fishtron.mains;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.fishtron.workflows.VisualisationClient;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
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

/** Created by tom on 9.5.2016. */

public class DagEvaTester {


    public static void main(String[] args) {


        //String jsonConfigFilename =  "configs/dageva/config.json" ;
        //String jsonConfigFilename =  "configs/dageva/config_stacking.json" ;
        //String jsonConfigFilename =  "configs/dageva/config_stacking2.json" ;
        //String jsonConfigFilename =  "configs/dageva/config_stackAndBoo.json" ;
        //String jsonConfigFilename =  "configs/dageva/config_stackAndBoo2.json" ;
        String jsonConfigFilename =  "configs/dageva/config_stackAndBoo3.json" ;
        //String jsonConfigFilename =  "configs/dageva/config_stackAndBoo4.json" ;


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

            String testParamsInfoStr = "{\"DT\": {\"min_samples_split\": [1, 2, 5, 10, 20], \"criterion\": [\"gini\", \"entropy\"], \"max_features\": [0.05, 0.1, 0.25, 0.5, 0.75, 1], \"min_samples_leaf\": [1, 2, 5, 10, 20], \"max_depth\": [1, 2, 5, 10, 15, 25, 50, 100]}, \"gaussianNB\": {}, \"SVC\": {\"gamma\": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"union\": {}, \"copy\": {}, \"PCA\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1], \"whiten\": [false, true]}, \"logR\": {\"penalty\": [\"l1\", \"l2\"], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"kMeans\": {}, \"kBest\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, \"vote\": {}}";
            JSONObject allParamsInfo = new JSONObject(testParamsInfoStr);

            String classPrefix = "cz.tomkren.fishtron.workflows.";

            SmartLibrary lib = SmartLibrary.mk(classPrefix, allParamsInfo, config.getJSONArray("lib"));
            String goalTypeStr = config.getString("goalType");
            Type goalType = Types.parse(goalTypeStr);
            QuerySolver querySolver = new QuerySolver(lib, rand);

            IndivGenerator<PolyTree> generator = new RandomParamsPolyTreeGenerator(goalType, config.getInt("generatingMaxTreeSize"), querySolver);

            /*int upToTreeSize = 20;
            TMap<PolyTree> treeTMap = querySolver.generateAllUpTo(goalTypeStr, upToTreeSize);
            List<PolyTree> trees = treeTMap.get(goalType);*/

            List<PolyTree> trees = generator.generate(config.getInt("populationSize"));


            logList("trees", trees);

            List<Object> objs = F.map(trees, PolyTree::computeValue);
            List<TypedDag> dags = F.map(objs, o -> (TypedDag)o);
            List<String> jsonTrees = F.map(dags, dag -> dag.toJson());
            List<JSONArray> kutilJsonTrees = F.map(dags, dag -> dag.toKutilJson(0,0));


            logList("json", jsonTrees);
            logList("trees", trees);
            logList("kutil-json", kutilJsonTrees);

            Log.it("num trees: "+ trees.size());

            //KutilMain.showDags(dags);

            //new VisualisationClient("127.0.0.1", 4223).showDags(dags);
            new VisualisationClient("192.168.0.12", 4223).showDags(dags);




        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (JSONException e) {
            Log.itln("JSON error: " + e.getMessage());
        } /*catch (XmlRpcException e) {
            Log.it("Dag-evaluate server error: Server is probably not running (or it is starting right now). Start the server and try again, please.");
        }*/


    }

    private static void logList(String tag, List<?> list) {
        Log.it("<"+tag+" begin>");
        Log.list(list);
        Log.it("<"+tag+" end>\n");
    }




}
