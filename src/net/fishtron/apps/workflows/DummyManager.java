package net.fishtron.apps.workflows;

import java.util.*;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import net.fishtron.eva.simple.FitIndiv;
import net.fishtron.eva.simple.FitVal;
import net.fishtron.eva.simple.EvalResult;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;

import cz.tomkren.fishtron.workflows.TypedDag;


/** Created by tom on 17. 2. 2017.*/

public class DummyManager<Indiv extends FitIndiv> implements Dag_IEvalManager<Indiv> {


    private Queue<JSONObject> frontaNaVyhodnoceni;

    private Map<Integer, AB<Indiv,JSONObject>> id2indivData;
    private int nextId;

    DummyManager() {
        frontaNaVyhodnoceni = new ArrayQueue<>();

        id2indivData = new HashMap<>();
        nextId = 0;
    }

    @Override
    public JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException {
        //dřív bylo: import cz.tomkren.fishtron.mains.DagEvaTester;
        //return DagEvaTester.testParamsInfo;
        return new JSONObject("{\"DT\": {\"min_samples_split\": [1, 2, 5, 10, 20], \"criterion\": [\"gini\", \"entropy\"], \"max_features\": [0.05, 0.1, 0.25, 0.5, 0.75, 1], \"min_samples_leaf\": [1, 2, 5, 10, 20], \"max_depth\": [1, 2, 5, 10, 15, 25, 50, 100]}, \"gaussianNB\": {}, \"SVC\": {\"gamma\": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"union\": {}, \"copy\": {}, \"PCA\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1], \"whiten\": [false, true]}, \"logR\": {\"penalty\": [\"l1\", \"l2\"], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"tol\": [0.0001, 0.001, 0.01]}, \"kMeans\": {}, \"kBest\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, \"vote\": {}}");
    }

    @Override
    public int getCoreCount() {
        return 16;
    }

    @Override
    public Object submit(List<AB<Indiv, JSONObject>> indivs) {

        JSONArray jsonIndivs = new JSONArray();

        for (AB<Indiv,JSONObject> indivData : indivs) {

            JSONObject indivJson = indivData._2();
            indivJson.put("id",nextId);
            id2indivData.put(nextId, indivData);

            Indiv indiv = indivData._1();
            Object indivValue = indiv.computeValue();
            JSONObject jsonCode = new JSONObject(((TypedDag) indivValue).toJson());

            JSONObject indivDataToSubmit = F.obj(
                    "id",   nextId,
                    "code", jsonCode
            );

            nextId++;
            jsonIndivs.put(indivDataToSubmit);
        }

        return fakeInnerSubmit(jsonIndivs); // returns submitMsg
    }

    private Object fakeInnerSubmit(JSONArray jsonIndivs) {

        List<JSONObject> listIndivs = F.map(jsonIndivs, x->(JSONObject) x);

        frontaNaVyhodnoceni.addAll(listIndivs);

        return "FAKE SUBMIT SUCCESSFUL";
    }

    private JSONArray fakeGetEvaluated() {

        JSONObject submittedIndivData = frontaNaVyhodnoceni.poll();

        int id = submittedIndivData.getInt("id");
        JSONObject indivDagJson = submittedIndivData.getJSONObject("code");

        //double fakeScore = Math.random();
        double fakeScore = indivDagJson.keySet().size();

        JSONArray oneIndivResult = F.arr(id, F.arr(fakeScore));

        return F.arr(oneIndivResult);
    }

    @Override
    public EvalResult<Indiv> getEvaluated() {

        JSONArray json = fakeGetEvaluated();

        int len = json.length();
        List<AB<Indiv,JSONObject>> evaluatedIndividuals = new ArrayList<>(len);

        for (int i = 0; i < len; i++) {
            JSONArray evalRes = json.getJSONArray(i);
            evaluatedIndividuals.add(getIndivBack(evalRes));
        }

        return () -> evaluatedIndividuals;
    }

    private AB<Indiv,JSONObject> getIndivBack(JSONArray evalResJsonArr) {

        int id = evalResJsonArr.getInt(0);
        JSONArray scores = evalResJsonArr.getJSONArray(1);

        double score = (scores.length() > 0) ? scores.getDouble(0) : mkErrorScore();
        double myScore = Math.max(0.0, score);

        AB<Indiv,JSONObject> indivData = id2indivData.remove(id);
        if (indivData == null) {throw new Error("EvalResult for individual with non-existing id "+id+"!");}

        Indiv indiv = indivData._1();
        JSONObject indivJson = indivData._2();

        FitVal fitVal = new FitVal.WithId(myScore, false, id);

        indiv.setFitVal(fitVal);
        indivJson.put("fitness", myScore);
        indivJson.put("rawScores", scores);

        return indivData;
    }

    private static double mkErrorScore(){
        return - Double.MAX_VALUE;
    }


    @Override
    public String quitServer() {
        return "Timmmmmmmy!";
    }
}
