package net.fishtron.server.jobs.templates;

import net.fishtron.server.api.Api;
import net.fishtron.server.jobs.EvaJob;
import net.fishtron.server.jobs.JobContainer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by tom on 08.10.2017.
 */
public class TestTroll implements EvaJob {

    public static String getJobName() {return "TestTroll";}

    private final Random rand;

    public TestTroll() {
        rand = new Random();
    }

    @Override
    public JSONObject runJob(Config jobConfig, JobContainer jobContainer) {

        if (rand.nextDouble() < 0.25) {
            throw new Error("Trololo!");
        }

        return Api.ok(Api.KEY_msg, "Všecko v klídku.");

    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        return Api.ok(Api.KEY_msg, "Ani slovo neřeknu. Nepamatujusi nic, nebyl jsem tam, nevim.");
    }
}
