package cz.tomkren.fishtron.sandbox2;

import net.fishtron.eva.simple.FitIndiv;

/** Created by tom on 2. 6. 2016.*/

interface EvalCallback<Indiv extends FitIndiv> {

    void handleEvalResult(Indiv indiv);
    void handleEvalError(Indiv indiv, Throwable error);

}
