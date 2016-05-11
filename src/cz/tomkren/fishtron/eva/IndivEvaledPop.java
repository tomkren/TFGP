package cz.tomkren.fishtron.eva;

import java.util.ArrayList;
import java.util.List;

/** Created by tom on 21.11.2015.*/

public class IndivEvaledPop<Indiv extends FitIndiv> implements EvaledPop<Indiv> {

    private Distribution<Indiv> popDist;
    private List<Indiv> terminators;

    public IndivEvaledPop(List<Indiv> pop, IndivFitFun<Indiv> fitness, int gen) {
        fitness.initGeneration(gen);
        terminators = new ArrayList<>();

        for (Indiv ind : pop) {

            FitVal fitVal = ind.getFitVal();
            if (fitVal == null || fitness.doRecomputeFitVal()) {
                fitVal = fitness.getIndivFitVal(ind);
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
