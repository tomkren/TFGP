package cz.tomkren.fishtron.server;

import cz.tomkren.fishtron.mains.DagEvaTester;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import cz.tomkren.utils.ResourceLoader;
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

    private Map<Integer, DagEvaTester> tests;
    private int nextTestId;

    public EvolutionServer(int port) {
        server = new Server(port);
        server.setHandler(this);

        jobs = new HashMap<>();
        nextJobId = 1;

        tests = new HashMap<>();
        nextTestId = 1;
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
        String cmdNameStr = CmdName.getHtml.name();

        if (encodedQueryString != null) {

            String decodedQueryStr = URLDecoder.decode(encodedQueryString, "UTF-8");
            try {
                JSONArray jsonCmd = new JSONArray(decodedQueryStr);
                cmdNameStr = jsonCmd.getString(0);
                responseToCmd = runCmd(cmdNameStr, jsonCmd.get(1));
            } catch (Exception e) {
                cmdNameStr = decodedQueryStr;
                responseToCmd = runCmd(cmdNameStr, new JSONObject());
            }

        } else {
            responseToCmd = defaultResponse(new JSONObject());
        }

        String finalResponseStr;
        if (responseToCmd instanceof JSONObject) {
            finalResponseStr = ((JSONObject)responseToCmd).toString(2);
            response.setContentType("application/json;charset=utf-8");
        } else {
            finalResponseStr = responseToCmd.toString();

            if (cmdNameStr.equals(CmdName.getHtml.name())) {
                response.setContentType("text/html;charset=utf-8");
            }
        }

        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "X-Unity-Version, X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Access-Control-Max-Age", "1728000");

        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(finalResponseStr);
    }

    private Object defaultResponse(Object arg) {
        return runCmd(CmdName.getHtml, arg);
    }

    private enum CmdName {getApi, makeJob, jobInfo, jobsInfo, jobLog, getHtml, runTest, testInfo};

    private Object runCmd(String cmdName, Object arg) {
        try {
            return runCmd(CmdName.valueOf(cmdName), arg);
        } catch (IllegalArgumentException e) {
            return defaultResponse(arg);
        }
    }

    private Object runCmd(CmdName cmdName, Object arg) {

        switch (cmdName) {
            case makeJob:  return makeJob(arg);
            case jobInfo:  return jobInfo(arg);
            case jobsInfo: return jobsInfo(arg);
            case jobLog:   return jobLog(arg);
            case getHtml:  return getHtml(arg);
            case runTest:  return runTest(arg);
            case testInfo: return testInfo(arg);
            case getApi:   return getApi();
            default :      return defaultResponse(arg);
        }
    }

    private JSONObject getApi() {
        return F.obj(
            "api", F.obj(
                CmdName.makeJob.toString() , F.obj("info", "Creates new job, returns jobId."),
                CmdName.jobInfo.toString() , F.obj("info", "Returns info about the requested job."),
                CmdName.jobsInfo.toString(), F.obj("info", "Returns info about all created jobs."),
                CmdName.jobLog.toString()  , F.obj("info", "Returns log for the requested job."),
                CmdName.getHtml.toString() , F.obj("info", "Returns html control page."),
                CmdName.getApi.toString()  , F.obj("info", "Returns this api.")
        ));
    }

    private JSONObject runTest(Object arg) {

        int testId = nextTestId;
        nextTestId++;
        JSONObject jsonResponse = F.obj("testId", testId);

        DagEvaTester tester = new DagEvaTester();
        tests.put(testId, tester);

        new Thread(tester::runTest).start();

        return jsonResponse;
    }

    private JSONObject testInfo(Object arg) {

        JSONObject jsonResponse;
        if (arg instanceof Integer) {

            int testId = (int)arg;
            DagEvaTester tester = tests.get(testId);

            if (tester != null) {
                jsonResponse = tester.getTestInfo();
            } else {
                jsonResponse = F.obj("error", "There is no test with that testId.");
            }

            jsonResponse.put("testId", testId);

        } else {
            jsonResponse = F.obj("error", "You must specify an (int) testId, instead you supplied: "+arg);
        }

        return jsonResponse;
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

    public static final String HTML_HOME = "/cz/tomkren/fishtron/server/index.html";

    private String getHtml(Object arg){

        return new ResourceLoader().loadString(HTML_HOME);

    }

    public static void main(String[] args) throws Exception {

        EvolutionServer es = new EvolutionServer(4223);
        es.startServer();

    }
}
