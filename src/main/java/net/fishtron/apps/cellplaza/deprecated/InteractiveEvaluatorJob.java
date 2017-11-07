package net.fishtron.apps.cellplaza.deprecated;


import net.fishtron.server.OLD.Api_OLD;
import net.fishtron.server.OLD.EvaJob_OLD;
import net.fishtron.server.OLD.EvaJobProcess;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

/**Created by tom on 21.03.2017.*/

public class InteractiveEvaluatorJob implements EvaJob_OLD {

    static final String JOB_NAME = "InteractiveEval";
    private static final String CMD_GET_POOL = "pool";
    static final String CMD_ADD_TO_POOL = "add";
    static final String INDIVS = "indivs";


    private EvaJobProcess jobProcess;

    @Override
    public void runJob_OLD(JSONObject jobOpts, EvaJobProcess jobProcess) {

        this.jobProcess = jobProcess;

        /*int i = 1;
        while (true) {
            jobProcess.log("("+i+") Log jak brno");
            F.sleep(1000);
            i++;
        }*/

    }

    @Override
    public JSONObject processApiCall_OLD(JSONArray path, JSONObject query) {
        String jobCmd = query.optString(Api_OLD.JOB_CMD, "WRONG FORMAT OR MISSING, MUST BE STRING");

        jobProcess.logList(F.map(query.getJSONArray("indivs"),x->x));

        if (jobCmd.equals(CMD_GET_POOL)) {
            return Api_OLD.ok("TODO", "Tady bude pool!");
        } else if (jobCmd.equals(CMD_ADD_TO_POOL)) {

            return Api_OLD.ok("TODO", "Tady bude aknoulidžment že sem to přidal");

        } else {
            return Api_OLD.error("Unsupported "+ Api_OLD.JOB_CMD+": "+jobCmd);
        }
    }
}
