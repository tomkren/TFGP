package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;

import java.util.List;
import java.util.Random;

/**Created by tom on 07.03.2017.*/

public class BasicMultiEvaOpts<Indiv extends MultiIndiv> implements MultiEvaOpts<Indiv> {

    private int numEvaluations;
    private int numIndividualsToGenerate;
    private int minPopulationSizeToOperate;
    private int maxPopulationSize;
    //private boolean saveBests;
    private int timeLimit;
    private long sleepTime;
    private IndivGenerator<Indiv> generator;
    private List<Boolean> isMaxims;
    private MultiEvalManager<Indiv> evalManager;
    private MultiSelection<Indiv> parentSelection;
    private Distribution<Operator<Indiv>> operators;
    private Random rand;

    public BasicMultiEvaOpts(int numEvaluations, int numIndividualsToGenerate, int minPopulationSizeToOperate,
                             int maxPopulationSize, /*boolean saveBests,*/ int timeLimit, long sleepTime,
                             IndivGenerator<Indiv> generator,
                             List<Boolean> isMaxims, MultiEvalManager<Indiv> evalManager,
                             MultiSelection<Indiv> parentSelection,
                             Distribution<Operator<Indiv>> operators,
                             Random rand) {
        this.numEvaluations = numEvaluations;
        this.numIndividualsToGenerate = numIndividualsToGenerate;
        this.minPopulationSizeToOperate = minPopulationSizeToOperate;
        this.maxPopulationSize = maxPopulationSize;
        //this.saveBests = saveBests;
        this.timeLimit = timeLimit;
        this.sleepTime = sleepTime;
        this.generator = generator;
        this.isMaxims = isMaxims;
        this.evalManager = evalManager;
        this.parentSelection = parentSelection;
        this.operators = operators;
        this.rand = rand;
    }

    @Override public int getNumEvaluations() {return numEvaluations;}
    @Override public int getMinPopulationSizeToOperate() {return minPopulationSizeToOperate;}
    @Override public int getNumIndividualsToGenerate() {return numIndividualsToGenerate;}
    @Override public int getMaxPopulationSize() {return maxPopulationSize;}
    //@Override public boolean saveBests() {return saveBests;}
    @Override public int getTimeLimit() {return timeLimit;}
    @Override public long getSleepTime() {return sleepTime;}
    @Override public IndivGenerator<Indiv> getGenerator() {return generator;}
    @Override public List<Boolean> getIsMaximizationList() {return isMaxims;}
    @Override public MultiEvalManager<Indiv> getEvalManager() {return evalManager;}
    @Override public MultiSelection<Indiv> getParentSelection() {return parentSelection;}
    @Override public Distribution<Operator<Indiv>> getOperators() {return operators;}
    @Override public Random getRandom() {return rand;}
}
