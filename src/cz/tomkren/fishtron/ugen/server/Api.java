package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

/**Created by tom on 20.03.2017.*/

public interface Api {

    String JOB_NAME = "job";
    String JOBS = "jobs";
    String JOB_NAMES = "jobNames";
    String JOB_ID = "jobId";
    String JOB_CMD = "jobCmd";

    JSONObject process(JSONArray path, JSONObject query);


    static JSONObject addOk(JSONObject response) {
        response.put("status", "ok");
        return response;
    }

    static JSONObject ok(Object... objs) {
        return addOk(F.obj(objs));
    }

}
