package net.fishtron.apps.tomkraft;

import net.fishtron.eva.multi.evaluators.ServerEvalManager;
import net.fishtron.eval.EvalLib;
import net.fishtron.server.api.Api;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Either;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class TomkraftEvalManager extends ServerEvalManager {

    private static final String KEY_id = "id";
    private static final String KEY_code = "code";
    private static final String KEY_score = "score";

    private Function<Object,Object> addJsonLambdas;

    TomkraftEvalManager(EvalLib evalLib, Checker checker, int preferredBufferSize, Function<Object,Object> addJsonLambdas) {
        super(evalLib, checker, preferredBufferSize);
        this.addJsonLambdas = addJsonLambdas;
    }

    @Override
    public int getEvalPoolSize(int suggestedPoolSize) {
        // todo : revise whether this choice is optimal
        return suggestedPoolSize;
    }

    @Override
    protected JSONObject mkIndivDataToSubmit(Object indivValue, int id) {

        return F.obj(
                KEY_id, id,
                KEY_code, addJsonLambdas.apply(indivValue)
        );

        // TODO : potenciálně může bejt i string, či číslo ("one symbol implementation") asi dát ček uplne pryč
        // TODO | vubec bych tuhle metodu zrušil a dal ji do rodiče s pevnym nazvosloví klíčů

        /*if (indivValue instanceof JSONObject || indivValue instanceof JSONArray) {

            return F.obj(
                    KEY_id, id,
                    KEY_code, indivValue
            );

        } else {
            throw new Error("indiv supposed to be JSONObject or JSONArray. toString ="+ indivValue.toString());
        }*/
    }

    @Override
    protected Either<AB<Integer,List<Double>>,JSONObject> reportQueryToEvalRes(JSONObject reportQuery) {

        int treeID = reportQuery.optInt(KEY_id, -1);
        if (treeID == -1) {
            return Either.ko(Api.error("Wrong format or unspecified "+KEY_id+".."));
        }

        double score = reportQuery.optDouble(KEY_score, -Double.MAX_VALUE);
        if (score == -Double.MAX_VALUE) {
            return Either.ko(Api.error("Wrong format or unspecified "+KEY_score+".."));
        }

        return Either.ok(AB.mk(treeID, Collections.singletonList(score)));
    }

}
