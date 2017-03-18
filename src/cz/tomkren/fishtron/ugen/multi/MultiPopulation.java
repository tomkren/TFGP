package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.utils.TODO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Created by tom on 07.03.2017. */

public class MultiPopulation<Indiv extends MultiIndiv> {

    private final List<Boolean> isMaxis;

    private Set<Indiv> individuals;
    private Set<Indiv> removedIndividuals;
    private int numUniqueCheckFails;
    private List<Indiv> terminators;
    private Indiv worstIndividual;


    MultiPopulation(List<Boolean> isMaxis) {
        this.isMaxis = isMaxis;
        individuals = new HashSet<>();
        removedIndividuals = new HashSet<>();
        numUniqueCheckFails = 0;
        terminators = new ArrayList<>();
        worstIndividual = null;
    }

    Indiv select(MultiSelection<Indiv> selection) {
        return selection.select(individuals);
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

        worstIndividual = MultiUtils.assignFrontsAndDistances(individuals, isMaxis);

        return true;
    }


    void removeWorstIndividual() {
        individuals.remove(worstIndividual);
        removedIndividuals.add(worstIndividual);
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
