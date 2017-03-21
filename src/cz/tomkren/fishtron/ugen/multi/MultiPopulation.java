package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.utils.TODO;

import java.util.*;

/** Created by tom on 07.03.2017. */

public class MultiPopulation<Indiv extends MultiIndiv> {

    private final List<Boolean> isMaxis;

    private Set<Indiv> individuals;
    private Set<Indiv> removedIndividuals;
    private int numUniqueCheckFails;
    private List<Indiv> terminators;
    private Indiv worstIndividual;


    MultiPopulation(List<Boolean> isMaxis) {
        if (isMaxis.isEmpty()) {throw new Error("0-fitness error : isMaxis is empty.");}

        this.isMaxis = isMaxis;
        individuals = new HashSet<>();
        removedIndividuals = new HashSet<>();
        numUniqueCheckFails = 0;
        terminators = new ArrayList<>();
        worstIndividual = null;
    }

    Indiv select(MultiSelection<Indiv> selection) {
        return selection.select(individuals, isMaxis);
    }

    // !!! TODO určitě předělat na addIndividuals, pač neefektivní vzledem k tomu že se furt přepočítávaj ty fronty !!! !!!

    boolean addIndividual(Indiv indiv) {

        // todo mělo by se kontrolovat ještě před evaluací!

        if (individuals.contains(indiv) || removedIndividuals.contains(indiv)) {
            numUniqueCheckFails ++;
            return false;
        }

        if (indiv.isTerminator()) {
            terminators.add(indiv);
        }

        individuals.add(indiv);


        if (isMaxis.size() > 1) {
            worstIndividual = MultiUtils.assignFrontsAndDistances(individuals, isMaxis);
        } else {
            worstIndividual = findWorstIndividual_singleFitness();
        }

        return true;
    }


    void removeWorstIndividual() {
        individuals.remove(worstIndividual);
        removedIndividuals.add(worstIndividual);
    }


    // TODO ověřit že max fakt dává nejhoršího :) !!!
    private Indiv findWorstIndividual_singleFitness() {
        Comparator<Indiv> singleComparator = (i1, i2) -> MultiIndiv.singleCompare(i1,i2,isMaxis.get(0));
        return individuals.stream().max(singleComparator).orElse(null);
    }




    public int size() {
        return individuals.size();
    }

    public int getNumUniqueCheckFails() {
        return numUniqueCheckFails;
    }

    public List<Indiv> getTerminators() {
        return terminators;
    }

    public Set<Indiv> getRemovedIndividuals() {
        return removedIndividuals;
    }

}
