package net.fishtron.eva.compare;

import net.fishtron.server.api.Api;
import net.fishtron.utils.Distribution;
import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.eva.multi.MultiIndiv;
import net.fishtron.utils.Checker;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

/** Created by tom on 22.03.2017.*/

public class BasicCompareOpts<Indiv extends MultiIndiv> implements CompareOpts<Indiv> {

    private Function<List<Indiv>,Indiv> compareFun;

    private int numEvaluations;
    private int numIndividualsToGenerate;
    private int maxPopulationSize;
    private int timeLimit;
    private long sleepTime;
    private IndivGenerator<Indiv> generator;
    private CompareSelection<Indiv> parentSelection;
    private Distribution<Operator<Indiv>> operators;
    private Api api;
    private Checker checker;

    public BasicCompareOpts(
            Function<List<Indiv>,Indiv> compareFun,
            int numEvaluations, int numIndividualsToGenerate,
            int maxPopulationSize, int timeLimit, long sleepTime,
            IndivGenerator<Indiv> generator,
            CompareSelection<Indiv> parentSelection,
            Distribution<Operator<Indiv>> operators,
            Api api, // may be null
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
        this.api = api;
        this.checker = checker;
    }

    @Override public Indiv compareIndividuals(List<Indiv> indivs) {return compareFun.apply(indivs);}

    @Override public int getNumEvaluations() {return numEvaluations;}
    @Override public int getNumIndividualsToGenerate() {return numIndividualsToGenerate;}
    @Override public int getMaxPopulationSize() {return maxPopulationSize;}
    @Override public int getTimeLimit() {return timeLimit;}
    @Override public long getSleepTime() {return sleepTime;}
    @Override public IndivGenerator<Indiv> getGenerator() {return generator;}
    @Override public CompareSelection<Indiv> getParentSelection() {return parentSelection;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return operators;}
    @Override public Random getRandom() {return checker.getRandom();}
    @Override public Api getApi() {return api;}
    @Override public Checker getChecker() {return checker;}
}
