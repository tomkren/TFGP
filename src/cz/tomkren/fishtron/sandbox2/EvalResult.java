package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;

import java.util.List;

/** Created by user on 9. 6. 2016. */

public interface EvalResult<Indiv extends FitIndiv> {

    List<Indiv> getSomeEvaluatedIndividuals();

    default int getNumRequestedIndividuals() {
        return getSomeEvaluatedIndividuals().size();
    }

}
