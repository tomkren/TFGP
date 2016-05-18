package cz.tomkren.fishtron.apps;

import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.fishtron.eva.TogetherFitFun;
import cz.tomkren.fishtron.sandbox.JsonEvalInterface;
import cz.tomkren.utils.F;
import org.json.JSONArray;

import java.util.List;

/** Created by tom on 22.2.2016.*/

public class YampaAntFitness implements TogetherFitFun {

    // TODO
    //public enum AntMode {EAT, BUILD}

    private final JsonEvalInterface evaluator;
    private final int perfectScore;
    private final String evalMethodName;

    public YampaAntFitness() {this("evalAnts", "http://localhost:8080");}

    public YampaAntFitness(String evalMethodName, String serverURL) {
        this.evalMethodName = evalMethodName;
        evaluator = new JsonEvalInterface(serverURL);
        perfectScore = evaluator.getInt("perfectScore");
    }

    @Override
    public List<FitVal> getFitVals(List<Object> values) {
        JSONArray jsonIndivs = new JSONArray(values);
        List<Integer> scores = evaluator.eval(evalMethodName, jsonIndivs, x->(int)x);
        return F.map(scores, score -> new FitVal.Basic(score, false /*todo score == perfectScore*/ ));
    }

}
