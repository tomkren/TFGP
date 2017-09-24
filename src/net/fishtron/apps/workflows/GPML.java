package net.fishtron.apps.workflows;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;

import net.fishtron.eva.simple.Logger;
import net.fishtron.eva.simple.Evolution;
import net.fishtron.eva.simple.EvolutionOpts;
import net.fishtron.eva.simple.AppTreeIndiv;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;

/** Aka DagEva3
 * Created by tom on 17. 2. 2017. */

public class GPML {

    private static final String version = "1.0.6";

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

    private static int numRestarts = 0;

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

            Log.it("Dag-evaluate server error: Server is probably not running, or it is starting right now..");

            int maxTries = 10;
            if (numRestarts < maxTries) {
                numRestarts ++;
                long sleepTime = 6000;
                Log.it("Sleeping for "+ (sleepTime/1000) +" seconds... (try "+numRestarts+"/"+maxTries+")");
                F.sleep(sleepTime);
                main(args);
            } else {
                Log.it("Shutting down.");
            }

        }
    }

}
