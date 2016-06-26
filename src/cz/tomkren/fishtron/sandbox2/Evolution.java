package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.Logger;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.utils.F;

import java.util.*;
import java.util.function.Function;

/** Created by tom on 9. 6. 2016
 * ...Synchronous version...
 */


public class Evolution<Indiv extends FitIndiv> {

    private EvolutionOpts<Indiv> opts;
    private Logger<Indiv> logger;

    private Population<Indiv> population;
    private int numSentIndividuals;
    private int numEvaluatedIndividuals;

    public Evolution(EvolutionOpts<Indiv> opts, Logger<Indiv> logger) {
        this.opts = opts;
        this.logger = logger;
    }

    // todo ještě to zkontrolovat ....

    // -- ITERATIVE EVOLUTION ------------------------------------------------------------------------

    public void startIterativeEvolution(int run) {
        checkOptions_IE();

        makeEmptyPopulation();
        EvalResult<Indiv> evalResult = null;
        long sleepTime = 2000; // TODO dát do configu...

        while (isEvaluationUnfinished_IE()) {
            if (isGeneratingNeeded_IE()) {
                evalResult = sendToEval(generateIndividuals_max(evalResult));
            } else if (isPopulationLargeEnoughForOperating_IE() && isSendingNeeded_IE()) {

                List<Indiv> children = makeChildren(evalResult,population,opts.getNumEvaluations());
                evalResult = children.size() > 0 ? sendToEval(children) : justAskForResults();

            } else {
                evalResult = justAskForResults();
            }

            updatePopulation_IE(evalResult);
            logger.iterativeLog(run, numEvaluatedIndividuals, population, evalResult);

            if (evalResult.isEmpty()) {
                sleep(sleepTime);
            }

        }

    }




    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void startIterativeEvolution_old(int run) {
        checkOptions_IE();

        makeEmptyPopulation();
        EvalResult<Indiv> evalResult = null;

        while (isEvaluationUnfinished_IE()) {
            if (isGeneratingNeeded_IE()) {
                evalResult = sendToEval(generateIndividuals_min(evalResult));
            } else if (isPopulationLargeEnoughForOperating_IE() && isSendingNeeded_IE()) {
                evalResult = sendToEval(makeChildren(evalResult,population,opts.getNumEvaluations()));
            } else {
                evalResult = justAskForResults();
            }

            updatePopulation_IE(evalResult);
            logger.iterativeLog(run, numEvaluatedIndividuals, population, evalResult);
        }

    }

    private void checkOptions_IE() {
        if (opts.getNumIndividualsToGenerate() < opts.getMinPopulationSizeToOperate()) {
            throw new Error("Evolver Options Check FAIL: numIndividualsToGenerate < minPopulationSizeToOperate");
        }
    }

    private boolean isGeneratingNeeded_IE() {return numSentIndividuals < opts.getNumIndividualsToGenerate();}
    private boolean isEvaluationUnfinished_IE() {return numEvaluatedIndividuals < opts.getNumEvaluations();}
    private boolean isSendingNeeded_IE() {return numSentIndividuals < opts.getNumEvaluations();}
    private boolean isPopulationLargeEnoughForOperating_IE() {
        return numEvaluatedIndividuals >= opts.getMinPopulationSizeToOperate();
    }

    private void updatePopulation_IE(EvalResult<Indiv> evalResult) {
        List<Indiv> evaluatedIndividuals = evalResult.getIndividuals();
        numEvaluatedIndividuals += evaluatedIndividuals.size();
        for (Indiv indiv : evaluatedIndividuals) {
            population.addIndividual(indiv);
            if (population.size() > opts.getMaxPopulationSize()) {
                population.removeWorstIndividual();
            }
        }
    }

    // -- GENERATIONS EVOLUTION -------------------------------------------------------------

    public void startGenerationsEvolution(int run) {
        checkOptions_GE();

        int gen = 0;
        int numGens = getNumGenerations_GE();

        EvalResult<Indiv> evalResult = generateGeneration_GE();
        logger.logPop(run, gen, population);

        for (gen = 1; gen < numGens; gen++) {

            evalResult = transformGeneration_GE(evalResult);
            logger.logPop(run, gen, population);
        }
    }

    private void checkOptions_GE() {
        if (opts.getNumEvaluations() % getGenerationSize_GE() != 0) {
            throw new Error("Generations Evolver Options Check FAIL: numEvaluations % generationSize != 0");
        }
    }

    private int getGenerationSize_GE() {return opts.getNumIndividualsToGenerate();}
    private int getNumGenerations_GE() {return opts.getNumEvaluations() / getGenerationSize_GE();}

    private EvalResult<Indiv> generateGeneration_GE() {
        makeEmptyPopulation();
        return fillPopulation_GE(null, this::generateIndividuals_min);
    }

    private EvalResult<Indiv> transformGeneration_GE(EvalResult<Indiv> evalResult) {

        Population<Indiv> parentPop = population;
        makeEmptyPopulation();

        if (opts.saveBest()) {
            population.addIndividual(parentPop.getBestIndividual());
            numSentIndividuals ++;
            numEvaluatedIndividuals ++;
        }

        return fillPopulation_GE(evalResult, eRes -> makeChildren(eRes, parentPop, getGenerationSize_GE()));
    }

    private EvalResult<Indiv> fillPopulation_GE(EvalResult<Indiv> evalResult, Function<EvalResult<Indiv>,List<Indiv>> mkIndivsBatch) {

        while (isBelowGenSize_GE(numEvaluatedIndividuals)) {

            if (isBelowGenSize_GE(numSentIndividuals)) {
                List<Indiv> indivsToEval = mkIndivsBatch.apply(evalResult);
                evalResult = sendToEval(indivsToEval);
            } else {
                evalResult = justAskForResults();
            }

            List<Indiv> evaledIndivs = evalResult.getIndividuals();
            numEvaluatedIndividuals += evaledIndivs.size();
            population.addIndividuals(evaledIndivs);
        }

        return evalResult;
    }

    private boolean isBelowGenSize_GE(int n) {
        return n < getGenerationSize_GE();
    }

    // -- GENERAL METHODS -------------------------------------------------------------------


    private void makeEmptyPopulation() {
        population = new Population<>(opts.isUniquenessCheckPerform());
        numSentIndividuals = 0;
        numEvaluatedIndividuals = 0;
    }

    private List<Indiv> generateIndividuals_max(EvalResult<Indiv> evalResult) {
        int yetToGenerate = opts.getNumIndividualsToGenerate() - numSentIndividuals;
        int evaluatorCapabilities = evalResult == null ? opts.getEvalManager().getEvalPoolSize(yetToGenerate)
                : evalResult.getNumEvaluatedIndividuals();
        int numToGenerate = Math.max(evaluatorCapabilities, yetToGenerate);
        return opts.getGenerator().generate(numToGenerate);
    }

    private List<Indiv> generateIndividuals_min(EvalResult<Indiv> evalResult) {
        int yetToGenerate = opts.getNumIndividualsToGenerate() - numSentIndividuals;
        int evaluatorCapabilities = evalResult == null ? opts.getEvalManager().getEvalPoolSize(yetToGenerate)
                                                       : evalResult.getNumEvaluatedIndividuals();
        int numToGenerate = Math.min(evaluatorCapabilities, yetToGenerate);
        return opts.getGenerator().generate(numToGenerate);
    }


    private EvalResult<Indiv> sendToEval(List<Indiv> indivs) {
        numSentIndividuals += indivs.size();
        return opts.getEvalManager().evalIndividuals(indivs);
    }

    private EvalResult<Indiv> justAskForResults() {
        return opts.getEvalManager().justAskForResults();
    }

    private List<Indiv> makeChildren(EvalResult<Indiv> evalResult, Population<Indiv> parentPop, int maxNumToMake) {

        int requestedByEvaluator = evalResult.getNumEvaluatedIndividuals();
        int yetToBeSent = maxNumToMake - numSentIndividuals;
        int numChildren = Math.min(requestedByEvaluator, yetToBeSent);

        List<Indiv> children = new ArrayList<>();

        while (children.size() < numChildren) {
            Operator<Indiv> operator = opts.getOperators().get(opts.getRandom());
            List<Indiv> parents = selectParents(operator.getNumInputs(), parentPop);
            children.addAll(operator.operate(parents));
        }

        return F.take(numChildren, children);
    }

    private List<Indiv> selectParents(int numParents, Population<Indiv> parentPop) {
        List<Indiv> parents = new ArrayList<>(numParents);

        Distribution<Indiv> popDistribution = parentPop.getDistribution();
        for (int i=0; i<numParents; i++) {
            parents.add(opts.getParentSelection().select(popDistribution));
        }

        return parents;
    }





}
