package cz.tomkren.fishtron.ugen.server;

import org.json.JSONObject;

/** Created by tom on 05.03.2017. */

public interface EvaJob {
    void run(JSONObject jobOpts, EvaJobProcess jobProcess);
}
