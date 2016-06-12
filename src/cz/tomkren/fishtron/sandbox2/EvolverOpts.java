package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;

import java.util.List;
import java.util.Random;

/** Created by tom on 1. 6. 2016.*/

interface EvolverOpts<Indiv extends FitIndiv> {

    // Parameters
    int getNumEvaluations();
    int getNumIndividualsToGenerate();
    int getMinPopulationSizeToOperate();
    int getMaxPopulationSize();
    boolean isUniquenessCheckPerform();

    // Generator
    IndivGenerator<Indiv> getGenerator();

    // Evaluation
    EvalManager<Indiv> getEvalManager();

    // Selection, operators etc.
    Selection<Indiv> getParentSelection();
    Distribution<Operator<Indiv>> getOperators();
    Random getRandom();

}
