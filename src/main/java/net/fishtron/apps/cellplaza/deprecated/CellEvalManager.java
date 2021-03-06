package net.fishtron.apps.cellplaza.deprecated;

import net.fishtron.apps.cellplaza.v2.CellOpts;
import net.fishtron.apps.cellplaza.v2.Libs;
import net.fishtron.apps.cellplaza.v2.Rule;
import net.fishtron.eval.EvalLib;
import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.eva.multi.MultiEvalManager;
import net.fishtron.eva.multi.MultiEvalResult;
import net.fishtron.server.OLD.Api_OLD;
import net.fishtron.server.OLD.EvaJobProcess;
import net.fishtron.server.OLD.JobManager;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.TODO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 20.03.2017. */

class CellEvalManager implements MultiEvalManager<AppTreeMI>, Api_OLD {

    private final EvalLib lib;
    private final Checker ch;
    private final Api_OLD interactiveEvalApi;

    private Map<Integer, AB<AppTreeMI,JSONObject>> id2indivData;
    private int nextId;

    private final CellOpts cellOpts;
    private final int numFrames;
    private final String runDirPath;

    CellEvalManager(EvalLib lib, CellOpts cellOpts, int numFrames, String runDirPath, Checker ch, EvaJobProcess jobProcess) {
        this.lib = lib;
        this.ch = ch;

        this.cellOpts = cellOpts;
        this.numFrames = numFrames;
        this.runDirPath = runDirPath;

        id2indivData = new HashMap<>();
        nextId = 0;

        JobManager jobMan = jobProcess.getJobManager();

        JSONObject response = jobMan.runJob(F.obj(
                Api_OLD.CMD, Api_OLD.CMD_RUN,
                Api_OLD.JOB_NAME, InteractiveEvaluatorJob.JOB_NAME
        ));

        if (!response.getString(Api_OLD.STATUS).equals(Api_OLD.OK)) {
            throw new Error("Unable to create "+InteractiveEvaluatorJob.JOB_NAME+" job, response: "+response);
        }

        int jobId = response.getInt(Api_OLD.JOB_ID);

        ch.log("\n>>> Created "+ InteractiveEvaluatorJob.JOB_NAME+" job: "+ response+", jobId: "+jobId+"\n");

        interactiveEvalApi = jobMan.getJobApi(jobId);
    }

    @Override
    public MultiEvalResult<AppTreeMI> evalIndividuals(List<AB<AppTreeMI, JSONObject>> indivs) {

        JSONArray jsonIndivs = new JSONArray();

        for (AB<AppTreeMI,JSONObject> indivData : indivs) {
            AppTreeMI  indiv     = indivData._1();
            JSONObject indivJson = indivData._2();

            indivJson.put("id",nextId);
            id2indivData.put(nextId, indivData);

            Object indivValue = indiv.computeValue(lib); // INDIVIDUAL EVALUATION

            JSONObject indivDataToSubmit = F.obj(
                    "id",    nextId,
                    "value", indivValueToJson(indivValue, nextId),
                    "tree", indiv.getTree()
            );

            nextId++;
            jsonIndivs.put(indivDataToSubmit);
        }

        interactiveEvalApi.processApiCall_OLD(null, F.obj(
                Api_OLD.JOB_CMD, InteractiveEvaluatorJob.CMD_ADD_TO_POOL,
                InteractiveEvaluatorJob.INDIVS, jsonIndivs
        ));

        int tries = 1;
        while (true) {
            ch.log("("+tries+") Waiting for user evaluations... ");
            F.sleep(2000);
            tries++;
        }

    }

    private Object indivValueToJson(Object indivValue, int indivId) {
        AB<String,Rule> p = (AB<String,Rule>)indivValue;
        String seedFilename = p._1();
        Rule rule = p._2();

        List<String> frames = Libs.genPhenotype(cellOpts, seedFilename, rule, numFrames, runDirPath, indivId, ch);

        return F.obj(
                "pair", F.arr(seedFilename,rule.toString()),
                "frames", F.jsonMap(frames)
        );
    }

    @Override
    public MultiEvalResult<AppTreeMI> justAskForResults() {
        throw new TODO();
    }

    @Override
    public int getEvalPoolSize(int suggestedPoolSize) {
        return suggestedPoolSize;
    }

    @Override
    public JSONObject processApiCall_OLD(JSONArray path, JSONObject query) {
        return interactiveEvalApi.processApiCall_OLD(path, query);
    }





}
