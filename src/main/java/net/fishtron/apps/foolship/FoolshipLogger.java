package net.fishtron.apps.foolship;

import net.fishtron.eva.multi.*;
import net.fishtron.utils.Checker;
import org.json.JSONObject;

/**
 * Created by tom on 09.10.2017.
 */
public class FoolshipLogger /*implements MultiLogger<AppTreeMI>*/ {

    private static final String logPath = "eva/foolship/evaLog";


    public static EvaLogger<AppTreeMI> mk(JSONObject config, Checker checker) {


        return new EvaLogger<>(config, logPath, checker, new FoolshipLogger.Shower(), null); // TODO new
    }

    /*@Override
    public void log(int run, int evalId, MultiEvalResult<AppTreeMI> evalResult) {
        throw new TODO();
    }*/


    private static class Shower implements IndivShower<AppTreeMI> {
        @Override
        public String indivToStdout(AppTreeMI appTreeMI) {
            return appTreeMI.getTree().toString();
        }
    }

}
