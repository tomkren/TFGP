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

    public void run() { // TODO promyslet esli fakt funguje...

        numSentIndividuals = 0;
        numEvaluatedIndividuals = 0;

        makeEmptyPopulation();

        EvalResult<Indiv> evalResult;

        do {
            evalResult = sendToEval(generateIndividuals());
            updatePopulation(evalResult);
        } while(isGeneratingNeeded());

        while (isOperatingNeeded()) {
            boolean makeMoreChildren = isPopulationLargeEnough() && isSendingNeeded();
            evalResult = makeMoreChildren ? sendToEval(makeChildren(evalResult)) : justAskForResults();
            updatePopulation(evalResult);
        }


    }


    private void makeEmptyPopulation() {
        population = new Population<>(opts.isUniquenessCheckPerform());
    }

    private List<Indiv> generateIndividuals() {
        int yetToGenerate = opts.getMinPopulationSize() - numSentIndividuals;
        int numToGenerate = Math.min(opts.getEvalManager().getEvalPoolSize(yetToGenerate), yetToGenerate);
        return opts.getGenerator().generate(numToGenerate);
    }

    private boolean isGeneratingNeeded() {return numSentIndividuals < opts.getMinPopulationSize();}

    private boolean isOperatingNeeded() {return numEvaluatedIndividuals < opts.getNumEvaluations();}

    private boolean isSendingNeeded() {return numSentIndividuals < opts.getNumEvaluations();}

    private boolean isPopulationLargeEnough() {return numEvaluatedIndividuals >= opts.getMinPopulationSize();}

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

    private List<Indiv> selectParents(int numParents) {
        List<Indiv> parents = new ArrayList<>(numParents);

        Distribution<Indiv> popDistrib = population.getDistribution();
        for (int i=0; i<numParents; i++) {
            parents.add(opts.getParentSelection().select(popDistrib));
        }

        return parents;
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






}
