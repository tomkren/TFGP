package cz.tomkren.fishtron.ugen.compare;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.utils.Checker;

import java.util.Random;
import java.util.function.BiFunction;

/** Created by tom on 22.03.2017.*/

public class BasicCompareOpts<Indiv extends MultiIndiv> implements CompareOpts<Indiv> {

    private BiFunction<Indiv,Indiv,Boolean> compareFun;

    private int numEvaluations;
    private int numIndividualsToGenerate;
    private int maxPopulationSize;
    private int timeLimit;
    private long sleepTime;
    private IndivGenerator<Indiv> generator;
    private CompareSelection<Indiv> parentSelection;
    private Distribution<Operator<Indiv>> operators;
    private Checker checker;

    public BasicCompareOpts(
            BiFunction<Indiv,Indiv,Boolean> compareFun,
            int numEvaluations, int numIndividualsToGenerate,
            int maxPopulationSize, int timeLimit, long sleepTime,
            IndivGenerator<Indiv> generator,
            CompareSelection<Indiv> parentSelection,
            Distribution<Operator<Indiv>> operators,
            Checker checker) {

        this.compareFun = compareFun;
        this.numEvaluations = numEvaluations;
        this.numIndividualsToGenerate = numIndividualsToGenerate;
        this.maxPopulationSize = maxPopulationSize;
        this.timeLimit = timeLimit;
        this.sleepTime = sleepTime;
        this.generator = generator;
        this.parentSelection = parentSelection;
        this.operators = operators;
        this.checker = checker;
    }

    @Override public boolean compareIndividuals(Indiv i1, Indiv i2) {return compareFun.apply(i1,i2);}

    @Override public int getNumEvaluations() {return numEvaluations;}
    @Override public int getNumIndividualsToGenerate() {return numIndividualsToGenerate;}
    @Override public int getMaxPopulationSize() {return maxPopulationSize;}
    @Override public int getTimeLimit() {return timeLimit;}
    @Override public long getSleepTime() {return sleepTime;}
    @Override public IndivGenerator<Indiv> getGenerator() {return generator;}
    @Override public CompareSelection<Indiv> getParentSelection() {return parentSelection;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return operators;}
    @Override public Random getRandom() {return checker.getRandom();}
    @Override public Checker getChecker() {return checker;}
}
