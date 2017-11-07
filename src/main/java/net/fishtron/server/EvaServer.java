package net.fishtron.server;

import net.fishtron.server.jobs.templates.TestFactorization;
import net.fishtron.server.jobs.EvaJob;
import net.fishtron.server.managers.ApiManager;
import net.fishtron.server.managers.JobManager;
import net.fishtron.server.managers.Manager;
import net.fishtron.utils.F;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;


/**
 * Created by tom on 02.10.2017.
 */
public class EvaServer extends AbstractHandler implements Manager {

    private static final String VERSION = "0.2.1";

    public static final String KEY_port = "port";

    private static JSONObject defaultConfig = F.obj(
            KEY_port, 2342
    );

    private final Server server;
    private final ApiManager apiMan;
    private final JobManager jobMan;

    public EvaServer(JSONObject config) {
        int port = config.getInt(KEY_port);

        List<Manager> managers = new ArrayList<>();
        managers.add(this);


        jobMan = new JobManager(Collections.emptyList());
        managers.add(jobMan);

        apiMan = new ApiManager(VERSION);
        managers.add(apiMan);


        F.log("\nMANAGERS REGISTER CMDs:");
        for (Manager man : managers) {
            F.log(man.greetings());
            apiMan.addApiCmds(man.mkApiCmds());
        }


        server = new Server(port);
        server.setHandler(this);
    }

    public void startEvaServer() {
        try {
            jobMan.start();
            server.start();

            //server.join();

        } catch (Exception e) {
            System.err.println("!!! ERROR :" + e.getMessage());
        }
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass, Object initData) {
        jobMan.addJobClass(jobName, jobClass, initData);
        F.log("Added class for job '"+jobName+"'.");
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass) {
        jobMan.addJobClass(jobName, jobClass, null);
        F.log("Added class for job '"+jobName+"'.");
    }

    @Override
    public String greetings() {
        return "EvaServer at your command, sir!";
    }


    public JobManager getJobManager() {
        return jobMan;
    }

    public void stopEvaServer() {
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

        handleRequest(baseRequest, request, apiMan::processRawApiCall, response);
    }

    public static void handleRequest(Request baseRequest,
                                     HttpServletRequest request,
                                     BiFunction<String,String,JSONObject> processRawApiCall,
                                     HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=utf-8");

        String path = request.getPathInfo();
        String encodedQueryString = request.getQueryString();
        String query = encodedQueryString == null ? null : URLDecoder.decode(encodedQueryString, "UTF-8");

        JSONObject jsonResponse = processRawApiCall.apply(path, query);

        addHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println(jsonResponse.toString());
    }

    public static void addHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "X-Unity-Version, X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Access-Control-Max-Age", "1728000");
    }

    private static void runServer() {
        EvaServer evaServer = new EvaServer(defaultConfig);
        evaServer.addJobClass(TestFactorization.getJobName(), TestFactorization.class);
        evaServer.startEvaServer();
    }

    public static void main(String[] args) {
        runServer();
    }

}
