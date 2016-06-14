package cz.tomkren.fishtron.server;

import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/** Created by tom on 13. 6. 2016.*/

public class EvolutionServer extends AbstractHandler {

    private Server server;

    private Map<Integer, EvolutionJob> jobs;
    private int nextJobId;

    public EvolutionServer(int port) {
        server = new Server(port);
        server.setHandler(this);

        jobs = new HashMap<>();
        nextJobId = 1;
    }

    public void startServer() {
        try {

            server.start();
            server.join();

        } catch (Exception e) {
            Log.err("!!! ERROR :"+ e.getMessage());
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        String encodedQueryString = request.getQueryString();

        Object responseToCmd;

        if (encodedQueryString != null) {

            String decodedQueryStr = URLDecoder.decode(encodedQueryString, "UTF-8");
            try {
                JSONArray jsonCmd = new JSONArray(decodedQueryStr);
                responseToCmd = runCmd(jsonCmd.getString(0), jsonCmd.get(1));
            } catch (Exception e) {
                responseToCmd = runCmd(decodedQueryStr, new JSONObject());
            }

        } else {
            responseToCmd = defaultResponse();
        }

        String finalResponseStr;
        if (responseToCmd instanceof JSONObject) {
            finalResponseStr = ((JSONObject)responseToCmd).toString(2);
            response.setContentType("application/json;charset=utf-8");
        } else {
            finalResponseStr = responseToCmd.toString();
            response.setContentType("text/plain;charset=utf-8");
        }

        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(finalResponseStr);
    }

    private Object defaultResponse() {
        return runCmd(CmdName.getApi, null);
    }

    private enum CmdName {getApi, makeJob, jobInfo, jobsInfo, jobLog};

    private Object runCmd(String cmdName, Object arg) {
        try {
            return runCmd(CmdName.valueOf(cmdName), arg);
        } catch (IllegalArgumentException e) {
            return defaultResponse();
        }
    }

    private Object runCmd(CmdName cmdName, Object arg) {

        switch (cmdName) {
            case makeJob:  return makeJob(arg);
            case jobInfo:  return jobInfo(arg);
            case jobsInfo: return jobsInfo(arg);
            case jobLog:   return jobLog(arg);
            case getApi:   return getApi();
            default :      return defaultResponse();
        }
    }

    private JSONObject getApi() {
        return F.obj(
            "api", F.obj(
                CmdName.makeJob.toString() , F.obj("info", "Creates new job, returns jobId."),
                CmdName.jobInfo.toString() , F.obj("info", "Returns info about the requested job."),
                CmdName.jobsInfo.toString(), F.obj("info", "Returns info about all created jobs."),
                CmdName.jobLog.toString()  , F.obj("info", "Returns log for the requested job."),
                CmdName.getApi.toString()  , F.obj("info", "Returns this api.")
        ));
    }

    private JSONObject makeJob(Object arg) {

        int jobId = nextJobId;
        nextJobId++;

        Log.it("makeJob #"+jobId+" ...");

        JSONObject jsonResponse = F.obj("jobId", jobId);

        JSONObject config;
        if (arg instanceof JSONObject) {
            config = (JSONObject)arg;
        } else {
            config = new JSONObject();
            jsonResponse.put("error", "Unsupported config format, default used instead. Config must be JSONObject, but it is: "+arg);
        }
        jsonResponse.put("config", config);

        EvolutionJob newJob = new EvolutionJob(config);
        jobs.put(jobId, newJob);
        newJob.start();

        return jsonResponse;
    }

    private JSONObject jobInfo(Object arg) {

        JSONObject jsonResponse;
        if (arg instanceof Integer) {

            int jobId = (int)arg;
            EvolutionJob job = jobs.get(jobId);

            if (job != null) {
                jsonResponse = getJobSummary(job);
            } else {
                jsonResponse = F.obj("error", "There is no job with that jobId.");
            }

            jsonResponse.put("jobId", jobId);

        } else {
            jsonResponse = F.obj("error", "You must specify an (int) jobId, instead you supplied: "+arg);
        }

        Log.it("jobInfo: "+jsonResponse);
        return jsonResponse;
    }

    private JSONObject getJobSummary(EvolutionJob job) {
        return F.obj("status", job.getStatus());
    }

    private JSONObject jobsInfo(Object arg) {
        JSONObject jsonResponse = new JSONObject();
        for (Map.Entry<Integer,EvolutionJob> e : jobs.entrySet()) {
            jsonResponse.put(Integer.toString(e.getKey()), getJobSummary(e.getValue()));
        }
        return jsonResponse;
    }

    private String jobLog(Object arg) {
        if (arg instanceof Integer) {

            int jobId = (int) arg;
            EvolutionJob job = jobs.get(jobId);

            if (job != null) {

                return job.getLog();

            } else {
                return "error: There is no job with that jobId.";
            }
        } else {
            return "error: You must specify an (int) jobId, instead you supplied: "+arg;
        }
    }

    public static void main(String[] args) throws Exception {

        EvolutionServer es = new EvolutionServer(4223);
        es.startServer();

    }
}
