package net.fishtron.server.OLD;

import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Created by Tomáš Křen on 19.2.2017.*/

class ApiManager implements Api_OLD {

    private final Map<String,Api_OLD> apiCmds;


    ApiManager(JobManager jobMan) {
        apiCmds = new HashMap<>();

        addApiCmd(Api_OLD.CMD_RUN, jobMan::runJob);
        addApiCmd(Api_OLD.CMD_JOB, jobMan::getJobInfo);
        addApiCmd(Api_OLD.CMD_LOG, jobMan::getJobLog);
        addApiCmd(Api_OLD.CMD_JOBS, jobMan::getJobsInfo);
    }

    private void addApiCmd(String cmdName, Api_OLD apiCmd) {
        apiCmds.put(cmdName, apiCmd);
    }

    private void addApiCmd(String cmdName, Supplier<JSONObject> apiCmd) {
        addApiCmd(cmdName, (p,q) -> apiCmd.get());
    }


    JSONObject processRawApiCall(String path, String query) {

        String[] pathParts = path.split("/");
        JSONArray pathJson = F.jsonMap(F.filter(Arrays.asList(pathParts), x->!x.equals("")), x->x);

        if (query == null) {
            if (pathJson.length() == 0) {
                return mkIndexResponse();
            } else {
                return processApiCall_OLD(pathJson, null);
            }
        }

        try {
            JSONObject jsonRequest = new JSONObject(query);
            return processApiCall_OLD(pathJson, jsonRequest);
        } catch (JSONException e) {
            try {
                JSONObject jsonRequest = parseStdQuery(query, "&");
                return processApiCall_OLD(pathJson, jsonRequest);
            } catch (ParseException pe) {
                return Api_OLD.error("Unsupported query detected, query must be a JSON or stdQuery.");
            }
        } catch (Exception e) {
            return mkUnexpectedErrorResponse(e);
        }
    }

    @Override
    public JSONObject processApiCall_OLD(JSONArray path, JSONObject query) {

        if (query == null) {
            query = F.obj();
        }

        String cmd = null;

        if (path.length() > 0) {
            cmd = path.getString(0); // higher priority than cmd field in query
            query.put(Api_OLD.CMD, cmd);   // and it overrides it, so we know that actual cmd is in the query
        } else if (query.has(Api_OLD.CMD) && query.get(Api_OLD.CMD) instanceof String) {
            cmd = query.getString(Api_OLD.CMD);
        }

        if (cmd != null) {

            Api_OLD apiCmd = apiCmds.get(cmd);

            if (apiCmd != null) {
                return apiCmd.processApiCall_OLD(path, query);
            }
        }

        return F.obj(
                Api_OLD.STATUS, Api_OLD.ERROR,
                Api_OLD.MSG, "Unsupported command.",
                "path", path,
                "query", query
        );
    }

    private static JSONObject mkIndexResponse() {
        return Api_OLD.ok(Api_OLD.MSG, "Welcome to EvaServer API!");
    }

    private static JSONObject mkUnexpectedErrorResponse(Exception e) {
        String msg = (e.getMessage() == null ? "" : " ... " + e.getMessage());
        return Api_OLD.error("Unexpected error occurred: " + e.toString() + msg);
    }

    private static JSONObject parseStdQuery(String query, String sepRegexp) throws ParseException {

        String[] parts = query.split(sepRegexp);
        JSONObject ret = new JSONObject();

        int offset = 0;
        for (String part : parts) {
            String[] subParts = part.split("=");
            if (subParts.length == 2) {
                ret.put(subParts[0], subParts[1]);
            } else {
                throw new ParseException("Wrong format of query part: "+part, offset);
            }
            offset += part.length();
        }

        return ret;
    }


}
