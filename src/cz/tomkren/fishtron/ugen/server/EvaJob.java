package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.fishtron.server.EvolutionJob;
import org.json.JSONObject;

/** Created by sekol on 05.03.2017. */

public abstract class EvaJob {

    enum Status {beforeStart, running, finished}

    private Status status;
    private final StringBuffer output;
    private final JSONObject jobOpts;


    public abstract void run(JSONObject jobOpts);

    public EvaJob(JSONObject jobOpts) {
        setStatus(Status.beforeStart);
        this.jobOpts = jobOpts;
        output = new StringBuffer();
    }

    public void start() {
        setStatus(Status.running);
        (new Thread(()->{
            run(jobOpts);
            setStatus(Status.finished);
        })).start();
    }

    private synchronized void setStatus(Status newStatus) {
        status = newStatus;
    }

}
