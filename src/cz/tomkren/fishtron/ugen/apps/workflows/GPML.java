package cz.tomkren.fishtron.ugen.apps.workflows;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;

import cz.tomkren.fishtron.eva.Logger;
import cz.tomkren.fishtron.sandbox2.Evolution;
import cz.tomkren.fishtron.sandbox2.EvolutionOpts;
import cz.tomkren.fishtron.ugen.eva.AppTreeIndiv;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;

/** Aka DagEva3
 * Created by tom on 17. 2. 2017. */

public class GPML {

    private static final String version = "1.0.3";

    private static void run(String jsonConfigFilename, String logPath) throws JSONException, IOException, XmlRpcException {
        Log.it("Program arguments:");
        Log.it("  jsonConfigFileName : " + jsonConfigFilename);
        Log.itln("  logPath            : " + logPath);

        String configStr = Files.toString(new File(jsonConfigFilename), Charsets.UTF_8);
        Log.itln(jsonConfigFilename +" = "+ configStr);
        JSONObject config = new JSONObject(configStr);

        Checker checker = Checker.mk(config);

        EvolutionSetup setup = new EvolutionSetup(config, checker);
        EvolutionOpts<AppTreeIndiv> opts = setup.getOpts();
        Logger<AppTreeIndiv> dagLogger = new DagLogger(config, logPath, checker, opts);

        Log.it("Config [OK] ...");

        Evolution<AppTreeIndiv> eva = new Evolution<>(opts, dagLogger);
        eva.startIterativeEvolution(1);

        if (config.getBoolean("killServer")) {
            String quitMsg = setup.quitServer();
            Log.it("\n\nKilling server, server kill response: "+ quitMsg);
        }

        checker.results();
    }

    public static void main(String[] args) {
        Log.it("GP-ML [v "+version+"]");

        if (args.length < 2 || args[0].equals("--help")) {
            Log.it("You must provide two program arguments: <json-config-filename> <log-dir-path>");
            return;
        }

        try {

            run(args[0], args[1]);

        } catch (JSONException e) {
            Log.err("JSON error: " + e.getMessage());
            throw new Error(e);
        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (XmlRpcException e) {
            long sleepTime = 5000;
            Log.it("Dag-evaluate server error: Server is probably not running, or it is starting right now..");
            Log.it("Sleeping for "+ (sleepTime/1000) +" seconds...");
            F.sleep(sleepTime);
            main(args);
        }
    }

}
