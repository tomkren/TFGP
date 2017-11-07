package net.fishtron.eva.multi.evaluators;

import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.eva.multi.MultiEvalResult;
import net.fishtron.eval.EvalLib;
import net.fishtron.server.api.Api;
import net.fishtron.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tom on 27.10.2017.
 */
public abstract class ServerEvalManager extends GeneralEvalManager implements Api {

    private static final String KEY_isEmpty = "isEmpty";

    private static final String JCMD_getEvalTask = "getEvalTask";
    private static final String JCMD_reportResult = "reportResult";

    private static final String KEY_nextId = "nextId";


    private final Queue<JSONObject> submittedIndivData;
    private final int preferredBufferSize;
    private final Queue<JSONObject> evaluatedResults;

    private final Checker checker;

    public ServerEvalManager(EvalLib evalLib, Checker checker, int preferredBufferSize) {
        super(evalLib);
        this.checker = checker;
        this.preferredBufferSize = preferredBufferSize;

        submittedIndivData = new ConcurrentLinkedQueue<>();
        evaluatedResults  = new ConcurrentLinkedQueue<>();
    }


    private int howManyToRequest() {
        return Math.max(preferredBufferSize - submittedIndivData.size(), 0);
    }

    @Override
    protected void offerOneIndivData(JSONObject indivDataToSubmit) {
        submittedIndivData.offer(indivDataToSubmit);
    }

    @Override
    protected List<JSONObject> pollEvalResults() {
        List<JSONObject> evalResults = new ArrayList<>();

        while (true) {
            JSONObject evalResult = evaluatedResults.poll();
            if (evalResult == null) {
                break;
            } else {
                evalResults.add(evalResult);
            }
        }

        return evalResults;
    }

    @Override
    protected MultiEvalResult<AppTreeMI> mkMultiEvalResult(List<AB<AppTreeMI, JSONObject>> evaluatedIndividuals) {
        int numRequestedIndividuals = howManyToRequest();
        return new ServerEvalResult(evaluatedIndividuals, numRequestedIndividuals);
    }


    public static class ServerEvalResult implements MultiEvalResult<AppTreeMI> {

        private final List<AB<AppTreeMI, JSONObject>> evaluatedIndividuals;
        private final int numRequestedIndividuals;

        ServerEvalResult(List<AB<AppTreeMI, JSONObject>> evaluatedIndividuals, int numRequestedIndividuals) {
            this.evaluatedIndividuals = evaluatedIndividuals;
            this.numRequestedIndividuals = numRequestedIndividuals;
        }

        @Override public List<AB<AppTreeMI, JSONObject>> getEvalResult() { return evaluatedIndividuals; }
        @Override public int getNumRequestedIndividualsByEvaluator() { return numRequestedIndividuals; }
    }



    private JSONObject api_getEvalTask() {

        JSONObject indivDataToSubmit = submittedIndivData.poll();

        // TODO | Here we remove the individual, but what to do if no one evaluates it? It needs to be resolved correctly.

        if (indivDataToSubmit == null) {
            return Api.ok(KEY_isEmpty, true);
        }

        indivDataToSubmit.put(KEY_isEmpty, false);

        return Api.addOk(indivDataToSubmit);
    }

    protected abstract Either<AB<Integer,List<Double>>,JSONObject> reportQueryToEvalRes(JSONObject reportQuery);

    private JSONObject api_reportResult(JSONObject query) {
        return reportQueryToEvalRes(query).ifOK(this::reportResult);
    }

    private JSONObject reportResult(AB<Integer,List<Double>> evalRes) {
        JSONObject evalResult = F.obj(
                KEY_id, evalRes._1(),
                KEY_scores, F.jsonMap(evalRes._2())
        );

        evaluatedResults.offer(evalResult);

        checker.log("\nclient -> evalResult: "+ evalResult.toString());
        return Api.ok(Api.KEY_msg, "Thanks for the report!");
    }


    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        String jobCmd = query.optString(Api.KEY_jobCmd, null);
        switch (jobCmd) {
            case JCMD_getEvalTask: return api_getEvalTask();
            case JCMD_reportResult: return api_reportResult(query);
            default: return Api.error("Unsupported "+KEY_jobCmd+": "+ jobCmd);
        }
    }

    @Override
    public JSONObject getInfo() {
        return F.obj(
                KEY_nextId, getNextId()
        );
    }

}
