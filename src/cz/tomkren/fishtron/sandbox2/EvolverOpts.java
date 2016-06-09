package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;

import java.util.List;
import java.util.Random;

/** Created by tom on 1. 6. 2016.*/

interface EvolverOpts<Indiv extends FitIndiv> {

    Random getRandom();
    int getNumEvaluations();

    int maxPopulationSize();
    int getMinPopulationSizeToOperate();

    int getEvalPoolSize();

    boolean performUniquenessCheck();
    Selection<Indiv> getSelection();
    Distribution<Operator<Indiv>> getOperators();

    Logger<Indiv> getLogger();

    IndivGenerator<Indiv> getGenerator();

    void evalIndividual_async(Indiv indiv, EvalCallback<Indiv> callback);

    void evalIndividual(Indiv indiv);

    List<EvalResult<Indiv>> evalIndividuals(List<Indiv> indivs);


    //default


}
