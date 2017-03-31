package cz.tomkren.fishtron.ugen.apps.gpml;

import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.ABC;
import cz.tomkren.utils.F;
import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**Created by tom on 17.03.2017.*/

public class DummyHistoryEvalManager<Indiv extends MultiIndiv> implements XmlRpcServer_MultiEvalManager<Indiv> {

    private EvalLib lib;
    //private Queue<JSONObject> fakeEvalQueue;
    private Map<Integer, ABC<Indiv,JSONObject,Long>> id2indivData;
    private int nextId;

    private Queue<JSONArray> history;

    DummyHistoryEvalManager(EvalLib lib, String historyJsonPath) {
        this.lib = lib;
        //fakeEvalQueue = new ArrayQueue<>();
        id2indivData = new HashMap<>();
        nextId = 0;

        history = new ArrayQueue<>();
        List<JSONArray> historyList = F.map(F.loadJsonArray(historyJsonPath), x->(JSONArray) x);
        for (JSONArray historyItem : historyList) {
            history.offer(historyItem);
        }
    }

    @Override
    public JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException {
        //return DagEvaTester.testParamsInfo;
        return new JSONObject("{\"SVC\":{\"tol\":[1.0E-4,0.001,0.01],\"C\":[0.1,0.5,1,2,5,10,15],\"gamma\":[\"auto\",1.0E-4,0.001,0.01,0.1,0.5]},\"kMeans\":{},\"LDA\":{\"shrinkage\":[null,\"auto\",0.1,0.5,1],\"solver\":[\"lsqr\",\"eigen\"]},\"union\":{},\"kBest\":{\"feat_frac\":[0.01,0.05,0.1,0.25,0.5,0.75,1]},\"gaussianNB\":{},\"PCA\":{\"whiten\":[false,true],\"feat_frac\":[0.01,0.05,0.1,0.25,0.5,0.75,1]},\"QDA\":{\"tol\":[1.0E-4,0.001,0.01],\"reg_param\":[0,0.1,0.5,1]},\"MLP\":{\"tol\":[1.0E-4,0.001,0.01],\"alpha\":[1.0E-4,0.001,0.01],\"power_t\":[0.1,0.5,1,2],\"learning_rate_init\":[1.0E-4,0.001,0.01],\"activation\":[\"identity\",\"logistic\",\"relu\"],\"learning_rate\":[\"constant\",\"invscaling\",\"adaptive\"],\"max_iter\":[10,100,200],\"hidden_layer_sizes\":[[100],[50],[20],[10]],\"solver\":[\"lbfgs\",\"sgd\",\"adam\"],\"momentum\":[0.1,0.5,0.9]},\"DT\":{\"max_features\":[0.05,0.1,0.25,0.5,0.75,1],\"criterion\":[\"gini\",\"entropy\"],\"min_samples_split\":[2,5,10,20],\"max_depth\":[1,2,5,10,15,25,50,100],\"min_samples_leaf\":[1,2,5,10,20]},\"SGD\":{\"epsilon\":[0.01,0.05,0.1,0.5],\"loss\":[\"hinge\",\"log\",\"modified_huber\",\"squared_hinge\",\"perceptron\"],\"eta0\":[0.01,0.1,0.5],\"l1_ratio\":[0,0.15,0.5,1],\"penalty\":[\"none\",\"l2\",\"l1\",\"elasticnet\"],\"alpha\":[1.0E-4,0.001,0.01],\"n_iter\":[5,10,100],\"power_t\":[0.1,0.5,1,2],\"learning_rate\":[\"constant\",\"optimal\"]},\"Perceptron\":{\"penalty\":[\"None\",\"l2\",\"l1\",\"elasticnet\"],\"alpha\":[1.0E-4,0.001,0.01],\"n_iter\":[1,2,5,10,100]},\"PAC\":{\"loss\":[\"hinge\",\"squared_hinge\"],\"C\":[0.1,0.5,1,2,5,10,15]},\"copy\":{},\"logR\":{\"tol\":[1.0E-4,0.001,0.01],\"C\":[0.1,0.5,1,2,5,10,15],\"penalty\":[\"l2\"],\"solver\":[\"sag\"]},\"vote\":{}}");
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
            indivJson.put("id", nextId);

            long startTime = System.currentTimeMillis();
            ABC<Indiv,JSONObject,Long> indivDataWithTime = ABC.mk(indivData._1(),indivData._2(), startTime);

            id2indivData.put(nextId, indivDataWithTime);

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
        //List<JSONObject> listIndivs = F.map(jsonIndivs, x->(JSONObject) x);
        //fakeEvalQueue.addAll(listIndivs);
        return "FAKE SUBMIT SUCCESSFUL";
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



    private JSONArray fakeGetEvaluated() {

        //JSONObject submittedIndivData = fakeEvalQueue.poll();


        JSONArray historyItem = history.peek();

        int id = historyItem.getInt(0);
        JSONArray scores = historyItem.getJSONArray(1);
        double timeScore = scores.length() > 2 ? scores.getDouble(2) : 0;


        ABC<Indiv,JSONObject,Long> indivDataWithTime = id2indivData.get(id);

        if (indivDataWithTime == null) {
            return F.arr();
        }

        long startTime = indivDataWithTime._3();
        double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;

        /*if (elapsedSeconds < timeScore) {
            return F.arr();
        }*/

        return F.arr(history.poll());
    }



    private AB<Indiv,JSONObject> getIndivBack(JSONArray evalResJsonArr) {

        int       id     = evalResJsonArr.getInt(0);
        JSONArray scores = evalResJsonArr.getJSONArray(1);

        double performanceScore = (scores.length() > 0) ? scores.getDouble(0) : mkErrorPerformanceScore();
        double timeScore        = (scores.length() > 2) ? scores.getDouble(2) : mkErrorTimeScore();


        ABC<Indiv,JSONObject,Long> indivData = id2indivData.remove(id);
        if (indivData == null) {throw new Error("EvalResult for individual with non-existing id "+id+".");}

        Indiv indiv = indivData._1();
        JSONObject indivJson = indivData._2();


        List<Double> fitness = Arrays.asList(performanceScore, timeScore);
        indiv.setFitness(fitness);

        indivJson.put("rawScores", scores);
        indivJson.put("fitness", F.jsonMap(fitness));

        return AB.mk(indivData._1(), indivData._2());
    }

    private static double mkErrorPerformanceScore(){return -Double.MAX_VALUE;}
    private static double mkErrorTimeScore(){return Double.MAX_VALUE;}



    @Override
    public String quitServer() {
        return "Timmmmmmmmmmmy!";
    }
}
