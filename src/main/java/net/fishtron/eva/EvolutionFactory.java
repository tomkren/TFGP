package net.fishtron.eva;

import net.fishtron.eva.compare.CompareEvaSetup;
import net.fishtron.eva.compare.CompareEvolution;
import net.fishtron.eva.multi.MultiEvaSetup;
import net.fishtron.eva.multi.MultiEvolution;
import net.fishtron.server.api.Api;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Either;
import org.json.JSONObject;

/**
 * Created by tom on 28.10.2017.
 */
public class EvolutionFactory {


    public static Either<Evolution,JSONObject> mk(JSONObject jobConfigOpts, Checker checker) {
        return EvaSetupFactory.mk(jobConfigOpts, checker).bind(EvolutionFactory::mk);
    }

    public static Either<Evolution,JSONObject> mk(EvaSetup setup) {
        if (setup instanceof MultiEvaSetup) {
            return Either.ok(mkMultiEvolution((MultiEvaSetup) setup));
        } else if (setup instanceof CompareEvaSetup) {
            return Either.ok(mkCompareEvolution((CompareEvaSetup) setup));
        } else {
            return Either.ko(Api.error("Unsupported EvaSetup class: "+setup.getClass().getName()));
        }
    }

    private static Evolution mkMultiEvolution(MultiEvaSetup setup) {
        return new MultiEvolution<>(setup.getEvaOpts(), setup.getLogger());
    }

    private static Evolution mkCompareEvolution(CompareEvaSetup setup) {
        return new CompareEvolution<>(setup.getOpts(), setup.getLogger());
    }



}
