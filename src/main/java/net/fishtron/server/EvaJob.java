package net.fishtron.server;

import org.json.JSONObject;

/** Created by tom on 05.03.2017. */

public interface EvaJob extends Api {
    void runJob(JSONObject jobOpts, EvaJobProcess jobProcess);
}
