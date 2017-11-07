package net.fishtron.eva.multi.evaluators;

import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.eva.multi.MultiEvalManager;
import net.fishtron.eva.multi.MultiEvalResult;
import net.fishtron.eval.EvalLib;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by tom on 27.10.2017.
 */
public abstract class GeneralEvalManager implements MultiEvalManager<AppTreeMI> {

    static final String KEY_id = "id";
    static final String KEY_scores = "scores";


    private final EvalLib evalLib;

    private Map<Integer, AB<AppTreeMI,JSONObject>> id2indivData;
    private int nextId;

    GeneralEvalManager(EvalLib evalLib) {
        this.evalLib = evalLib;

        id2indivData = new HashMap<>();
        nextId = 1;
    }

    @Override
    public MultiEvalResult<AppTreeMI> evalIndividuals(List<AB<AppTreeMI, JSONObject>> indivs) {
        submit(indivs);
        return getEvaluated();
    }

    @Override
    public MultiEvalResult<AppTreeMI> justAskForResults() {
        return getEvaluated();
    }

    private void submit(List<AB<AppTreeMI, JSONObject>> indivs) {
        for (AB<AppTreeMI,JSONObject> indivData : indivs) {
            AppTreeMI  indiv     = indivData._1();
            JSONObject indivJson = indivData._2();

            int id = nextId;
            nextId ++;

            indiv.setId(id);
            indivJson.put(KEY_id, id);
            id2indivData.put(id, indivData);

            Object indivValue = indiv.computeValue(evalLib); // INDIVIDUAL EVALUATION
            JSONObject indivDataToSubmit = mkIndivDataToSubmit(indivValue, id);
            offerOneIndivData(indivDataToSubmit);
        }
    }

    // TODO ... maybe synchronized
    int getNextId() {
        return nextId;
    }

    protected abstract JSONObject mkIndivDataToSubmit(Object indivValue, int id);
    protected abstract void offerOneIndivData(JSONObject indivDataToSubmit);
    protected abstract List<JSONObject> pollEvalResults();
    protected abstract MultiEvalResult<AppTreeMI> mkMultiEvalResult(List<AB<AppTreeMI,JSONObject>> evaluatedIndividuals);

    private MultiEvalResult<AppTreeMI> getEvaluated() {

        List<JSONObject> evalResults = pollEvalResults();
        List<AB<AppTreeMI,JSONObject>> evaluatedIndividuals = F.map(evalResults, this::getIndivBack);


        /*
        List<AB<AppTreeMI,JSONObject>> evaluatedIndividuals = new ArrayList<>();
        for (JSONObject evalResult : evalResults) {
            if (evalResult != null) {
                evaluatedIndividuals.add(getIndivBack(evalResult));
            }
        }
        */

        return mkMultiEvalResult(evaluatedIndividuals);
    }

    private AB<AppTreeMI,JSONObject> getIndivBack(JSONObject evalResult) {

        int       id     = evalResult.getInt(KEY_id);
        JSONArray scores = evalResult.getJSONArray(KEY_scores);

        AB<AppTreeMI,JSONObject> indivData = id2indivData.remove(id);
        if (indivData == null) {
            // TODO should handle gracefully !!!!!!!!!!!!!!!!!!
            throw new Error("evalResult for individual with non-existing id "+id+".");
        }

        AppTreeMI indiv = indivData._1();
        JSONObject indivJson = indivData._2();

        List<Double> fitness = F.map(scores, x->(Double)x); // TODO check!

        indiv.setFitness(fitness);
        indivJson.put("fitness", F.jsonMap(fitness));
        //indivJson.put("rawScores", scores);

        return indivData;
    }

}
