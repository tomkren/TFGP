package net.todo.apps.workflows;

import net.fishtron.apps.gpml.Dag_JsonEvalInterface;
import net.fishtron.eva.simple.EvalResult;
import net.fishtron.eva.simple.FitIndiv;
import net.fishtron.eva.simple.FitVal;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.tomkren.fishtron.workflows.TypedDag;


/** Created by user on 10. 6. 2016.*/

public class Dag_EvalManager<Indiv extends FitIndiv> implements Dag_IEvalManager<Indiv> {

    private Dag_JsonEvalInterface dagEvaluator;

    private String getParamSetsMethodName;
    private String submitMethodName;
    private String getEvaluatedMethodName;
    private String getCoreCountMethodName;

    private String datasetFilename;

    //private Function<Object,Object> toJsonObject;

    private Map<Integer, AB<Indiv,JSONObject>> id2indivData;
    private int nextId;

    public Dag_EvalManager(String getParamSetsMethodName, String getCoreCountMethodName, String submitMethodName,
                           String getEvaluatedMethodName, String evaluatorURL, String datasetFilename) {

        this.getParamSetsMethodName = getParamSetsMethodName;
        this.getCoreCountMethodName = getCoreCountMethodName;
        this.submitMethodName = submitMethodName;
        this.getEvaluatedMethodName = getEvaluatedMethodName;
        this.datasetFilename = datasetFilename;

        //this.toJsonObject = toJsonObject;
        dagEvaluator = new Dag_JsonEvalInterface(evaluatorURL);

        id2indivData = new HashMap<>();
        nextId = 0;
    }

    public JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException {
        String jsonStr = dagEvaluator.getMethodParams(getParamSetsMethodName, datasetFilename);
        JSONObject allParamsInfo = new JSONObject(jsonStr);
        Log.itln("allParamsInfo = "+ allParamsInfo);
        return allParamsInfo;
    }

    public String quitServer() {
        return dagEvaluator.quitServer();
    }

    @Override
    public int getCoreCount() {
        return dagEvaluator.getInt(getCoreCountMethodName);
    }

    private JSONObject dagToJson(Object indivValue) {

        TypedDag dag = (TypedDag) indivValue;
        String jsonStr = dag.toJson(); // todo prasarna..

        return new JSONObject(jsonStr);
    }

    @Override
    public Object submit(List<AB<Indiv,JSONObject>> indivs) {

        JSONArray jsonIndivs = new JSONArray();

        for (AB<Indiv,JSONObject> indivData : indivs) {

            Indiv      indiv     = indivData._1();
            JSONObject indivJson = indivData._2();

            id2indivData.put(nextId, indivData);
            indivJson.put("id",nextId);

            Object indivValue = indiv.computeValue();
            JSONObject jsonCode = dagToJson(indivValue);

            JSONObject indivDataToSubmit = F.obj(
                "id",   nextId,
                "code", jsonCode
            );

            nextId++;
            jsonIndivs.put(indivDataToSubmit);
        }

        return dagEvaluator.submit(submitMethodName, jsonIndivs, datasetFilename); // returns submitMsg
    }

    @Override
    public EvalResult<Indiv> getEvaluated() {
        JSONArray json = dagEvaluator.getEvaluated(getEvaluatedMethodName);

        int len = json.length();
        List<AB<Indiv,JSONObject>> evaluatedIndividuals = new ArrayList<>(len);

        for (int i = 0; i < len; i++) {
            JSONArray evalRes = json.getJSONArray(i);
            evaluatedIndividuals.add(getIndivBack(evalRes));
        }

        return () -> evaluatedIndividuals;
    }

    private AB<Indiv,JSONObject> getIndivBack(JSONArray evalResJsonArr) {

        int       id     = evalResJsonArr.getInt(0);
        JSONArray scores = evalResJsonArr.getJSONArray(1);

        double score = (scores.length() > 0) ? scores.getDouble(0) : mkErrorScore();

        // todo udělat aby umělo i záporný skóre !!! !!! !!!
        double myScore = Math.max(0.0, score);

        AB<Indiv,JSONObject> indivData = id2indivData.remove(id);
        if (indivData == null) {throw new Error("EvalResult for individual with non-existing id "+id+"!");}

        Indiv indiv = indivData._1();
        JSONObject indivJson = indivData._2();

        FitVal fitVal = new FitVal.WithId(myScore, isPerfect(myScore), id);

        indiv.setFitVal(fitVal);
        indivJson.put("fitness", myScore);
        indivJson.put("rawScores", scores);

        return indivData;
    }

    /* TODO score == perfectScore*/
    private static boolean isPerfect(double score) {
        return false;
    }


    private static double mkErrorScore(){
        return - Double.MAX_VALUE;
    }


}
