package net.fishtron.apps.workflows;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.fishtron.mains.DagEvaTester;
import cz.tomkren.fishtron.sandbox2.Dag_IEvalManager;
import cz.tomkren.fishtron.sandbox2.EvalResult;
import cz.tomkren.fishtron.workflows.TypedDag;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

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
        return DagEvaTester.testParamsInfo;
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
