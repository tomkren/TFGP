package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;
import cz.tomkren.utils.Weighted;

import java.util.List;
import java.util.Random;

/** Created by tom on 1. 6. 2016.*/

interface PopulationSolver2<Indiv extends Weighted> {

    Random getRandom();
    int getNumEvaluations();
    int getPopSize();
    Selection<Indiv> getSelection();
    Distribution<Operator<Indiv>> getOperators();
    Logger<Indiv> getLogger();

    IndivGenerator<Indiv> getGenerator();


    default void startRun(int run) {

        Random rand = getRandom();

        int numEvaluations = getNumEvaluations();
        int popSize = getPopSize();
        Selection<Indiv> selection = getSelection();
        Distribution<Operator<Indiv>> operators = getOperators();
        Logger<Indiv> logger = getLogger();

        int gen = 0;
        List<Indiv> pop = getGenerator().generate(popSize);

        throw new Error("TODO");



    }


}
