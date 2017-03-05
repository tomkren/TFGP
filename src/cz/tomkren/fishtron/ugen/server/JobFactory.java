package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.fishtron.ugen.server.jobs.Test;
import org.json.JSONObject;

/** Created by tom on 05.03.2017. */

public class JobFactory {

    public static EvaJob mkJob(String jobName) {


        if (jobName.equals("test")) {
            return new Test();
        }

        return null;
    }

}
