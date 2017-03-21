package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

/**Created by tom on 20.03.2017.*/

public interface Api {

    JSONObject processApiCall(JSONArray path, JSONObject query);


    String CMD = "cmd";

    // cmds
    String CMD_RUN  = "run";
    String CMD_JOB  = "job";
    String CMD_LOG  = "log";
    String CMD_JOBS = "jobs";

    // keys
    String STATUS = "status";
    String MSG = "msg";
    String JOB_NAME = "job";
    String JOBS = "jobs";
    String JOB_NAMES = "jobNames";
    String JOB_ID = "jobId";
    String JOB_CMD = "jobCmd";
    String JOB_OPTS = "jobOpts";
    String JOB_STATUS = "jobStatus";

    // status
    String OK = "ok";
    String ERROR = "error";



    static JSONObject error(String msg) {
        return F.obj(
                Api.STATUS, Api.ERROR,
                Api.MSG, msg
        );
    }

    static JSONObject addOk(JSONObject response) {
        response.put(STATUS, OK);
        return response;
    }

    static JSONObject ok(Object... objs) {
        return addOk(F.obj(objs));
    }

}
