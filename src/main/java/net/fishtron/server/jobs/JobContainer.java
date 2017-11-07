package net.fishtron.server.jobs;

import net.fishtron.server.api.Api;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.*;

/**
 * Created by tom on 22.08.2017.
 */
public class JobContainer extends TimerTask {


    enum Status {beforeStart, running, sleeping, stopping, finished}

    private Status status;
    private final int jobContainerId;
    private final EvaJob job;
    private final EvaJob.Config jobConfig;

    private final Deque<Object> outputs;
    private final Deque<Object> errorOutputs;
    private static final int MAX_LOG_SIZE = 10000; // todo předelat na parametr v job configu
    private static final int MAX_ERROR_LOG_SIZE = 10000; // todo předelat na parametr v job configu
    private long numAllLogLines;
    private long numAllErrorLogLines;
    private boolean hasLastLogNoln;

    private boolean isStopRequested;
    private boolean isRestartRequested;


    public JobContainer(int jobContainerId, EvaJob job, EvaJob.Config jobConfig) {
        setStatus(Status.beforeStart);
        setIsStopRequested(false);
        setIsRestartRequested(false);

        this.jobContainerId = jobContainerId;
        this.job = job;
        this.jobConfig = new EvaJob.Config(jobConfig);

        outputs = new ArrayDeque<>();
        errorOutputs = new ArrayDeque<>();
        numAllLogLines = 0;
        numAllErrorLogLines = 0;
        hasLastLogNoln = false;
    }

    // TimerTask run method
    @Override
    public void run() {
        if (getStatus() != Status.running) {
            startOneExecutionInNewThread();
        } else {
            infoLog("Rejected scheduled start, job still running.");
        }
    }

    private void startOneExecutionInNewThread() {
        setStatus(Status.running);
        (new Thread(()->{
            Instant job_start = Instant.now();

            JSONObject resultReport;
            try {
                resultReport = job.runJob(jobConfig, this);
            } catch (Throwable e) {
                resultReport = Api.addError(e.getMessage(), F.obj(
                        "stackTrace", F.jsonMap(e.getStackTrace(), StackTraceElement::toString)
                ));
            }

            Instant job_end = Instant.now();

            JSONObject metaInfo = F.obj(
                    "job_start", job_start.toString(),
                    "job_end", job_end.toString(),
                    "job_time", prettyDeltaSeconds(job_end, job_start)
            );

            if (isStopRequested()) {
                metaInfo.put("job_stopped", true);
            }

            resultReport.put("meta", metaInfo);
            log(resultReport);

            setIsStopRequested(false);

            if (isRestartRequested()) {

                setIsRestartRequested(false);
                startOneExecutionInNewThread();

            } else {
                boolean isSleeping = jobConfig.isRepeated() && !isStopRequested();
                setStatus(isSleeping ? Status.sleeping : Status.finished);
            }


        })).start();
    }

    public void stopJobContainer() {
        this.cancel();
        setIsStopRequested(true);
        setStatusAfterStopRequest();
    }


    public void restartJobContainer() {

        if (getStatus() == Status.finished) {
            startOneExecutionInNewThread();
        } else {
            requestStopAndRestart();
        }

    }

    private static double prettyDeltaSeconds(Instant end, Instant start) {
        double deltaSeconds = (end.toEpochMilli() - start.toEpochMilli()) / 1000.0;
        return F.prettyDouble(deltaSeconds, 5);
    }

    synchronized public void log(Object raw_x) {

        Object x = parseLogObject(raw_x);

        if (isErrorLogObject(x)) {

            errorOutputs.offer(x);
            numAllErrorLogLines ++;

            if (errorOutputs.size() > getMaxErrorLogSize()) {
                errorOutputs.pollFirst();
            }

        }

        if (hasLastLogNoln && (x instanceof String)) {
            Object lastLogObj = outputs.peekLast();
            if (lastLogObj instanceof String) {
                outputs.pollLast();
                x = lastLogObj.toString() + x.toString();
            }
        }

        outputs.offer(x);
        numAllLogLines ++;

        if (outputs.size() > getMaxLogSize()) {
            outputs.pollFirst();
        }

        hasLastLogNoln = false;
    }

    synchronized public void log_noln(Object raw_x) {
        log(raw_x);
        hasLastLogNoln = true;
    }


    public int getMaxLogSize() {return MAX_LOG_SIZE;}
    public int getMaxErrorLogSize() {return  MAX_ERROR_LOG_SIZE;}

    public long getNumAllLogLines() {return numAllLogLines;}
    public long getNumAllErrorLogLines() {return numAllErrorLogLines;}


    private void infoLog(Object x) {
        log("__INFO__ : "+x+" ["+ Instant.now()+ "]");
    }

    synchronized public JSONArray getLog(int numLast) {
        return getLog(outputs, numLast);
    }

    synchronized public JSONArray getErrorLog(int numLast) {
        return getLog(errorOutputs, numLast);
    }

    private static JSONArray getLog(Deque<Object> logDeque, int numLast) {
        if (numLast <  0) {return F.jsonMap(logDeque);}
        if (numLast == 0) {return F.arr();}

        Iterator<Object> iter = logDeque.descendingIterator();

        Deque<Object> ret = new ArrayDeque<>();
        while (ret.size() < numLast && iter.hasNext()) {
            ret.offerFirst(iter.next());
        }

        return F.jsonMap(ret);
    }


    private static Object parseLogObject(Object x) {
        if (x == null) {return JSONObject.NULL;}
        return x;
    }

    private static boolean isErrorLogObject(Object x) {
        if (x instanceof JSONObject) {
            JSONObject obj = (JSONObject) x;
            if (obj.has(Api.KEY_status)) {
                return ! obj.optString(Api.KEY_status, Api.STATUS_error).equals(Api.STATUS_ok);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }


    private synchronized void setStatus(Status newStatus) { status = newStatus; }
    private synchronized Status getStatus() { return status; }

    private synchronized void setStatusAfterStopRequest() {
        if (status == Status.running) {
            status = Status.stopping;
        } else {
            status = Status.finished;
        }
    }


    private synchronized void setIsStopRequested(boolean newIsStopRequested) { isStopRequested = newIsStopRequested; }
    public synchronized boolean isStopRequested() { return isStopRequested; }

    private synchronized void setIsRestartRequested(boolean newIsRestartRequested) {isRestartRequested = newIsRestartRequested;}
    private synchronized boolean isRestartRequested() {return isRestartRequested;}

    private synchronized void requestStopAndRestart() {
        isStopRequested = true;
        isRestartRequested = true;

        status = (status == Status.running ? Status.stopping : Status.finished);
    }



    public Api getJobApi() { return job; }


    public int getJobContainerId() { return jobContainerId; }

    public JSONObject toJson() {
        return F.obj(
                Api.KEY_jobContainerId, jobContainerId,
                Api.KEY_jobConfig, jobConfig.json(),
                Api.KEY_jobStatus, getStatus().name(),
                Api.KEY_isRepeated, jobConfig.isRepeated()
        );
    }

}
