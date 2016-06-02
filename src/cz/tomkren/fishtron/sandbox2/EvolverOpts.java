package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;
import java.util.Random;

/** Created by tom on 1. 6. 2016.*/

interface EvolverOpts<Indiv extends FitIndiv> {

    Random getRandom();
    int getNumEvaluations();
    int getPopSize();
    int getEvalPoolSize();

    Selection<Indiv> getSelection();
    Distribution<Operator<Indiv>> getOperators();
    Logger<Indiv> getLogger();

    IndivGenerator<Indiv> getGenerator();

    void evalIndividual(Indiv indiv, EvalCallback<Indiv> callback);



    //default


}
