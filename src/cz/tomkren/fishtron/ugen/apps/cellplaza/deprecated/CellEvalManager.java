package cz.tomkren.fishtron.ugen.apps.cellplaza.deprecated;

import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.CellOpts;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Libs;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Rule;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.multi.MultiEvalManager;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import net.fishtron.server.Api;
import net.fishtron.server.EvaJobProcess;
import net.fishtron.server.JobManager;
import cz.tomkren.utils.*;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 20.03.2017. */

class CellEvalManager implements MultiEvalManager<AppTreeMI>, Api {

    private final EvalLib lib;
    private final Checker ch;
    private final Api interactiveEvalApi;

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
                Api.CMD, Api.CMD_RUN,
                Api.JOB_NAME, InteractiveEvaluatorJob.JOB_NAME
        ));

        if (!response.getString(Api.STATUS).equals(Api.OK)) {
            throw new Error("Unable to create "+InteractiveEvaluatorJob.JOB_NAME+" job, response: "+response);
        }

        int jobId = response.getInt(Api.JOB_ID);

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

        interactiveEvalApi.processApiCall(null, F.obj(
                Api.JOB_CMD, InteractiveEvaluatorJob.CMD_ADD_TO_POOL,
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
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        return interactiveEvalApi.processApiCall(path, query);
    }





}
