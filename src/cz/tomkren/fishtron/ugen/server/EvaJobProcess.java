package cz.tomkren.fishtron.ugen.server;

import org.json.JSONObject;

/** Created by tom on 05.03.2017. */

public class EvaJobProcess {

    enum Status {beforeStart, running, finished}

    private Status status;
    private final EvaJob job;
    private final JSONObject jobOpts;
    private final StringBuffer output;

    EvaJobProcess(EvaJob job, JSONObject jobOpts) {
        setStatus(Status.beforeStart);
        this.job = job;
        this.jobOpts = jobOpts;
        output = new StringBuffer();
    }

    public void start() {
        setStatus(Status.running);
        (new Thread(()->{
            job.run(jobOpts, this);
            setStatus(Status.finished);
        })).start();
    }

    private synchronized void setStatus(Status newStatus) {
        status = newStatus;
    }

}
