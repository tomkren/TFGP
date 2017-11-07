package net.fishtron.apps.cellplaza;

import net.fishtron.apps.cellplaza.v2.CellPlaza;
import net.fishtron.eva.compare.CompareEvolution;
import net.fishtron.eva.multi.*;
import net.fishtron.server.EvaServer;
import net.fishtron.server.OLD.EvaJob_OLD;
import net.fishtron.server.OLD.EvaJobProcess;
import net.fishtron.server.OLD.EvaServer_OLD;
import net.fishtron.server.OLD.jobs.Test;
import net.fishtron.server.api.Api;
import net.fishtron.server.jobs.EvaJob;
import net.fishtron.server.jobs.JobContainer;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/** Created by tom on 20.03.2017. */

public class CellEva implements EvaJob, EvaJob_OLD {
    private static final String version = "0.0.2";
    private static final String CELL_EVA_JOB_NAME = "CellEva";
    public static String getJobName() {return CELL_EVA_JOB_NAME;}


    private final CellEvaOpts ceOpts;
    private final Checker checker;
    //private CellEvalManager evalManager;
    private InteractiveComparator interactiveComparator;



    public CellEva(Object ceOptsObj) {
        this((CellEvaOpts) ceOptsObj);
    }

    private CellEva(CellEvaOpts ceOpts) {
        this.ceOpts = ceOpts;
        this.checker = Checker.mk(ceOpts.getConfig());
    }

    private void runEvolution(JSONObject jobOpts) {
        JSONObject config = ceOpts.getConfig();
        String logPath = ceOpts.getLogPath();

        EvaSetup_CellEva setup = new EvaSetup_CellEva(jobOpts, config, logPath, checker);
        interactiveComparator = setup.getInteractiveComparator();

        CompareEvolution<AppTreeMI> eva = new CompareEvolution<>(setup.getOpts(), setup.getLogger());
        eva.start();

        checker.results();
    }

    private void log(Object x) {checker.it(x);}
    private void log() {checker.it();}


    @Override
    public void runJob_OLD(JSONObject jobOpts, EvaJobProcess jobProcess) {


        checker.setLogFun(jobProcess::log);
        checker.setLogFun_noln(jobProcess::log_noln);

        log("Starting CellEva ...");
        log(ceOpts);
        log();

        try {

            runEvolution(jobOpts);

        } catch (Error e) {
            resolveError(e);
        }
    }

    @Override
    public JSONObject runJob(Config jobConfig, JobContainer jobContainer) {

        checker.setLogFun(jobContainer::log);
        checker.setLogFun_noln(jobContainer::log); // TODO nebude fungovat to _noln
        checker.setIsStopRequestedFun(jobContainer::isStopRequested);

        log("Starting CellEva (new) ...");
        log(ceOpts);
        log();

        try {

            runEvolution(jobConfig.getOpts());
            return Api.ok(Api.KEY_msg, "evolution finished");

        } catch (Error e) {
            resolveError(e);
            return Api.error(e.getMessage());
        }


    }

    private void resolveError(Error e) {
        e.printStackTrace();
        log();
        log(String.join("\n",F.map(e.getStackTrace(), StackTraceElement::toString)));
        log();
        log("MSG = "+e.getMessage());
        log();
        log("EXITING WITH ERROR !");
    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        if (interactiveComparator == null) {
            return InteractiveComparator.mkInitializingResponse();
        } else {
            return interactiveComparator.processApiCall(path, query);
        }
    }

    @Override
    public JSONObject processApiCall_OLD(JSONArray path, JSONObject query) {
        return processApiCall(path, query);
    }


    public static void main(String[] args) {
        Log.it("CellEva [v "+version+"]");
        Log.it();


        boolean useDefaultConfig = args.length < 1;
        String configPath = useDefaultConfig ? CellPlaza.CONFIG_PATH : args[0];

        boolean useDefaultLogPath = args.length < 2;
        String logPath = useDefaultLogPath ? CellPlaza.BASE_DIR+"/"+"evaLog" : args[1];

        try {
            JSONObject config = F.tryLoadJson(configPath);
            Log.it("config : "+config.toString());
            Log.it("logPath : "+logPath);
            Log.it();



            boolean runOnServer = config.optBoolean("runOnServer", true);

            CellEvaOpts ceOpts = new CellEvaOpts(config, logPath);

            if (runOnServer) {

                if (!true) {

                    EvaServer_OLD evaServer = new EvaServer_OLD(config.getJSONObject("evaServer"));
                    evaServer.addJobClass(CELL_EVA_JOB_NAME, CellEva.class, ceOpts);
                    evaServer.addJobClass("Test", Test.class);
                    evaServer.getJobManager().runJob(CELL_EVA_JOB_NAME, F.obj("plazaDir", "mini_50"));
                    evaServer.startServer();

                } else {


                    EvaServer evaServer = new EvaServer(config.getJSONObject("evaServer"));
                    evaServer.addJobClass(CELL_EVA_JOB_NAME, CellEva.class, ceOpts);

                    evaServer.startEvaServer();

                    EvaJob.Config jobConfig = new EvaJob.Config("cell_eva", CELL_EVA_JOB_NAME, F.obj("plazaDir", "mini_50"), 0, null, true);
                    evaServer.getJobManager().scheduleJob(jobConfig);

                }



            } else {
                new CellEva((Object)ceOpts).runEvolution(null);
            }




        } catch (IOException e) {
            Log.it(e);
            String configDescription = useDefaultConfig ? "the default" : "your custom";
            Log.it("Unable to load "+configDescription+" config file '"+configPath+"', please check the path.");
        }


    }

}
