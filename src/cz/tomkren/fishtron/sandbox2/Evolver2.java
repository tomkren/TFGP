package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;

import java.util.Random;

/** Created by tom on 1. 6. 2016. */

public class Evolver2<Indiv extends FitIndiv> implements PopulationSolver2<Indiv> {

    private Random rand;

    private int numEvaluations;
    private int popSize;
    private Selection<Indiv> selection;
    private Distribution<Operator<Indiv>> operators;
    private Logger<Indiv> logger;

    private IndivGenerator<Indiv> generator;



    @Override public Random getRandom() {return rand;}

    @Override public int getPopSize() {return popSize;}
    @Override public int getNumEvaluations() {return numEvaluations;}
    @Override public Selection<Indiv> getSelection() {return selection;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return operators;}
    @Override public Logger<Indiv> getLogger() {return logger;}

    @Override public IndivGenerator<Indiv> getGenerator() {return generator;}

}
