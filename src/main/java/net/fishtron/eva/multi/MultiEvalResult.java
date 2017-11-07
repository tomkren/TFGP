package net.fishtron.eva.multi;

import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 07.03.2017. */

public interface MultiEvalResult<Indiv extends MultiIndiv> {

    List<AB<Indiv,JSONObject>> getEvalResult();

    default List<Indiv> getIndividuals() {
        return F.map(getEvalResult(), AB::_1);
    }

    default List<Integer> getIndivIds() {
        return F.map(getEvalResult(), p -> p._2().getInt("id"));
    }

    // meant to be overridden in evaluator
    default int getNumRequestedIndividualsByEvaluator() {
        return getIndividuals().size();
    }

    default int getNumEvaluatedIndividuals() {
        return getIndividuals().size();
    }

    default boolean isEmpty() {
        return getIndividuals().isEmpty();
    }

}
