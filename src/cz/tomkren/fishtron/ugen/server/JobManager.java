package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    JSONObject addJob(EvaJob job, JSONObject jobOpts) {
        int jobId = nextJobId;
        nextJobId ++;

        Log.it("addJob #"+jobId+" ...");

        JSONObject jsonResponse = F.obj("jobId", jobId);

        jsonResponse.put("jobOpts", jobOpts);

        EvaJobProcess newJob = new EvaJobProcess(job, jobOpts);

        jobs.put(jobId, newJob);
        newJob.start();

        return jsonResponse;
    }

}
