package net.fishtron.eva.compare;

import net.fishtron.server.api.Api;
import net.fishtron.utils.Distribution;
import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.eva.multi.MultiIndiv;
import net.fishtron.utils.Checker;

import java.util.Random;

/** Created by tom on 22.03.2017.*/

public class BasicCompareOpts<Indiv extends MultiIndiv> implements CompareOpts<Indiv> {

    private IndivComparator<Indiv> comparator;

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
            IndivComparator<Indiv> comparator,
            int numEvaluations, int numIndividualsToGenerate,
            int maxPopulationSize, int timeLimit, long sleepTime,
            IndivGenerator<Indiv> generator,
            CompareSelection<Indiv> parentSelection,
            Distribution<Operator<Indiv>> operators,
            Checker checker) {

        this.comparator = comparator;
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

    @Override public IndivComparator<Indiv> getComparator() {return comparator;}

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

    //@Override public Api getApi() {return comparator;}
}
