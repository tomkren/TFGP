package cz.tomkren.fishtron.ugen.multi;

import net.fishtron.utils.AB;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 07.03.2017. */

public interface MultiEvalManager<Indiv extends MultiIndiv> {

    MultiEvalResult<Indiv> evalIndividuals(List<AB<Indiv,JSONObject>> indivs);
    MultiEvalResult<Indiv> justAskForResults();

    int getEvalPoolSize(int suggestedPoolSize);
}