package cz.tomkren.fishtron.ugen.apps.gpml;

import cz.tomkren.fishtron.mains.DagEvaTester;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import net.fishtron.utils.F;
import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.jetty.util.ArrayQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

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
        return DagEvaTester.testParamsInfo;
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
            JSONObject jsonCode = new JSONObject(((TypedDag) indivValue).toJson());

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
