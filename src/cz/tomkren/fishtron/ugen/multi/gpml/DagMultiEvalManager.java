package cz.tomkren.fishtron.ugen.multi.gpml;

import cz.tomkren.fishtron.sandbox2.Dag_JsonEvalInterface;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**Created by tom on 07.03.2017.*/

public class DagMultiEvalManager<Indiv extends MultiIndiv> implements XmlRpcServer_MultiEvalManager<Indiv> {

    private Dag_JsonEvalInterface dagEvaluator;

    private String getParamSetsMethodName;
    private String getCoreCountMethodName;
    private String submitMethodName;
    private String getEvaluatedMethodName;

    private String datasetFilename;

    private Map<Integer, AB<Indiv,JSONObject>> id2indivData;
    private int nextId;

    DagMultiEvalManager(String getParamSetsMethodName, String getCoreCountMethodName, String submitMethodName,
                               String getEvaluatedMethodName, String evaluatorURL, String datasetFilename) {

        this.getParamSetsMethodName = getParamSetsMethodName;
        this.getCoreCountMethodName = getCoreCountMethodName;
        this.submitMethodName = submitMethodName;
        this.getEvaluatedMethodName = getEvaluatedMethodName;
        this.datasetFilename = datasetFilename;

        dagEvaluator = new Dag_JsonEvalInterface(evaluatorURL);

        id2indivData = new HashMap<>();
        nextId = 0;
    }

    @Override
    public JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException {
        String jsonStr = dagEvaluator.getMethodParams(getParamSetsMethodName, datasetFilename);
        return new JSONObject(jsonStr);
    }

    @Override
    public int getCoreCount() {
        return dagEvaluator.getInt(getCoreCountMethodName);
    }

    @Override
    public Object submit(List<AB<Indiv, JSONObject>> indivs) {

        JSONArray jsonIndivs = new JSONArray();

        for (AB<Indiv,JSONObject> indivData : indivs) {
            Indiv      indiv     = indivData._1();
            JSONObject indivJson = indivData._2();

            indivJson.put("id",nextId);
            id2indivData.put(nextId, indivData);

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

    private JSONObject dagToJson(Object indivValue) {
        TypedDag dag = (TypedDag) indivValue;
        String jsonStr = dag.toJson(); // todo prasarna.. že toJson vrací string
        return new JSONObject(jsonStr);
    }

    @Override
    public MultiEvalResult<Indiv> getEvaluated() {

        JSONArray json = dagEvaluator.getEvaluated(getEvaluatedMethodName);

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

        indivJson.put("fitness", F.jsonMap(fitness));
        indivJson.put("rawScores", scores);

        return indivData;
    }

    private static double mkErrorPerformanceScore(){
        return -Double.MAX_VALUE;
    }

    private static double mkErrorTimeScore(){
        return Double.MAX_VALUE;
    }


    @Override
    public String quitServer() {
        return dagEvaluator.quitServer();
    }
}
