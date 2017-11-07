package net.fishtron.server.managers;

import net.fishtron.server.api.Api;
import net.fishtron.server.api.ApiCmd;

import net.fishtron.utils.F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** Created by sekol on 12.12.2016. */

public class ApiManager implements Api, Manager {

    private final Map<String,Api> apiCmds;
    private final Map<String,String> cmdManuals;
    private final String version;

    public ApiManager(String version) {
        apiCmds = new HashMap<>();
        cmdManuals = new HashMap<>();
        this.version = version;
    }

    public void addApiCmds(List<ApiCmd> apiCmds) {
        apiCmds.forEach(this::addApiCmd);
    }

    private void addApiCmd(ApiCmd apiCmd) {
        String cmdName = apiCmd.getName();
        apiCmds.put(cmdName, apiCmd.getApiProcessor());
        cmdManuals.put(cmdName, apiCmd.getManual());
        F.log(" ADD cmd", cmdName, ":", apiCmd.getManual());
    }


    public JSONObject processRawApiCall(String path, String query) {

        String[] pathParts = path.split("/");
        JSONArray pathJson = F.jsonMap(F.filter(Arrays.asList(pathParts), x->!x.equals("")), x->x);

        if (query == null) {
            if (pathJson.length() == 0) {
                return mkIndexResponse();
            } else {
                return processApiCall(pathJson, null);
            }
        }

        try {
            JSONObject jsonRequest = new JSONObject(query);
            return processApiCall(pathJson, jsonRequest);
        } catch (JSONException e) {
            try {
                JSONObject jsonRequest = parseStdQuery(query, "&");
                return processApiCall(pathJson, jsonRequest);
            } catch (ParseException pe) {
                return Api.error("Unsupported query detected, query must be a JSON or stdQuery.");
            }
        } catch (Exception e) {
            return mkUnexpectedErrorResponse(e);
        }
    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {

        if (query == null) {
            query = F.obj();
        }

        String cmd = null;

        if (path.length() > 0) {
            cmd = path.getString(0); // higher priority than cmd field in query
            query.put(Api.KEY_cmd, cmd);   // and it overrides it, so we know that actual cmd is in the query
        } else if (query.has(Api.KEY_cmd) && query.get(Api.KEY_cmd) instanceof String) {
            cmd = query.getString(Api.KEY_cmd);
        }

        if (cmd != null) {

            Api apiCmd = apiCmds.get(cmd);

            if (apiCmd != null) {
                return apiCmd.processApiCall(path, query);
            }
        }

        return Api.addError("Unsupported command.", F.obj("path",path, "query",query));
    }


    @Override
    public String greetings() {
        return "ApiManager, watching the requests for you, no problemo.";
    }

    private JSONObject mkIndexResponse() {
        return Api.ok(
                Api.KEY_msg, "Welcome to BrickSim API! :)",
                Api.KEY_version, version,
                Api.KEY_cmds, F.jsonMap(cmdManuals, x->x)
        );
    }


    private static JSONObject mkUnexpectedErrorResponse(Exception e) {
        String msg = (e.getMessage() == null ? "" : " ... " + e.getMessage());
        return Api.error("Unexpected error occurred: " + e.toString() + msg);
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
