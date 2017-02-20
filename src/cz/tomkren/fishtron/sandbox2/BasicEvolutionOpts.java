package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;
import org.json.JSONObject;

import java.util.Random;

/** Created by tom on 1. 6. 2016. */

public class BasicEvolutionOpts<Indiv extends FitIndiv> implements EvolutionOpts<Indiv> {

    private int numEvaluations;
    private int minPopulationSizeToOperate;
    private int numIndividualsToGenerate;
    private int maxPopulationSize;
    private boolean isUniquenessCheckPerformed;
    private boolean saveBest;
    private int timeLimit;
    private IndivGenerator<Indiv> generator;
    private EvalManager<Indiv> evalManager;
    private Selection<Indiv> parentSelection;
    private Distribution<Operator<Indiv>> operators;
    private Random rand;

    public BasicEvolutionOpts(int numEvaluations, int minPopulationSizeToOperate, int numIndividualsToGenerate, int maxPopulationSize,
                              boolean isUniquenessCheckPerformed, boolean saveBest, int timeLimit,
                              IndivGenerator<Indiv> generator, EvalManager<Indiv> evalManager,
                              Selection<Indiv> parentSelection, Distribution<Operator<Indiv>> operators,
                              Random rand) {
        this.numEvaluations = numEvaluations;
        this.minPopulationSizeToOperate = minPopulationSizeToOperate;
        this.numIndividualsToGenerate = numIndividualsToGenerate;
        this.maxPopulationSize = maxPopulationSize;
        this.isUniquenessCheckPerformed = isUniquenessCheckPerformed;
        this.saveBest = saveBest;
        this.timeLimit = timeLimit;
        this.generator = generator;
        this.evalManager = evalManager;
        this.parentSelection = parentSelection;
        this.operators = operators;
        this.rand = rand;
    }

    @Override public int getNumEvaluations() {return numEvaluations;}
    @Override public int getMinPopulationSizeToOperate() {return minPopulationSizeToOperate;}
    @Override public int getNumIndividualsToGenerate() {return numIndividualsToGenerate;}
    @Override public int getMaxPopulationSize() {return maxPopulationSize;}
    @Override public boolean isUniquenessCheckPerform() {return isUniquenessCheckPerformed;}
    @Override public boolean saveBest() {return saveBest;}
    @Override public int getTimeLimit() {return timeLimit;}
    @Override public IndivGenerator<Indiv> getGenerator() {return generator;}
    @Override public EvalManager<Indiv> getEvalManager() {return evalManager;}
    @Override public Selection<Indiv> getParentSelection() {return parentSelection;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return operators;}
    @Override public Random getRandom() {return rand;}

}
