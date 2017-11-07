package net.fishtron.server;

import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 05.03.2017. */

public class EvaJobProcess {

    enum Status {beforeStart, running, finished}

    private Status status;
    private final int jobId;
    private final EvaJob job;
    private final JSONObject jobOpts;
    private final StringBuffer output;
    private final JobManager jobMan;

    EvaJobProcess(int jobId, EvaJob job, JSONObject jobOpts, JobManager jobMan) {
        setStatus(Status.beforeStart);
        this.jobId = jobId;
        this.job = job;
        this.jobOpts = jobOpts;
        this.jobMan = jobMan;

        if (jobOpts.has(Api.CMD)) {
            jobOpts.remove(Api.CMD);
        }

        output = new StringBuffer();
    }

    public void start() {
        setStatus(Status.running);
        (new Thread(()->{
            job.runJob(jobOpts, this);
            setStatus(Status.finished);
        })).start();
    }

    public void log(Object x) {
        output.append(x).append('\n');
    }

    public void logList(List<?> xs) {
        for (Object x : xs) {
            log(x);
        }
    }

    public void log_noln(Object x) {
        output.append(x);
    }

    public String getLog() {
        return output.toString();
    }

    private synchronized void setStatus(Status newStatus) {
        status = newStatus;
    }

    private synchronized Status getStatus() {
        return status;
    }

    Api getJobApi() {
        return job;
    }

    public JobManager getJobManager() {
        return jobMan;
    }

    public JSONObject toJson() {
        return F.obj(
                Api.JOB_ID, jobId,
                Api.JOB_OPTS, jobOpts,
                Api.JOB_STATUS, getStatus().name()
        );
    }

}
