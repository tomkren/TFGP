package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.CellPlaza;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.multi.MultiEvaOpts;
import cz.tomkren.fishtron.ugen.multi.MultiEvolution;
import cz.tomkren.fishtron.ugen.multi.MultiLogger;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONObject;

import java.io.IOException;

/** Created by tom on 20.03.2017. */

public class CellEva {

    private static final String version = "0.0.1";

    private static void run(JSONObject config, String logPath, Checker ch) {

        EvaSetup_CellEva setup = new EvaSetup_CellEva(config, ch);
        MultiEvaOpts<AppTreeMI> opts = setup.getOpts();
        MultiLogger<AppTreeMI> logger = new CellEvaLogger(config, logPath, ch, opts);

        MultiEvolution<AppTreeMI> eva = new MultiEvolution<>(opts, logger);
        eva.startIterativeEvolution(1);
    }

    public static void main(String[] args) {
        Log.it("CellEva [v "+version+"]");
        Log.it();
        Checker ch = new Checker();

        boolean useDefaultConfig = args.length < 1;
        String configPath = useDefaultConfig ? CellPlaza.CONFIG_PATH : args[0];

        boolean useDefaultLogPath = args.length < 2;
        String logPath = useDefaultLogPath ? CellPlaza.BASE_DIR+"/"+"eva" : args[1];

        try {
            JSONObject config = F.tryLoadJson(configPath);
            Log.it("config : "+config.toString());
            Log.it("logPath : "+logPath);
            Log.it();

            run(config, logPath, ch);

        } catch (IOException e) {
            Log.it(e);
            String configDescription = useDefaultConfig ? "the default" : "your custom";
            ch.fail("Unable to load "+configDescription+" config file '"+configPath+"', please check the path.");
        }

        ch.results();
    }
}
