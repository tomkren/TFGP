package cz.tomkren.fishtron.ugen.compare;

import cz.tomkren.fishtron.ugen.multi.MultiIndiv;

import java.util.*;

/**Created by tom on 22.03.2017.*/

class ComparePopulation<Indiv extends MultiIndiv> {

    private final int maxPopulationSize;

    private Set<Indiv> population;
    private Set<Indiv> removedIndividuals;
    private int numUniqueCheckFails;
    //private List<Indiv> terminators;


    ComparePopulation(int maxPopulationSize) {
        this.maxPopulationSize = maxPopulationSize;

        population = new HashSet<>();
        removedIndividuals = new HashSet<>();
        numUniqueCheckFails = 0;
        //terminators = new ArrayList<>();
    }

    Set<Indiv> getPopulation() {
        return population;
    }

    void addIndividuals(Collection<Indiv> indivs) {

        for (Indiv indiv : indivs) {
            addIndividual(indiv);
        }

        while (population.size() > maxPopulationSize) {
            removeWorstIndividual();
        }
    }


    private void addIndividual(Indiv indiv) {
        if (population.contains(indiv) || removedIndividuals.contains(indiv)) {
            numUniqueCheckFails ++;
            return;
        }

        /*if (indiv.isTerminator()) {
            terminators.add(indiv);
        }*/

        population.add(indiv);
    }

    private void removeWorstIndividual() {
        Optional<Indiv> maybeMaxLoser = population.stream().max(Comparator.comparingInt(MultiIndiv::getNumLosses));
        maybeMaxLoser.ifPresent(maxLoser -> {
            population.remove(maxLoser);
            removedIndividuals.add(maxLoser);
        });
    }





}
