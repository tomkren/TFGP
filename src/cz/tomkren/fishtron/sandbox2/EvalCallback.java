package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.utils.Weighted;

/** Created by tom on 2. 6. 2016.*/

interface EvalCallback<Indiv extends FitIndiv> {

    void handleEvalResult(Indiv indiv);
    void handleEvalError(Indiv indiv, Throwable error);

}
