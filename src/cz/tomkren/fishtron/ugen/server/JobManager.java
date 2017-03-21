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

    private final JobFactory jobFactory;
    private final Map<Integer, EvaJobProcess> jobs;
    private int nextJobId;


    JobManager() {
        jobFactory = new JobFactory();
        jobs = new HashMap<>();
        nextJobId = 1;
    }

    void start() {
        //todo
    }

    void stop() {
        //todo
    }

    void addJobClass(String jobName, Class<? extends EvaJob> jobClass) {
        jobFactory.addJobClass(jobName, jobClass);
    }


    JSONObject getJobsInfo() {
         JSONArray jobsArr = F.jsonMap(jobs.keySet(), this::getJobInfo);
         return Api.ok(
                 Api.JOBS, jobsArr,
                 Api.JOB_NAMES, jobFactory.getJobNames()
         );
    }

    private JSONObject getJobInfo(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return ApiManager.mkErrorResponse("There is no job with jobId "+jobId);
        }
        return Api.addOk(jobProcess.toJson());
    }

    private JSONObject getJobLog(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return ApiManager.mkErrorResponse("There is no job with jobId "+jobId);
        }
        String logStr = jobProcess.getLog();
        JSONArray logLines = F.jsonMap(logStr.split("\\n"));
        return Api.addOk(F.obj("log", logLines));
    }

    private Api getJobApi(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return null;
        }
        return jobProcess.getJobApi();
    }

    private JSONObject runJob(JSONObject jobOpts) {

        if (!jobOpts.has(Api.JOB_NAME)) {
            return ApiManager.mkErrorResponse("Missing key: "+ Api.JOB_NAME);
        }

        Object jobNameObj = jobOpts.get(Api.JOB_NAME);
        if (!(jobNameObj instanceof String)) {
            return ApiManager.mkErrorResponse(Api.JOB_NAME +" must be a String.");
        }

        String jobName = (String) jobNameObj;
        EvaJob job = jobFactory.mkJob(jobName);

        if (job == null) {
            return ApiManager.mkErrorResponse("Unknown job: "+jobOpts.getString("job"));
        }

        int jobId = nextJobId;
        nextJobId ++;

        EvaJobProcess newJob = new EvaJobProcess(jobId, job, jobOpts);


        Log.it("New job ("+ Api.JOB_ID +"="+jobId+") successfully made # and starting ...");

        jobs.put(jobId, newJob);
        newJob.start();

        return Api.addOk(F.obj(Api.JOB_ID,jobId));
    }


    JSONObject runJob(JSONArray path, JSONObject query) {
        if (path.length() > 1) {
            query.put(Api.JOB_NAME, path.get(1));
        }
        return runJob(query);
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
                query.put(Api.JOB_ID, idInt);
            } catch (NumberFormatException e) {
                return ApiManager.mkErrorResponse(Api.JOB_ID +" in path must be an integer.");
            }
        }

        if (path.length() > 2) {
            String jobCmdStr = path.getString(2);
            query.put(Api.JOB_CMD, jobCmdStr);
        }

        if (!query.has(Api.JOB_ID)) {
            return ApiManager.mkErrorResponse("Missing key: "+ Api.JOB_ID);
        }

        Object jobIdObj = query.get(Api.JOB_ID);
        if (!(jobIdObj instanceof Integer)) {
            return ApiManager.mkErrorResponse(Api.JOB_ID +" must be an integer.");
        }

        int jobId = (int) jobIdObj;

        if (query.has(Api.JOB_CMD) && query.has("cmd") && query.get("cmd").equals("job")) {
            // instead of showing some job info we process jobCmd
            // e.g. when /job/1/someJobCmd was called.
            //      or   /job/1?{"jobCmd":"someJobCmd"}
            //      or   ?{"cmd":"job", "jobId":1, "jobCmd":"someJobCmd"}
            Api jobApi = getJobApi(jobId);
            if (jobApi == null) {return ApiManager.mkErrorResponse("There is no job with jobId "+jobId);}
            return jobApi.process(path, query);
        }

        return infoFun.apply(jobId);
    }



}
