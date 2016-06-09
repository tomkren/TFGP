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

    public void run() {

        numSentIndividuals = 0;
        numEvaluatedIndividuals = 0;

        makeEmptyPopulation();

        List<EvalResult<Indiv>> someEvalResults = sendToEval(generateInitialIndividuals());
        insertToPopulation(someEvalResults);

        while (isEvaluationNeeded()) {
            boolean doMakeChildren = isPopulationLargeEnough() && isSendingNeeded();
            someEvalResults = doMakeChildren ? sendToEval(makeChildren()) : justAskForResults();
            insertToPopulation(someEvalResults);
        }
    }

    private void makeEmptyPopulation() {
        population = new Population<>(opts.performUniquenessCheck());
    }

    private List<Indiv> generateInitialIndividuals() {
        int numIndividualsToGenerate = Math.min(opts.getEvalPoolSize(), opts.getNumEvaluations());
        return opts.getGenerator().generate(numIndividualsToGenerate);
    }

    private boolean isEvaluationNeeded() {
        return numEvaluatedIndividuals < opts.getNumEvaluations();
    }

    private boolean isPopulationLargeEnough() {
        return population.size() >= opts.getMinPopulationSizeToOperate();
    }

    private boolean isSendingNeeded() {
        return numSentIndividuals < opts.getNumEvaluations();
    }

    private List<EvalResult<Indiv>> sendToEval(List<Indiv> indivs) {
        numSentIndividuals += indivs.size();
        return opts.evalIndividuals(indivs);
    }

    private List<EvalResult<Indiv>> justAskForResults() {
        return opts.evalIndividuals(Collections.emptyList());
    }

    private void insertToPopulation(List<EvalResult<Indiv>> evalResults) {

        if (evalResults.isEmpty()) {
            throw new Error("evalResults is expected to be not empty.");
        }

        numEvaluatedIndividuals += evalResults.size();

        for (EvalResult<Indiv> res : evalResults) {
            population.addIndividual(res.getIndividual());
            if (population.size() > opts.maxPopulationSize()) {
                population.removeWorstIndividual();
            }
        }
    }

    private List<Indiv> selectParents(int numParents) {
        List<Indiv> parents = new ArrayList<>(numParents);

        Distribution<Indiv> popDistrib = population.getDistribution();
        for (int i=0; i<numParents; i++) {
            parents.add(opts.getSelection().select(popDistrib));
        }

        return parents;
    }

    private List<Indiv> makeChildren() {

        Operator<Indiv> operator = opts.getOperators().get(opts.getRandom());
        List<Indiv> parents = selectParents(operator.getNumInputs());
        List<Indiv> children = operator.operate(parents);

        int numChildren = Math.min(children.size(), opts.getNumEvaluations() - numSentIndividuals );
        return F.take(numChildren, children);
    }






}
