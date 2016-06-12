package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.utils.F;

import java.util.*;

/** Created by tom on 9. 6. 2016
 * Synchronous version...
 */


public class IterativeEvolver<Indiv extends FitIndiv> {

    private EvolverOpts<Indiv> opts;
    private Population<Indiv> population;

    private int numSentIndividuals;
    private int numEvaluatedIndividuals;

    public IterativeEvolver(EvolverOpts<Indiv> opts) {
        this.opts = opts;
    }

    // todo ještě to zkontrolovat ....
    public void run() {
        checkOptions();

        numSentIndividuals = 0;
        numEvaluatedIndividuals = 0;
        makeEmptyPopulation();
        EvalResult<Indiv> evalResult = null;


        while (isEvaluationUnfinished()) {
            if (isGeneratingNeeded()) {
                evalResult = sendToEval(generateIndividuals(evalResult));
            } else if (isPopulationLargeEnoughForOperating() && isSendingNeeded()) {
                evalResult = sendToEval(makeChildren(evalResult));
            } else {
                evalResult = justAskForResults();
            }
            updatePopulation(evalResult);
        }

    }

    private void checkOptions() {
        if (opts.getNumIndividualsToGenerate() < opts.getMinPopulationSizeToOperate()) {
            throw new Error("Evolver Options Check FAIL: numIndividualsToGenerate < minPopulationSizeToOperate");
        }
    }
    
    private void makeEmptyPopulation() {
        population = new Population<>(opts.isUniquenessCheckPerform());
    }

    private List<Indiv> generateIndividuals(EvalResult<Indiv> evalResult) {
        int yetToGenerate = opts.getNumIndividualsToGenerate() - numSentIndividuals;
        int evaluatorCapabilities = evalResult == null ? opts.getEvalManager().getEvalPoolSize(yetToGenerate) : evalResult.getNumRequestedIndividuals();
        int numToGenerate = Math.min(evaluatorCapabilities, yetToGenerate);
        return opts.getGenerator().generate(numToGenerate);
    }

    private boolean isEvaluationUnfinished()              {return numEvaluatedIndividuals < opts.getNumEvaluations();}
    private boolean isGeneratingNeeded()                  {return numSentIndividuals < opts.getNumIndividualsToGenerate();}
    private boolean isPopulationLargeEnoughForOperating() {return numEvaluatedIndividuals >= opts.getMinPopulationSizeToOperate();}
    private boolean isSendingNeeded()                     {return numSentIndividuals < opts.getNumEvaluations();}

    private EvalResult<Indiv> sendToEval(List<Indiv> indivs) {
        numSentIndividuals += indivs.size();
        return opts.getEvalManager().evalIndividuals(indivs);
    }

    private EvalResult<Indiv> justAskForResults() {
        return opts.getEvalManager().justAskForResults();
    }

    private void updatePopulation(EvalResult<Indiv> evalResult) {

        List<Indiv> evaluatedIndividuals = evalResult.getSomeEvaluatedIndividuals();

        numEvaluatedIndividuals += evaluatedIndividuals.size();

        for (Indiv indiv : evaluatedIndividuals) {
            population.addIndividual(indiv);
            if (population.size() > opts.getMaxPopulationSize()) {
                population.removeWorstIndividual();
            }
        }
    }

    private List<Indiv> makeChildren(EvalResult<Indiv> evalResult) {

        int requestedByEvaluator = evalResult.getNumRequestedIndividuals();
        int yetToBeSent = opts.getNumEvaluations() - numSentIndividuals;
        int numChildren = Math.min(requestedByEvaluator, yetToBeSent);

        List<Indiv> children = new ArrayList<>();

        while (children.size() < numChildren) {
            Operator<Indiv> operator = opts.getOperators().get(opts.getRandom());
            List<Indiv> parents = selectParents(operator.getNumInputs());
            children.addAll(operator.operate(parents));
        }

        return F.take(numChildren, children);
    }

    private List<Indiv> selectParents(int numParents) {
        List<Indiv> parents = new ArrayList<>(numParents);

        Distribution<Indiv> popDistribution = population.getDistribution();
        for (int i=0; i<numParents; i++) {
            parents.add(opts.getParentSelection().select(popDistribution));
        }

        return parents;
    }





}
