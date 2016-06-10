package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.utils.Log;

import java.util.Collections;
import java.util.List;

/** Created by user on 10. 6. 2016. */

public interface EvalManager<Indiv extends FitIndiv> {

    EvalResult<Indiv> evalIndividuals(List<Indiv> indivs);


    default EvalResult<Indiv> justAskForResults() {
        return evalIndividuals(Collections.emptyList());
    }

    int getEvalPoolSize(int suggestedPoolSize);



}