package net.fishtron.server.OLD;

import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 05.03.2017. */

public class EvaJobProcess {

    enum Status {beforeStart, running, finished}

    private Status status;
    private final int jobId;
    private final EvaJob_OLD job;
    private final JSONObject jobOpts;
    private final StringBuffer output;
    private final JobManager jobMan;

    EvaJobProcess(int jobId, EvaJob_OLD job, JSONObject jobOpts, JobManager jobMan) {
        setStatus(Status.beforeStart);
        this.jobId = jobId;
        this.job = job;
        this.jobOpts = jobOpts;
        this.jobMan = jobMan;

        if (jobOpts.has(Api_OLD.CMD)) {
            jobOpts.remove(Api_OLD.CMD);
        }

        output = new StringBuffer();
    }

    public void start() {
        setStatus(Status.running);
        (new Thread(()->{
            job.runJob_OLD(jobOpts, this);
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

    Api_OLD getJobApi() {
        return job;
    }

    public JobManager getJobManager() {
        return jobMan;
    }

    public JSONObject toJson() {
        return F.obj(
                Api_OLD.JOB_ID, jobId,
                Api_OLD.JOB_OPTS, jobOpts,
                Api_OLD.JOB_STATUS, getStatus().name()
        );
    }

}
