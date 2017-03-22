package cz.tomkren.fishtron.ugen.apps.cellplaza.deprecated;


import cz.tomkren.fishtron.ugen.server.Api;
import cz.tomkren.fishtron.ugen.server.EvaJob;
import cz.tomkren.fishtron.ugen.server.EvaJobProcess;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

/**Created by tom on 21.03.2017.*/

public class InteractiveEvaluatorJob implements EvaJob {

    static final String JOB_NAME = "InteractiveEval";
    private static final String CMD_GET_POOL = "pool";
    static final String CMD_ADD_TO_POOL = "add";
    static final String INDIVS = "indivs";


    private EvaJobProcess jobProcess;

    @Override
    public void runJob(JSONObject jobOpts, EvaJobProcess jobProcess) {

        this.jobProcess = jobProcess;

        /*int i = 1;
        while (true) {
            jobProcess.log("("+i+") Log jak brno");
            F.sleep(1000);
            i++;
        }*/

    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        String jobCmd = query.optString(Api.JOB_CMD, "WRONG FORMAT OR MISSING, MUST BE STRING");

        jobProcess.logList(F.map(query.getJSONArray("indivs"),x->x));

        if (jobCmd.equals(CMD_GET_POOL)) {
            return Api.ok("TODO", "Tady bude pool!");
        } else if (jobCmd.equals(CMD_ADD_TO_POOL)) {

            return Api.ok("TODO", "Tady bude aknoulidžment že sem to přidal");

        } else {
            return Api.error("Unsupported "+Api.JOB_CMD+": "+jobCmd);
        }
    }
}
