package cz.tomkren.fishtron.eva;

import net.fishtron.utils.Distribution;

import java.util.ArrayList;
import java.util.List;

/** Created by tom on 1. 7. 2015. */

public class BasicEvaledPop<Indiv extends FitIndiv> implements EvaledPop<Indiv> {

    private Distribution<Indiv> popDist;
    private List<Indiv> terminators;

    public BasicEvaledPop(List<Indiv> pop, FitFun fitness, int gen) {
        fitness.initGeneration(gen);
        terminators = new ArrayList<>();

        for (Indiv ind : pop) {

            FitVal fitVal = ind.getFitVal();
            if (fitVal == null || fitness.doRecomputeFitVal()) {
                fitVal = fitness.getFitVal(ind.computeValue());
                ind.setFitVal(fitVal);
            }

            if (fitVal.isOK()) {
                terminators.add(ind);
            }
        }

        popDist = new Distribution<>(pop);
    }



    @Override
    public boolean isTerminating() {
        return !terminators.isEmpty();
    }

    @Override
    public Indiv getBestIndividual() {
        return popDist.getBest();
    }

    @Override
    public Distribution<Indiv> getIndividuals() {
        return popDist;
    }

    @Override
    public List<Indiv> getTerminators() {
        return terminators;
    }

    @Override
    public String toString() {
        return popDist.toString();
    }
}
