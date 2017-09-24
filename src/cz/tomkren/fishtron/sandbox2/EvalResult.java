package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.List;

/** Created by user on 9. 6. 2016. */

public interface EvalResult<Indiv extends FitIndiv> {

    List<AB<Indiv,JSONObject>> getEvalResult();

    default List<Indiv> getIndividuals() {
        return F.map(getEvalResult(), AB::_1);
    }

    default List<Integer> getIndivIds() {
        return F.map(getEvalResult(), p -> p._2().getInt("id") );
    }

    default int getNumEvaluatedIndividuals() {
        return getIndividuals().size();
    }

    default boolean isEmpty() {
        return getIndividuals().isEmpty();
    }

    default AB<Indiv,JSONObject> getBestInResult() {
        AB<Indiv,JSONObject> best = null;
        double wBest = - Double.MAX_VALUE;
        for (AB<Indiv,JSONObject> r : getEvalResult()) {
            double w = r._1().getWeight();
            if (w > wBest) {
                wBest = w;
                best = r;
            }
        }
        return best;
    }

}
