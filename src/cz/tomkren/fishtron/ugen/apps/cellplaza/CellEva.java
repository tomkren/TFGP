package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.CellPlaza;
import cz.tomkren.fishtron.ugen.multi.*;
import cz.tomkren.fishtron.ugen.server.Api;
import cz.tomkren.fishtron.ugen.server.EvaJob;
import cz.tomkren.fishtron.ugen.server.EvaJobProcess;
import cz.tomkren.fishtron.ugen.server.EvaServer;
import cz.tomkren.fishtron.ugen.server.jobs.Test;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.function.Consumer;

/** Created by tom on 20.03.2017. */

public class CellEva implements EvaJob {
    private static final String version = "0.0.1";

    private final CellEvaOpts ceOpts;
    private final Checker checker;
    //private boolean runOnServer;


    public CellEva(Object ceOptsObj) {
        this((CellEvaOpts) ceOptsObj);
    }

    private CellEva(CellEvaOpts ceOpts) {
        this.ceOpts = ceOpts;
        this.checker = ceOpts.ch;
    }

    private void runEvolution() {
        JSONObject config = ceOpts.config;
        String logPath = ceOpts.logPath;

        EvaSetup_CellEva setup = new EvaSetup_CellEva(config, checker);
        MultiEvaOpts<AppTreeMI> opts = setup.getOpts();
        MultiLogger<AppTreeMI> logger = new EvaLogger<>(config, logPath, checker, opts, new CellShower());

        MultiEvolution<AppTreeMI> eva = new MultiEvolution<>(opts, logger);
        eva.startIterativeEvolution(1);
    }

    private void log(Object x) {checker.it(x);}
    private void log() {checker.it();}


    @Override
    public void runJob(JSONObject jobOpts, EvaJobProcess jobProcess) {

        checker.setLogFun(jobProcess::log);
        checker.setLogFun_noln(jobProcess::log_noln);

        log("Starting CellEva ...");
        log(ceOpts);
        log();

        try {

            runEvolution();

        } catch (Error e) {
            resolveError(e);
        }
    }

    private void resolveError(Error e) {
        e.printStackTrace();
        log();
        log(String.join("\n",F.map(e.getStackTrace(), StackTraceElement::toString)));
        log();
        log("e.getMessage = "+e.getMessage());
        log("e.getLocalizedMessage = "+e.getLocalizedMessage());
        log();
        log("EXITING WITH ERROR !");
    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        return Api.ok("msg","todo");
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

            Checker ch = Checker.mk(config);

            boolean runOnServer = config.optBoolean("runOnServer", true);

            CellEvaOpts ceOpts = new CellEvaOpts(config, logPath, false, ch);

            if (runOnServer) {

                EvaServer evaServer = new EvaServer(config.getJSONObject("evaServer"));
                evaServer.addJobClass("CellEva", CellEva.class, ceOpts);
                evaServer.addJobClass("Test", Test.class);
                evaServer.startServer();

            } else {
                new CellEva((Object)ceOpts).runEvolution();
            }

            ch.results();


        } catch (IOException e) {
            Log.it(e);
            String configDescription = useDefaultConfig ? "the default" : "your custom";
            Log.it("Unable to load "+configDescription+" config file '"+configPath+"', please check the path.");
        }


    }

    private static class CellEvaOpts {

        private final JSONObject config;
        private final String logPath;
        private final boolean runOnServer;
        private final Checker ch;

        CellEvaOpts(JSONObject config, String logPath, boolean runOnServer, Checker ch) {
            this.config = config;
            this.logPath = logPath;
            this.runOnServer = runOnServer;
            this.ch = ch;
        }

        @Override
        public String toString() {
            return "CellEvaOpts{" +
                    "config=" + config +
                    ", logPath='" + logPath + '\'' +
                    ", runOnServer=" + runOnServer +
                    ", ch=" + ch +
                    '}';
        }
    }

}
