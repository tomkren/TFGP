package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.utils.Checker;

import java.util.List;
import java.util.Random;

/**Created by tom on 07.03.2017.*/

public class BasicMultiEvaOpts<Indiv extends MultiIndiv> implements MultiEvaOpts<Indiv> {

    private final int numEvaluations;
    private final int numIndividualsToGenerate;
    private final int minPopulationSizeToOperate;
    private final int maxPopulationSize;
    private final int timeLimit;
    private final long sleepTime;
    private final IndivGenerator<Indiv> generator;
    //private final List<Boolean> isMaxims;
    //private final List<String> fitnessLabels;
    private final FitnessSignature fitnessSignature;
    private final MultiEvalManager<Indiv> evalManager;
    private final MultiSelection<Indiv> parentSelection;
    private final Distribution<Operator<Indiv>> operators;
    private final Checker checker;

    public BasicMultiEvaOpts(int numEvaluations, int numIndividualsToGenerate, int minPopulationSizeToOperate,
                             int maxPopulationSize, int timeLimit, long sleepTime,
                             IndivGenerator<Indiv> generator,
                             FitnessSignature fitnessSignature, /*List<Boolean> isMaxims, List<String> fitnessLabels,*/ MultiEvalManager<Indiv> evalManager,
                             MultiSelection<Indiv> parentSelection,
                             Distribution<Operator<Indiv>> operators,
                             Checker checker) {
        this.numEvaluations = numEvaluations;
        this.numIndividualsToGenerate = numIndividualsToGenerate;
        this.minPopulationSizeToOperate = minPopulationSizeToOperate;
        this.maxPopulationSize = maxPopulationSize;
        this.timeLimit = timeLimit;
        this.sleepTime = sleepTime;
        this.generator = generator;
        //this.isMaxims = isMaxims;
        //this.fitnessLabels = fitnessLabels;
        this.fitnessSignature = fitnessSignature;
        this.evalManager = evalManager;
        this.parentSelection = parentSelection;
        this.operators = operators;
        this.checker = checker;
    }

    @Override public int getNumEvaluations() {return numEvaluations;}
    @Override public int getMinPopulationSizeToOperate() {return minPopulationSizeToOperate;}
    @Override public int getNumIndividualsToGenerate() {return numIndividualsToGenerate;}
    @Override public int getMaxPopulationSize() {return maxPopulationSize;}
    @Override public int getTimeLimit() {return timeLimit;}
    @Override public long getSleepTime() {return sleepTime;}
    @Override public IndivGenerator<Indiv> getGenerator() {return generator;}
    //@Override public List<Boolean> getIsMaximizationList() {return fitnessSignature.getIsMaximizationList();}
    //@Override public List<String> getFitnessLabels() {return fitnessSignature.getFitnessLabels();}
    @Override public FitnessSignature getFitnessSignature() {return fitnessSignature;}
    @Override public MultiEvalManager<Indiv> getEvalManager() {return evalManager;}
    @Override public MultiSelection<Indiv> getParentSelection() {return parentSelection;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return operators;}
    @Override public Random getRandom() {return checker.getRandom();}
    @Override public Checker getChecker() {return checker;}
}
