package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.Selection;
import cz.tomkren.utils.TODO;

/** Created by tom on 2. 6. 2016.*/

public class Population<Indiv extends FitIndiv> {

    Distribution<Indiv> individuals;

    public Population(int maxSize) {
        individuals = new Distribution<>();
    }

    public void addIndividual(Indiv indiv) {
        individuals.add(indiv);
    }

    public int size() {
        return individuals.size();
    }

    public void removeWorstIndividual() {
        individuals.removeWorst();
    }


}
