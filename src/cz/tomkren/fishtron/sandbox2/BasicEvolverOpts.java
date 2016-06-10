package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;

import java.util.Random;

/** Created by tom on 1. 6. 2016. */

public abstract class BasicEvolverOpts<Indiv extends FitIndiv> implements EvolverOpts<Indiv> {

    private int numEvaluations ;
    private int minPopulationSize ;
    private int maxPopulationSize ;
    private boolean isUniquenessCheckPerformed ;
    private IndivGenerator<Indiv> generator;
    private EvalManager<Indiv> evalManager;
    private Selection<Indiv> parentSelection;
    private Distribution<Operator<Indiv>> operators;
    private Random rand;

    public BasicEvolverOpts(int numEvaluations, int minPopulationSize, int maxPopulationSize,
                            boolean isUniquenessCheckPerformed, IndivGenerator<Indiv> generator,
                            EvalManager<Indiv> evalManager, Selection<Indiv> parentSelection,
                            Distribution<Operator<Indiv>> operators, Random rand) {
        this.numEvaluations = numEvaluations;
        this.minPopulationSize = minPopulationSize;
        this.maxPopulationSize = maxPopulationSize;
        this.isUniquenessCheckPerformed = isUniquenessCheckPerformed;
        this.generator = generator;
        this.evalManager = evalManager;
        this.parentSelection = parentSelection;
        this.operators = operators;
        this.rand = rand;
    }

    @Override public int getNumEvaluations() {return numEvaluations;}
    @Override public int getMinPopulationSize() {return minPopulationSize;}
    @Override public int getMaxPopulationSize() {return maxPopulationSize;}
    @Override public boolean isUniquenessCheckPerform() {return isUniquenessCheckPerformed;}
    @Override public IndivGenerator<Indiv> getGenerator() {return generator;}
    @Override public EvalManager<Indiv> getEvalManager() {return evalManager;}
    @Override public Selection<Indiv> getParentSelection() {return parentSelection;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return operators;}
    @Override public Random getRandom() {return rand;}

}
