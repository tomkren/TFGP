package cz.tomkren.fishtron.sandbox2;

import net.fishtron.eva.simple.FitIndiv;
import net.fishtron.utils.AB;
import net.fishtron.utils.Log;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/** Created by user on 10. 6. 2016. */

public interface EvalManager<Indiv extends FitIndiv> {

    EvalResult<Indiv> evalIndividuals(List<AB<Indiv,JSONObject>> indivs);


    default EvalResult<Indiv> justAskForResults() {
        Log.it("JUST ASKING !!! EMPTY --------------------------------------------------------------");
        return evalIndividuals(Collections.emptyList());
    }

    int getEvalPoolSize(int suggestedPoolSize);



}
