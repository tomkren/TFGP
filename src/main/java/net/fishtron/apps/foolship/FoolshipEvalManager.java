package net.fishtron.apps.foolship;

import net.fishtron.eva.multi.evaluators.ServerEvalManager;
import net.fishtron.eval.EvalLib;
import net.fishtron.server.api.Api;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Either;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by tom on 09.10.2017.
 */
public class FoolshipEvalManager extends ServerEvalManager {

    private static final String KEY_treeID = "treeID"; // TODO replace with id (needs to be done in Unity also)
    private static final String KEY_DNA = "DNA"; // TODO replace with "code" possibly
    private static final String KEY_evalTime = "evalTime";
    private static final String KEY_score = "score";

    private final double evalTime;

    FoolshipEvalManager(EvalLib evalLib, Checker checker, double evalTime, int preferredBufferSize) {
        super(evalLib, checker, preferredBufferSize);
        this.evalTime = evalTime;
    }

    @Override
    public int getEvalPoolSize(int suggestedPoolSize) {
        // todo : revise whether this choice is optimal
        return suggestedPoolSize;
    }


    @Override
    protected JSONObject mkIndivDataToSubmit(Object indivValue, int id) {

        // TODO potenciálně může bejt i string ("one symbol implementation")

        if (indivValue instanceof JSONObject || indivValue instanceof JSONArray) {

            return F.obj(
                    KEY_treeID, id,
                    KEY_DNA, indivValue,
                    KEY_evalTime, evalTime
            );

        } else {
            throw new Error("indiv supposed to be JSONObject or JSONArray. toString ="+ indivValue.toString());
        }
    }

    @Override
    protected Either<AB<Integer,List<Double>>,JSONObject> reportQueryToEvalRes(JSONObject reportQuery) {

        int treeID = reportQuery.optInt(KEY_treeID, -1);
        if (treeID == -1) {
            return Either.ko(Api.error("Wrong format or unspecified "+KEY_treeID+".."));
        }

        double score = reportQuery.optDouble(KEY_score, -Double.MAX_VALUE);
        if (score == -Double.MAX_VALUE) {
            return Either.ko(Api.error("Wrong format or unspecified "+KEY_score+".."));
        }

        List<Double> scores = Collections.singletonList(score);
        return Either.ok(AB.mk(treeID, scores));
    }
}
