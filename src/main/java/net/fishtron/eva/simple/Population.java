package net.fishtron.eva.simple;

import net.fishtron.utils.Distribution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Created by tom on 2. 6. 2016.*/

// todo : limit uniquenes fails!!!

public class Population<Indiv extends FitIndiv> implements EvaledPop<Indiv> {

    private Distribution<Indiv> individuals;

    private Set<Indiv> uniquenessSet;
    private int numUniqueCheckFails;

    private List<Indiv> terminators;

    public Population(boolean performUniquenessCheck) {

        individuals = new Distribution<>();

        uniquenessSet = performUniquenessCheck ? new HashSet<>() : null;
        numUniqueCheckFails = 0;

        terminators = new ArrayList<>();
    }

    public boolean addIndividual(Indiv indiv) {

        FitVal fitVal = indiv.getFitVal();

        if (fitVal == null) {throw new Error("Population.addInividual: individual must be evaluated!");}

        if (uniquenessSet != null) {
            if (uniquenessSet.contains(indiv)) {
                numUniqueCheckFails ++;
                return false;
            } else {
                uniquenessSet.add(indiv);
            }
        }

        if (fitVal.isOK()) {
            terminators.add(indiv);
        }

        individuals.add(indiv);
        return true;
    }

    public void addIndividuals(List<Indiv> indivs) {
        indivs.forEach(this::addIndividual);
    }


    public void removeWorstIndividual() {
        individuals.removeWorst();
    }

    public int size() {
        return individuals.size();
    }

    public int getNumUniqueCheckFails() {
        return numUniqueCheckFails;
    }


    public Distribution<Indiv> getDistribution() {
        return individuals;
    }


    @Override public boolean isTerminating() {return !terminators.isEmpty();}
    @Override public Indiv getBestIndividual() {return individuals.getBest();}
    @Override public Distribution<Indiv> getIndividuals() {return individuals;}
    @Override public List<Indiv> getTerminators() {return terminators;}

    @Override public String toString() {return individuals.toString();}

}
