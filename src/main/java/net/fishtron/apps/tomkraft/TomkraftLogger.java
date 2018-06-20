package net.fishtron.apps.tomkraft;

import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.eva.multi.EvaLogger;
import net.fishtron.eva.multi.IndivShower;
import net.fishtron.utils.Checker;
import org.json.JSONObject;

public class TomkraftLogger {

    private static final String logPath = "eva/tomkraft/evaLog"; // TODO neni potřeba něco udělat aby tohle bylo ok?

    public static EvaLogger<AppTreeMI> mk(JSONObject config, Checker checker) {
        return new EvaLogger<>(config, logPath, checker, new TomkraftLogger.Shower(), null); // TODO new
    }

    private static class Shower implements IndivShower<AppTreeMI> {
        @Override
        public String indivToStdout(AppTreeMI appTreeMI) {
            return appTreeMI.getTree().toString();
        }
    }

}
