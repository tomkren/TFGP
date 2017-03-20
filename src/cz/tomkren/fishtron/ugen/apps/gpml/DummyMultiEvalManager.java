package cz.tomkren.fishtron.ugen.apps.gpml;

import cz.tomkren.fishtron.mains.DagEvaTester;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**Created by tom on 17.03.2017.*/

public class DummyMultiEvalManager<Indiv extends MultiIndiv> implements XmlRpcServer_MultiEvalManager<Indiv> {

    private EvalLib lib;
    private Queue<JSONObject> fakeEvalQueue;
    private Map<Integer, AB<Indiv,JSONObject>> id2indivData;
    private int nextId;

    DummyMultiEvalManager(EvalLib lib) {
        this.lib = lib;
        fakeEvalQueue = new ArrayQueue<>();
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

            id2indivData.put(nextId, indivData);

            JSONObject indivJson = indivData._2();
            indivJson.put("id", nextId);

            Indiv indiv = indivData._1();
            Object indivValue = indiv.computeValue(lib);
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
        fakeEvalQueue.addAll(listIndivs);
        return "FAKE SUBMIT SUCCESSFUL";
    }

    private JSONArray fakeGetEvaluated() {

        JSONObject submittedIndivData = fakeEvalQueue.poll();

        int id = submittedIndivData.getInt("id");
        JSONObject indivDagJson = submittedIndivData.getJSONObject("code");

        double fakeScore1 = indivDagJson.keySet().size();
        double fakeStdDevScore = Math.random();
        double fakeScore2 = Math.random();

        JSONArray oneIndivResult = F.arr(id, F.arr(fakeScore1,fakeStdDevScore,fakeScore2));
        return F.arr(oneIndivResult);
    }

    @Override
    public MultiEvalResult<Indiv> getEvaluated() {
        JSONArray json = fakeGetEvaluated();

        List<AB<Indiv,JSONObject>> evaluatedIndividuals = new ArrayList<>(json.length());

        for (int i = 0; i < json.length(); i++) {
            JSONArray evalRes = json.getJSONArray(i);
            evaluatedIndividuals.add(getIndivBack(evalRes));
        }

        return () -> evaluatedIndividuals;
    }

    private AB<Indiv,JSONObject> getIndivBack(JSONArray evalResJsonArr) {

        int       id     = evalResJsonArr.getInt(0);
        JSONArray scores = evalResJsonArr.getJSONArray(1);

        double performanceScore = (scores.length() > 0) ? scores.getDouble(0) : mkErrorPerformanceScore();
        double timeScore        = (scores.length() > 2) ? scores.getDouble(2) : mkErrorTimeScore();


        AB<Indiv,JSONObject> indivData = id2indivData.remove(id);
        if (indivData == null) {throw new Error("EvalResult for individual with non-existing id "+id+"!");}

        Indiv indiv = indivData._1();
        JSONObject indivJson = indivData._2();


        List<Double> fitness = Arrays.asList(performanceScore, timeScore);
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
