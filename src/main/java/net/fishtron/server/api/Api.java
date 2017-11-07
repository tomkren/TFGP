package net.fishtron.server.api;

import net.fishtron.utils.Either;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

/** Created by tom on 21.08.2017. */

public interface Api {

    // TODO move BH-specific constants away !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    JSONObject processApiCall(JSONArray path, JSONObject query);

    default JSONObject getInfo() {
        return F.obj();
    }



    String KEY_cmd = "cmd";

    String CMD_jobs = "jobs";
    String CMD_run = "run";
    String CMD_job = "job";
    String JCMD_stop = "stop";
    String JCMD_restart = "restart";
    String JCMD_log = "log";
    String JCMD_errorLog = "errorLog";
    String CMD_addJobConfig = "addJobConfig";
    String CMD_updateJobConfig = "updateJobConfig";
    String CMD_offers = "offers";
    String CMD_set = "set";
    String CMD_getOneOffer = "getOneOffer";
    String CMD_getInitState = "getInitState";
    String CMD_setFlag = "setFlag";
    String CMD_countOffers = "countOffers";
    String CMD_getFreshOfferData = "getFreshOfferData";
    String CMD_exit = "exit";

    // keys
    String KEY_version = "version";
    String KEY_status = "status";
    String KEY_opts = "opts";
    String KEY_msg = "msg";
    String KEY_cmds = "cmds";
    String KEY_jobName = "jobName";
    String KEY_jobContainers = "jobContainers";
    String KEY_jobConfigs = "jobConfigs";
    String KEY_jobNames = "jobNames";
    String KEY_jobContainerId = "jobContainerId";
    String KEY_jobCmd = "jobCmd";
    String KEY_jobConfig = "jobConfig";
    String KEY_jobStatus = "jobStatus";
    String KEY_delay = "delay";
    String KEY_period = "period";
    String KEY_scheduleOnStartup = "scheduleOnStartup";
    String KEY__id = "_id";
    String KEY_isRepeated = "isRepeated";

    String KEY_offerID = "offerID";
    String KEY_marketID = "marketID";

    String KEY_log = "log";
    String KEY_numLast = "numLast";
    String KEY_maxLogSize = "maxLogSize";
    String KEY_maxErrorLogSize = "maxErrorLogSize";
    String KEY_numAllLogLines = "numAllLogLines";
    String KEY_numAllErrorLogLines = "numAllErrorLogLines";

    String KEY_data = "data";

    String KEY_path = "path";
    String KEY_query = "query";

    String KEY_contentLength = "contentLength";

    // status
    String STATUS_ok = "ok";
    String STATUS_error = "error";

    // Job Commands
    String JCMD_content = "content";

    // Manuals
    String MAN_todo = "[TODO] Currently there is no manual for this command, sorry.";


    static JSONObject addError(String msg, JSONObject response) {
        response.put(KEY_status, STATUS_error);
        response.put(KEY_msg, msg);
        return response;
    }

    static JSONObject error(String msg) {
        return addError(msg, new JSONObject());
    }

    static JSONObject addOk(JSONObject response) {
        response.put(KEY_status, STATUS_ok);
        return response;
    }

    static JSONObject ok(Object... objs) {
        return addOk(F.obj(objs));
    }


    static Either<String,JSONObject> getPathOrQueryString(JSONArray path, int pathIndex, JSONObject query, String key) {
        if (path.length() > pathIndex) {
            return Either.ok(path.getString(pathIndex));
        } else {
            return getQueryString(query, key);
        }

    }

    static Either<String,JSONObject> getQueryString(JSONObject query, String key) {
        if (query.has(key)) {
            Object value = query.get(key);
            if (value instanceof String) {
                return Either.ok((String) value);
            } else {
                return Either.ko(error("The "+key+" must be a string."));
            }
        } else {
            return Either.ko(error("A "+key+" must be specified in the query."));
        }
    }
}
