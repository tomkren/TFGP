package net.fishtron.server.api;

import org.json.JSONObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by tom on 27.08.2017.
 */
public class ApiCmd {

    private final String name;
    private final Api apiProcessor;
    private final String manual;

    public ApiCmd(String name, Api apiProcessor, String manual) {
        this.name = name;
        this.apiProcessor = apiProcessor;
        this.manual = manual;
    }

    public ApiCmd(String cmdName, Supplier<JSONObject> apiCmd, String comment) {
        this(cmdName, (p,q) -> apiCmd.get(), comment);
    }

    public ApiCmd(String cmdName, Function<JSONObject,JSONObject> apiCmd, String comment) {
        this(cmdName, (p,q) -> apiCmd.apply(q), comment);
    }


    public String getName() {
        return name;
    }

    public Api getApiProcessor() {
        return apiProcessor;
    }

    public String getManual() {
        return manual;
    }


}
