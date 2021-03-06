package net.fishtron.apps.gpml;

import net.fishtron.eval.EvalLib;
import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.eva.multi.MultiEvalResult;
import net.fishtron.trees.AppTree;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

import net.fishtron.apps.gpml.dag.NewSimpleTypedDag;
//import c z.tomkren.fishtron.workflows.TypedDag;


/**Created by tom on 17.03.2017.*/

public class DummyMultiEvalManager implements XmlRpcServer_MultiEvalManager<AppTreeMI> {

    private EvalLib lib;
    private Queue<JSONObject> fakeEvalQueue;
    private Map<Integer, AB<AppTreeMI,JSONObject>> id2indivData;
    private int nextId;
    private boolean pollRandomNumber;
    private Checker checker;

    DummyMultiEvalManager(EvalLib lib, boolean pollRandomNumber, Checker ch) {
        this.lib = lib;
        fakeEvalQueue = new ArrayQueue<>();
        id2indivData = new HashMap<>();
        nextId = 1;
        this.pollRandomNumber = pollRandomNumber;
        this.checker = ch;
    }

    @Override
    public JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException {
        //dřív byl: import c z.tomkren.fishtron.mains.DagEvaTester;
        //return DagEvaTester.testParamsInfo;
        return new JSONObject("{\"DT\": {\"min_samples_split\": [1, 2, 5, 10, 20], \"criterion\": [\"gini\", \"entropy\"], \"max_features\": [0.05, 0.1, 0.25, 0.5, 0.75, 1], \"min_samples_leaf\": [1, 2, 5, 10, 20], \"max_depth\": [1, 2, 5, 10, 15, 25, 50, 100]}, \"gaussianNB\": {}, \"SVC\": {\"gamma\": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"union\": {}, \"copy\": {}, \"PCA\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1], \"whiten\": [false, true]}, \"logR\": {\"penalty\": [\"l1\", \"l2\"], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"kMeans\": {}, \"kBest\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, \"vote\": {}}");
    }

    @Override
    public int getCoreCount() {
        return 16;
    }

    @Override
    public Object submit(List<AB<AppTreeMI, JSONObject>> indivs) {
        JSONArray jsonIndivs = new JSONArray();

        for (AB<AppTreeMI,JSONObject> indivData : indivs) {

            id2indivData.put(nextId, indivData);

            AppTreeMI indiv = indivData._1();
            JSONObject indivJson = indivData._2();

            indiv.setId(nextId);
            indivJson.put("id", nextId);

            Object indivValue = indiv.computeValue(lib);
            JSONObject jsonCode = new JSONObject(((NewSimpleTypedDag) indivValue).toJson());

            AppTree tree = indiv.getTree();
            int workflowSize = SizeUtils.workflowSize(tree);
            int treeSize = tree.size();

            JSONObject indivDataToSubmit = F.obj(
                    "id",   nextId,
                    "code", jsonCode,
                    "workflowSize", workflowSize,
                    "treeSize", treeSize
            );

            nextId++;
            jsonIndivs.put(indivDataToSubmit);
        }

        return fakeInnerSubmit(jsonIndivs); // returns submitMsg
    }

    private Object fakeInnerSubmit(JSONArray jsonIndivs) {
        List<JSONObject> listIndivs = F.map(jsonIndivs, x->(JSONObject) x);
        fakeEvalQueue.addAll(listIndivs);
        return "FAKE SUBMIT SUCCESSFUL";
    }

    private JSONArray fakeGetEvaluated() {

        int howManyPoll = pollRandomNumber ? checker.getRandom().nextInt(fakeEvalQueue.size()+1) : 1;

        JSONArray evaluatedResults = new JSONArray();

        for (int i = 0; i<howManyPoll; i++) {

            JSONObject submittedIndivData = fakeEvalQueue.poll();

            int id = submittedIndivData.getInt("id");
            //JSONObject indivDagJson = submittedIndivData.getJSONObject("code");

            double fakeScore1 = submittedIndivData.getInt("workflowSize");
            double fakeStdDevScore = Math.random();
            double fakeScore2 = submittedIndivData.getInt("treeSize");

            JSONArray oneIndivResult = F.arr(id, F.arr(fakeScore1,fakeStdDevScore,fakeScore2));

            evaluatedResults.put(oneIndivResult);
        }


        return evaluatedResults; //F.arr(oneIndivResult);
    }

    @Override
    public MultiEvalResult<AppTreeMI> getEvaluated() {
        JSONArray json = fakeGetEvaluated();

        List<AB<AppTreeMI,JSONObject>> evaluatedIndividuals = new ArrayList<>(json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONArray evalRes = json.getJSONArray(i);
            evaluatedIndividuals.add(getIndivBack(evalRes));
        }

        return () -> evaluatedIndividuals;
    }

    private AB<AppTreeMI,JSONObject> getIndivBack(JSONArray evalResJsonArr) {

        int       id     = evalResJsonArr.getInt(0);
        JSONArray scores = evalResJsonArr.getJSONArray(1);

        double score_1 = (scores.length() > 0) ? scores.getDouble(0) : mkErrorPerformanceScore();
        double score_2 = (scores.length() > 2) ? scores.getDouble(2) : mkErrorTimeScore();


        AB<AppTreeMI,JSONObject> indivData = id2indivData.remove(id);
        if (indivData == null) {throw new Error("EvalResult for individual with non-existing id "+id+"!");}

        AppTreeMI indiv = indivData._1();
        JSONObject indivJson = indivData._2();


        List<Double> fitness = Arrays.asList(score_1, score_2);
        indiv.setFitness(fitness);

        indivJson.put("rawScores", scores);
        indivJson.put("fitness", F.jsonMap(fitness));

        return indivData;
    }

    private static double mkErrorPerformanceScore(){return -Double.MAX_VALUE;}
    private static double mkErrorTimeScore(){return Double.MAX_VALUE;}



    @Override
    public String quitServer() {
        return "Timmmmmmmmmmmy!";
    }
}
