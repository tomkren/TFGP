package net.fishtron.apps.tomkraft;

import net.fishtron.eva.compare.IndivComparator;
import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.eval.EvalLib;
import net.fishtron.server.api.Api;
import net.fishtron.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * Created by tom on 09.07.2018.
 */
public class TkInteractiveComparator implements Api, IndivComparator<AppTreeMI> {

    private static final String KEY_id = "id";
    private static final String KEY_code = "code";
    //private static final String KEY_score = "score";

    private static final String JCMD_getEvalTask = "getEvalTask";
    private static final String JCMD_reportResult = "reportResult";

    // getEvalTask keys:
    private static final String KEY_evalTaskId = "evalTaskId";
    private static final String KEY_indivPack = "indivPack";
    private static final String KEY_isEmpty = "isEmpty";

    // processEvaluationReport keys:
    private static final String KEY_winnerId = "winnerId";
    private static final String KEY_loserIds = "loserIds";



    private final EvalLib lib;
    private final long sleepTime;
    private final boolean removeGivenTasksImmediately;
    private final Checker ch;

    private int nextIndivId;
    private int nextEvalTaskId;
    private Queue<EvalTask> inputQueue; // indivPacksToCompare
    private Queue<EvalReport> outputQueue; // comparedIndivPacks
    private Map<Integer, AppTreeMI> id2indiv;

    private Function<Object,Object> addJsonLambdas;


    private static final int numWaitRoundsToPrintMsg = 100;


    public TkInteractiveComparator(EvalLib lib, long sleepTime, boolean removeGivenTasksImmediately, Function<Object,Object> addJsonLambdas, Checker ch) {

        this.lib = lib;
        this.sleepTime = sleepTime;
        this.removeGivenTasksImmediately = removeGivenTasksImmediately;

        this.ch = ch;
        this.addJsonLambdas = addJsonLambdas;

        nextIndivId = 1;
        nextEvalTaskId = 1;
        inputQueue = new ConcurrentLinkedQueue<>();
        outputQueue = new ConcurrentLinkedQueue<>();
        id2indiv = new HashMap<>();

        /* TODO
        history = new CellplazaHistory(cellOpts.getNumStates());
        */
    }

    @Override
    public AppTreeMI compareFun(List<AppTreeMI> indivs) {

        EvalTask newEvalTask = createNewEvalTask(indivs);
        inputQueue.offer(newEvalTask);

        EvalReport result = outputQueue.poll();

        int waitRound = 0;
        while (result == null && !ch.isStopRequested()) {

            if (waitRound % numWaitRoundsToPrintMsg == 0) {
                int msgId = waitRound / numWaitRoundsToPrintMsg;
                ch.log("(" + msgId + ") Waiting for any user to compare an indiv pack...");
            }

            F.sleep(sleepTime);
            result = outputQueue.poll();

            waitRound++;
        }

        if (result == null) { // manual stop was requested
            return null; // TODO check that null does not break something
        }

        int resultEvalTaskId = result.evalTaskId;


        if (!removeGivenTasksImmediately) {
            boolean removeSuccessful = inputQueue.removeIf(et -> et.evalTaskId == resultEvalTaskId); // TODO analyse this approach : one indiv pack may be given to more then one user, this should be improved.
            // todo je to dlouhodobě debilní, uděláme něco jako pendingQueue, kde bude timeToLive ... - ale až to bude fachat

            if (!removeSuccessful) {
                throw new Error("<!removeSuccessful> We received illegal EvalReport data, better to crash now! [TODO!! better handling]");
                //return null; // todo better handling of incorect data!
            }
        }


        AppTreeMI winner = id2indiv.get(result.winnerId);

        if (winner == null) {
            throw new Error("<winner == null> We received illegal EvalReport data, better to crash now! [TODO!! better handling]");
            //return null; // todo better handling of incorect data!
        }

        return winner;
    }


    private EvalTask createNewEvalTask(List<AppTreeMI> indivs) {

        JSONArray indivPack = F.jsonMap(indivs, this::indivToJson);

        int newEvalTaskId = nextEvalTaskId;
        nextEvalTaskId ++;

        return new EvalTask(newEvalTaskId, indivPack);
    }

    private JSONObject indivToJson(AppTreeMI indiv) {

        int id = nextIndivId;
        nextIndivId++;

        indiv.setId(id);
        id2indiv.put(id, indiv);


        Object indivValue = indiv.computeValue(lib); // INDIVIDUAL EVALUATION

        return F.obj(
                KEY_id, id,
                KEY_code, addJsonLambdas.apply(indivValue)
        );
    }


    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        String jobCmd = query.optString(Api.KEY_jobCmd, null);
        switch (jobCmd) {
            case JCMD_getEvalTask: return api_getEvalTask();           // aka: CMD_GET_PAIR_TO_COMPARE
            case JCMD_reportResult: return api_reportResult(query);    // aka: CMD_OFFER_RESULT

            // TODO add history related requests

            default: return Api.error("Unsupported value for "+KEY_jobCmd+": '"+ jobCmd+"'");
        }
    }


    private JSONObject api_getEvalTask() {
        EvalTask evalTask = removeGivenTasksImmediately ? inputQueue.poll() : inputQueue.peek();
        JSONObject evalTaskJson = evalTask != null ? evalTask.toJson() : EvalTask.MkEmptyTaskJson();
        return Api.addOk(evalTaskJson);
    }


    private JSONObject api_reportResult(JSONObject query) {
        return parseEvaluationReport(query).ifOK(this::processEvaluationReport);
    }

    private JSONObject processEvaluationReport(EvalReport evalRes) {

        outputQueue.offer(evalRes);

        ch.log("\nclient -> evalResult: "+ evalRes.toString());
        return Api.ok(Api.KEY_msg, "Thanks for the report!");
    }


    private static Either<EvalReport,JSONObject> parseEvaluationReport(JSONObject reportQuery) {

        // Parse evalTaskId:
        int evalTaskId = reportQuery.optInt(KEY_evalTaskId, -1);
        if (evalTaskId == -1) {
            return Either.ko(Api.error("Wrong format or unspecified "+KEY_evalTaskId+", must be int."));
        }

        // Parse winnerId:
        int winnerId = reportQuery.optInt(KEY_winnerId, -1);
        if (winnerId == -1) {
            return Either.ko(Api.error("Wrong format or unspecified "+KEY_winnerId+", must be int."));
        }

        // Parse loserIds:
        JSONArray loserIdsArr = reportQuery.optJSONArray(KEY_loserIds);
        if (loserIdsArr == null) {
            return Either.ko(Api.error("Wrong format or unspecified "+KEY_loserIds+", must be JSONArray."));
        }

        int n = loserIdsArr.length();
        int[] loserIds = new int[n];
        for (int i = 0; i < n; i++) {
            int loserId = loserIdsArr.optInt(i, -1);
            if (loserId == -1) {
                return Either.ko(Api.error("Wrong format of value at "+KEY_loserIds+"["+i+"], must be int."));
            }
            loserIds[i] = loserId;
        }

        // Everything parsed ok:
        return Either.ok(new EvalReport(evalTaskId, winnerId, loserIds));
    }

    private static class EvalTask {

        private final int evalTaskId;
        private final JSONArray indivPack;

        EvalTask(int evalTaskId, JSONArray indivPack) {
            this.evalTaskId = evalTaskId;
            this.indivPack = indivPack;
        }

        public static JSONObject MkEmptyTaskJson() {
            return F.obj(KEY_isEmpty, true);
        }

        public JSONObject toJson() {
            return F.obj(
                    KEY_evalTaskId, evalTaskId,
                    KEY_indivPack, indivPack,
                    KEY_isEmpty, false
            );
        }
    }


    private static class EvalReport {
        int evalTaskId;
        int winnerId;
        int[] loserIds;

        EvalReport(int evalTaskId, int winnerId, int[] loserIds) {
            this.evalTaskId = evalTaskId;
            this.winnerId = winnerId;
            this.loserIds = loserIds;
        }

        @Override
        public String toString() {
            return "EvalReport{" +
                    "evalTaskId=" + evalTaskId +
                    ", winnerId=" + winnerId +
                    ", loserIds=" + Arrays.toString(loserIds) +
                    '}';
        }
    }


}
