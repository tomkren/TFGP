package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.multi.MultiEvaOpts;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import cz.tomkren.fishtron.ugen.multi.MultiLogger;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.TODO;
import org.json.JSONObject;

/**Created by tom on 20.03.2017.*/

public class CellEvaLogger implements MultiLogger<AppTreeMI> {


    CellEvaLogger(JSONObject config, String logPath, Checker checker, MultiEvaOpts<AppTreeMI> opts) {
        throw new TODO();
    }

    @Override
    public void log(int run, int evalId, MultiEvalResult<AppTreeMI> evalResult) {
        throw new TODO();
    }
}
