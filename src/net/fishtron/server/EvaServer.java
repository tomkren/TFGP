package net.fishtron.server;

import net.fishtron.server.jobs.Test;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/** Created by Tomáš Křen on 18.2.2017.*/

public class EvaServer extends AbstractHandler {

    private static JSONObject defaultConfig = F.obj(
            "port", 2342
    );

    private final Server server;
    private final ApiManager apiMan;
    private final JobManager jobMan;


    public EvaServer(JSONObject config) {
        int port = config.getInt("port");

        jobMan = new JobManager();
        apiMan = new ApiManager(jobMan);

        server = new Server(port);
        server.setHandler(this);
    }

    public void startServer() {
        try {

            jobMan.start();

            server.start();
            server.join();

        } catch (Exception e) {
            Log.err("!!! ERROR :" + e.getMessage());
        }
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass, Object initData) {
        jobMan.addJobClass(jobName, jobClass, initData);
        Log.it("Added class for job '"+jobName+"'.");
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass) {
        jobMan.addJobClass(jobName, jobClass, null);
        Log.it("Added class for job '"+jobName+"'.");
    }

    public JobManager getJobManager() {
        return jobMan;
    }

    public void stopEva() {
        try {
            server.stop();
            jobMan.stop();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=utf-8");

        String path = request.getPathInfo();
        String encodedQueryString = request.getQueryString();
        String query = encodedQueryString == null ? null : URLDecoder.decode(encodedQueryString, "UTF-8");

        JSONObject jsonResponse = apiMan.processRawApiCall(path, query);

        addHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(jsonResponse.toString());
    }

    private static void addHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "X-Unity-Version, X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Access-Control-Max-Age", "1728000");
    }

    private static void runServer() {
        EvaServer evaServer = new EvaServer(defaultConfig);
        evaServer.addJobClass("test", Test.class);
        evaServer.startServer();
    }

    public static void main(String[] args) {
        runServer();
    }

}
