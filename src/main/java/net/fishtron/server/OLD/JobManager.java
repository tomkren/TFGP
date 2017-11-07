package net.fishtron.server.OLD;

import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Created by Tomáš Křen on 19.2.2017.*/

public class JobManager {

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

    void addJobClass(String jobName, Class<? extends EvaJob_OLD> jobClass, Object initData) {
        jobFactory.addJobClass(jobName, jobClass, initData);
    }


    JSONObject getJobsInfo() {
         JSONArray jobsArr = F.jsonMap(jobs.keySet(), this::getJobInfo);
         return Api_OLD.ok(
                 Api_OLD.JOBS, jobsArr,
                 Api_OLD.JOB_NAMES, jobFactory.getJobNames()
         );
    }

    private JSONObject getJobInfo(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return Api_OLD.error("There is no job with jobId "+jobId);
        }
        return Api_OLD.addOk(jobProcess.toJson());
    }

    private JSONObject getJobLog(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return Api_OLD.error("There is no job with jobId "+jobId);
        }
        String logStr = jobProcess.getLog();
        JSONArray logLines = F.jsonMap(logStr.split("\\n"));
        return Api_OLD.ok(
                "log", logLines,
                "jobId", jobId
        );
    }

    public Api_OLD getJobApi(int jobId) {
        EvaJobProcess jobProcess = jobs.get(jobId);
        if (jobProcess == null) {
            return null;
        }
        return jobProcess.getJobApi();
    }


    public JSONObject runJob(String jobName, JSONObject jobOpts) {
        jobOpts.put(Api_OLD.JOB_NAME, jobName);
        return runJob(jobOpts);
    }

    public JSONObject runJob(JSONObject jobOpts) {

        if (!jobOpts.has(Api_OLD.JOB_NAME)) {
            return Api_OLD.error("Missing key: "+ Api_OLD.JOB_NAME);
        }

        Object jobNameObj = jobOpts.get(Api_OLD.JOB_NAME);
        if (!(jobNameObj instanceof String)) {
            return Api_OLD.error(Api_OLD.JOB_NAME +" must be a String.");
        }

        String jobName = (String) jobNameObj;
        EvaJob_OLD job = jobFactory.mkJob(jobName);

        if (job == null) {
            return Api_OLD.error("Unknown job: "+jobOpts.getString("job"));
        }

        int jobId = nextJobId;
        nextJobId ++;

        EvaJobProcess newJobProcess = new EvaJobProcess(jobId, job, jobOpts, this);

        jobs.put(jobId, newJobProcess);
        newJobProcess.start();

        Log.it("New '"+jobName+"' job ["+ Api_OLD.JOB_ID +"="+jobId+"] successfully started.");

        return Api_OLD.addOk(F.obj(Api_OLD.JOB_ID,jobId));
    }


    JSONObject runJob(JSONArray path, JSONObject query) {
        if (path.length() > 1) {
            query.put(Api_OLD.JOB_NAME, path.get(1));
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
                query.put(Api_OLD.JOB_ID, idInt);
            } catch (NumberFormatException e) {
                return Api_OLD.error(Api_OLD.JOB_ID +" in path must be an integer.");
            }
        }

        if (path.length() > 2) {
            String jobCmdStr = path.getString(2);
            query.put(Api_OLD.JOB_CMD, jobCmdStr);
        }

        if (!query.has(Api_OLD.JOB_ID)) {
            return Api_OLD.error("Missing key: "+ Api_OLD.JOB_ID);
        }

        Object jobIdObj = query.get(Api_OLD.JOB_ID);
        if (!(jobIdObj instanceof Integer)) {
            return Api_OLD.error(Api_OLD.JOB_ID +" must be an integer.");
        }

        int jobId = (int) jobIdObj;

        if (query.has(Api_OLD.JOB_CMD) && query.has(Api_OLD.CMD) && query.get(Api_OLD.CMD).equals(Api_OLD.CMD_JOB)) {
            // instead of showing some job info we process jobCmd
            // e.g. when /job/1/someJobCmd was called.
            //      or   /job/1?{"jobCmd":"someJobCmd"}
            //      or   ?{"cmd":"job", "jobId":1, "jobCmd":"someJobCmd"}
            Api_OLD jobApi = getJobApi(jobId);
            if (jobApi == null) {return Api_OLD.error("There is no job with jobId "+jobId);}
            return jobApi.processApiCall_OLD(path, query);
        }

        return infoFun.apply(jobId);
    }



}
