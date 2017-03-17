package cz.tomkren.fishtron.ugen.multi.gpml;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.multi.MultiEvaOpts;
import cz.tomkren.fishtron.ugen.multi.MultiEvolution;
import cz.tomkren.fishtron.ugen.multi.MultiLogger;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


/**Created by tom on 07.03.2017.*/

public class MultiGPML {

    private static final String version = "4.0.1";

    private static void run(String jsonConfigFilename, String logPath) throws JSONException, IOException, XmlRpcException {
        Log.it("Program arguments:");
        Log.it("  jsonConfigFileName : " + jsonConfigFilename);
        Log.it("  logPath            : " + logPath);
        Log.it();

        String configStr = Files.toString(new File(jsonConfigFilename), Charsets.UTF_8);
        Log.itln(jsonConfigFilename +" = "+ configStr);
        JSONObject config = new JSONObject(configStr);

        Checker checker = Checker.mk(config);

        MultiEvaSetup setup = new MultiEvaSetup(config, checker);
        MultiEvaOpts<AppTreeMI> opts = setup.getOpts();
        MultiLogger<AppTreeMI> dagLogger = new DagMultiLogger(config, logPath, checker, opts);

        Log.it("Config [OK] ...");

        MultiEvolution<AppTreeMI> eva = new MultiEvolution<>(opts, dagLogger);
        eva.startIterativeEvolution(1);

        if (config.getBoolean("killServer")) {
            String quitMsg = setup.quitServer();
            Log.it("\n\n");
            Log.it("Killing server, server kill response: "+ quitMsg);
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
            Log.err("JSON error:  " + e.getMessage());
            throw new Error(e);
        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (XmlRpcException e) {

            Log.it("Dag-evaluate server error: Server is probably not running, or it is starting right now..");

            int maxTries = 10;
            if (numRestarts < maxTries) {
                numRestarts ++;
                long sleepTime = 6000;
                Log.it("Sleeping for "+ (sleepTime/1000) +" seconds ... (try "+numRestarts+"/"+maxTries+")");
                F.sleep(sleepTime);
                main(args);
            } else {
                Log.it("Shutting down.");
            }

        }
    }

}
