package cz.tomkren.fishtron.ugen.server.jobs;

import cz.tomkren.fishtron.ugen.server.EvaJob;
import cz.tomkren.fishtron.ugen.server.EvaJobProcess;
import org.json.JSONObject;

/** Created by tom on 05.03.2017. */

public class Test implements EvaJob {

    @Override
    public void run(JSONObject jobOpts, EvaJobProcess jobProcess) {
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 1000000; j++) {
                if (i % 1000 == 42 && j % 100000 == 23) {
                    jobProcess.log(i+" "+j);
                }
            }
        }
        jobProcess.log("Poslušně hlásim finiš!");
    }

}
