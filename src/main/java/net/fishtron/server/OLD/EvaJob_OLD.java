package net.fishtron.server.OLD;

import org.json.JSONObject;

/** Created by tom on 05.03.2017. */

public interface EvaJob_OLD extends Api_OLD {
    void runJob_OLD(JSONObject jobOpts, EvaJobProcess jobProcess);
}
