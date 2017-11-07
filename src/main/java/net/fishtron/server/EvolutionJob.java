package net.fishtron.server;

import net.fishtron.eva.Evolution;
import net.fishtron.eva.EvolutionFactory;
import net.fishtron.server.api.Api;
import net.fishtron.server.api.Configs;
import net.fishtron.server.jobs.EvaJob;
import net.fishtron.server.jobs.JobContainer;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by tom on 09.10.2017.
 */
public class EvolutionJob implements EvaJob {

    public static String getJobName() {return "EvolutionJob";}


    private static final String JCMD_getInfo = "getInfo";
    private static final String KEY_seed = "seed";

    private Checker checker;
    private Api subApi;


    @Override
    public JSONObject runJob(Config jobConfig, JobContainer jobContainer) {
        JSONObject opts = jobConfig.getOpts();

        Long seed = Configs.get_Long(opts, KEY_seed, null);
        checker = new Checker(seed, false, jobContainer::log, jobContainer::log_noln, jobContainer::isStopRequested);
        subApi = null;

        return EvolutionFactory.mk(opts, checker).ifOK(this::runEvolution);
    }

    private JSONObject runEvolution(Evolution eva) {
        subApi = eva.getApi();

        eva.startEvolution();

        checker.results();
        return Api.ok(Api.KEY_msg, "Successfully finished.");
    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        String jobCmd = query.optString(Api.KEY_jobCmd, null);
        switch (jobCmd) {
            case JCMD_getInfo: return getInfo();
            default:
                if (subApi != null) {
                    return subApi.processApiCall(path, query);
                } else {
                    return Api.error("Unsupported "+KEY_jobCmd+": "+ jobCmd);
                }
        }
    }

    public JSONObject getInfo() {
        JSONObject infoJson = subApi == null ? F.obj() : subApi.getInfo();
        infoJson.put(KEY_seed, String.valueOf(checker.getSeed()));
        return Api.addOk(infoJson);
    }

}
