package cz.tomkren.fishtron.server;

import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/** Created by tom on 13. 6. 2016.*/

public class EvolutionServer extends AbstractHandler {

    private Server server;

    public EvolutionServer(int port) {
        server = new Server(port);
        server.setHandler(this);
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

        JSONObject responseToCmd;

        if (encodedQueryString != null) {

            String decodedQueryStr = URLDecoder.decode(encodedQueryString, "UTF-8");

            Log.it("cmd = "+decodedQueryStr);

            responseToCmd = runCmd(decodedQueryStr);

        } else {
            responseToCmd = runCmd(CmdName.getApi);
        }




        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        response.getWriter().println(responseToCmd.toString(2));

    }

    private enum CmdName {getApi, makeJob, jobInfo};

    private JSONObject runCmd(String cmdName) {
        try {
            return runCmd(CmdName.valueOf(cmdName));
        } catch (IllegalArgumentException e) {
            return runCmd(CmdName.getApi);
        }
    }

    private JSONObject runCmd(CmdName cmdName) {

        switch (cmdName) {
            case makeJob: return makeJob();
            case jobInfo: return jobInfo();
            case getApi:  return getApi();
            default :     return getApi();
        }
    }

    private JSONObject getApi() {
        return F.obj(
            "api", F.obj(
                CmdName.makeJob.toString(), "TODO",
                CmdName.jobInfo.toString(), "TODO2",
                CmdName.getApi.toString() , "TODO0"
        ));
    }

    private JSONObject makeJob() {
        Log.it("TODO: makeJob called!");
        return F.obj("msg", "TODO!");
    }

    private JSONObject jobInfo() {
        Log.it("TODO: jobInfo called!");
        return F.obj("msg", "TODO!!");
    }

    public static void main(String[] args) throws Exception {

        EvolutionServer es = new EvolutionServer(4223);
        es.startServer();

    }
}
