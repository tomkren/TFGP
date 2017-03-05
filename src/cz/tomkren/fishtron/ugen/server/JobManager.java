package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Created by Tomáš Křen on 19.2.2017.*/

class JobManager {

    private final Map<Integer, EvaJobProcess> jobs;
    private int nextJobId;


    JobManager() {
        jobs = new HashMap<>();
        nextJobId = 1;
    }

    void start() {
        //todo
    }

    void stop() {
        //todo
    }

    private static final String JOB_NAME_KEY = "job";
    private static final String JOBS_KEY = "jobs";
    private static final String JOB_ID_KEY = "jobId";

    JSONObject getJobsInfo() {
         JSONArray jobsArr = F.jsonMap(jobs.keySet(), this::getJobInfo);
         return ApiManager.addOk(F.obj(JOBS_KEY, jobsArr));
    }

    private JSONObject getJobInfo(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return ApiManager.mkErrorResponse("There is no job with jobId "+jobId);
        }
        return ApiManager.addOk(jobProcess.toJson());
    }

    private JSONObject getJobLog(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return ApiManager.mkErrorResponse("There is no job with jobId "+jobId);
        }
        return ApiManager.addOk(F.obj("log", jobProcess.getLog()));
    }

    private JSONObject makeJob(JSONObject jobOpts) {

        if (!jobOpts.has(JOB_NAME_KEY)) {
            return ApiManager.mkErrorResponse("Missing key: "+JOB_NAME_KEY);
        }

        Object jobNameObj = jobOpts.get(JOB_NAME_KEY);
        if (!(jobNameObj instanceof String)) {
            return ApiManager.mkErrorResponse(JOB_NAME_KEY+" must be a String.");
        }

        String jobName = (String) jobNameObj;
        EvaJob job = JobFactory.mkJob(jobName);

        if (job == null) {
            return ApiManager.mkErrorResponse("Unknown job: "+jobOpts.getString("job"));
        }

        int jobId = nextJobId;
        nextJobId ++;

        EvaJobProcess newJob = new EvaJobProcess(jobId, job, jobOpts);


        Log.it("New job ("+JOB_ID_KEY+"="+jobId+") successfully made # and starting ...");

        jobs.put(jobId, newJob);
        newJob.start();

        return ApiManager.addOk(F.obj(JOB_ID_KEY,jobId));
    }


    JSONObject makeJob(JSONArray path, JSONObject query) {
        if (path.length() > 1) {
            query.put(JOB_NAME_KEY, path.get(1));
        }
        return makeJob(query);
    }

    JSONObject getJobInfo(JSONArray path, JSONObject query) {
        return getInfo(this::getJobInfo, path, query);
    }

    JSONObject getJobLog(JSONArray path, JSONObject query) {
        return getInfo(this::getJobLog, path, query);
    }



    private JSONObject getInfo(Function<Integer,JSONObject> infoFun, JSONArray path, JSONObject query) {
        if (path.length() > 1) {
            String idStr = path.getString(1);
            try {
                int idInt = Integer.parseInt(idStr);
                query.put(JOB_ID_KEY, idInt);
            } catch (NumberFormatException e) {
                return ApiManager.mkErrorResponse(JOB_ID_KEY+" in path must be an integer.");
            }
        }

        if (!query.has(JOB_ID_KEY)) {
            return ApiManager.mkErrorResponse("Missing key: "+JOB_ID_KEY);
        }

        Object jobIdObj = query.get(JOB_ID_KEY);
        if (!(jobIdObj instanceof Integer)) {
            return ApiManager.mkErrorResponse(JOB_ID_KEY+" must be an integer.");
        }

        int jobId = (int) jobIdObj;
        return infoFun.apply(jobId);
    }



}
