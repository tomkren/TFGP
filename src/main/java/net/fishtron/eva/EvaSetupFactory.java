package net.fishtron.eva;

import net.fishtron.apps.cellplaza.CellEvaOpts;
import net.fishtron.apps.cellplaza.EvaSetup_CellEva;
import net.fishtron.apps.foolship.FoolshipSetup;
import net.fishtron.apps.tomkraft.TomkraftSetup;
import net.fishtron.server.api.Api;
import net.fishtron.server.api.Configs;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Either;
import org.json.JSONObject;

/**
 * Created by tom on 09.10.2017.
 */
public class EvaSetupFactory {

    private static final String KEY_setup = "setup";

    public static Either<EvaSetup,JSONObject> mk(JSONObject jobConfigOpts, Checker checker) {
        String setupName = Configs.get_String(jobConfigOpts, KEY_setup, null);
        if (setupName == null) {return Either.ko(Api.error("EvaSetupFactory: Missing or null key '"+KEY_setup+"'."));}

        switch (setupName) {
            case FoolshipSetup.SETUP_NAME:    return Either.ok(new FoolshipSetup(jobConfigOpts, checker));
            case EvaSetup_CellEva.SETUP_NAME: return mkCellplazaSetup(jobConfigOpts, checker);
            case TomkraftSetup.SETUP_NAME:    return Either.ok(new TomkraftSetup(jobConfigOpts, checker));
        }

        return Either.ko(Api.error("Unsupported "+KEY_setup+": '"+setupName+"'."));
    }

    private static Either<EvaSetup,JSONObject> mkCellplazaSetup(JSONObject jobConfigOpts, Checker checker) {
        return CellEvaOpts.mkDefault().ifOK2(cellEvaOpts ->
            new EvaSetup_CellEva(jobConfigOpts, cellEvaOpts.getConfig(), cellEvaOpts.getLogPath(), checker)
        );
    }


}
