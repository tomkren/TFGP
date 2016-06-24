package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by user on 10. 6. 2016.*/

public class Dag_EvalManager<Indiv extends FitIndiv> implements Dag_IEvalManager<Indiv> {

    private Dag_JsonEvalInterface dagEvaluator;

    private String getParamSetsMethodName;
    private String submitMethodName;
    private String getEvaluatedMethodName;
    private String getCoreCountMethodName;

    //private Function<Object,Object> toJsonObject;

    private Map<Integer, Indiv> id2indiv;
    private int nextId;


    public Dag_EvalManager(String getParamSetsMethodName, String getCoreCountMethodName, String submitMethodName, String getEvaluatedMethodName, String evaluatorURL) {
        this.getParamSetsMethodName = getParamSetsMethodName;
        this.getCoreCountMethodName = getCoreCountMethodName;
        this.submitMethodName = submitMethodName;
        this.getEvaluatedMethodName = getEvaluatedMethodName;

        //this.toJsonObject = toJsonObject;
        dagEvaluator = new Dag_JsonEvalInterface(evaluatorURL);

        id2indiv = new HashMap<>();
        nextId = 1;
    }

    public JSONObject getAllParamsInfo(String datasetFilename) {
        String json = dagEvaluator.getMethodParams(getParamSetsMethodName, datasetFilename);
        Log.itln("allParamsInfo = "+ json);
        return new JSONObject(json);
    }

    @Override
    public int getCoreCount() {
        return dagEvaluator.getInt(getCoreCountMethodName);
    }

    private JSONArray dagToJson(Object indivValue) {

        TypedDag dag = (TypedDag) indivValue;
        String jsonStr = dag.toJson(); // todo prasarna..
        return new JSONArray(jsonStr);
    }

    @Override
    public Object submit(List<Indiv> indivs) {

        JSONArray jsonIndivs = new JSONArray();

        for (Indiv indiv : indivs) {

            id2indiv.put(nextId, indiv);

            Object indivValue = indiv.computeValue();
            Object jsonCode = dagToJson(indivValue);

            JSONObject indivData = F.obj(
                "id",   nextId,
                "code", jsonCode
            );

            nextId++;
            jsonIndivs.put(indivData);
        }

        //return () -> someEvaluatedIndivs;
        return dagEvaluator.submit(submitMethodName, jsonIndivs); // returns submitMsg
    }

    @Override
    public EvalResult<Indiv> getEvaluated() {
        List<Indiv> evaluatedIndividuals = dagEvaluator.getEvaluated(getEvaluatedMethodName, this::getIndivBack);
        return () -> evaluatedIndividuals;
    }

    private Indiv getIndivBack(Object evalRes) {
        Object[] evalResArr = (Object[]) evalRes;

        int    id       = (int)      evalResArr[0];
        double[] scores = (double[]) evalResArr[1];

        double score = (scores.length > 0) ? scores[0] : mkErrorScore();

        // todo udělat aby umělo i záporný skóre !!! !!! !!!
        score = Math.max(0.0, score);
        FitVal fitVal = new FitVal.Basic(score, isPerfect(score));

        Indiv indiv = id2indiv.remove(id);

        if (indiv == null) {throw new Error("EvalResult for individual with non-existing id "+id+"!");}

        indiv.setFitVal(fitVal);
        return indiv;
    }

    /* TODO score == perfectScore*/
    private static boolean isPerfect(double score) {
        return false;
    }

    private static double mkErrorScore(){
        return -1000;
    }


}
