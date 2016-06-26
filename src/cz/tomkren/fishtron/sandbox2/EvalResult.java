package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import java.util.List;

/** Created by user on 9. 6. 2016. */

public interface EvalResult<Indiv extends FitIndiv> {

    List<AB<Integer,Indiv>> getEvalResult();

    default List<Indiv> getIndividuals() {
        return F.map(getEvalResult(), AB::_2);
    }

    default List<Integer> getIndivIds() {
        return F.map(getEvalResult(), AB::_1);
    }

    default int getNumEvaluatedIndividuals() {
        return getIndividuals().size();
    }

    default boolean isEmpty() {
        return getIndividuals().isEmpty();
    }

    default AB<Integer,Indiv> getBestInResult() {
        AB<Integer,Indiv> best = null;
        double wBest = - Double.MAX_VALUE;
        for (AB<Integer,Indiv> r : getEvalResult()) {
            double w = r._2().getWeight();
            if (w > wBest) {
                wBest = w;
                best = r;
            }
        }
        return best;
    }

}
