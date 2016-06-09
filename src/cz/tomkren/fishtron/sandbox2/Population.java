package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.Selection;
import cz.tomkren.utils.TODO;

import java.util.HashSet;
import java.util.Set;

/** Created by tom on 2. 6. 2016.*/

public class Population<Indiv extends FitIndiv> {

    private Distribution<Indiv> individuals;
    private Set<Indiv> uniquenessSet;
    private int numUniqueCheckFails;

    public Population(boolean performUniquenessCheck) {

        individuals = new Distribution<>();
        uniquenessSet = performUniquenessCheck ? new HashSet<>() : null;
        numUniqueCheckFails = 0;
    }

    public boolean addIndividual(Indiv indiv) {

        if (uniquenessSet != null) {
            if (uniquenessSet.contains(indiv)) {
                numUniqueCheckFails ++;
                return false;
            } else {
                uniquenessSet.add(indiv);
            }
        }

        individuals.add(indiv);
        return true;
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
}
