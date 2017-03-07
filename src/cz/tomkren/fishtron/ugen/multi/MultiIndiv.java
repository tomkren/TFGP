package cz.tomkren.fishtron.ugen.multi;

import java.util.List;

/**Created by tom on 06.03.2017.*/

public interface MultiIndiv {

    List<Double> getFitness();

    int getId();
    void setId(int id);

    int getFront();
    void setFront(int frontNumber);

    double getCrowdingDistance();
    void setCrowdingDistance(double ssc);


    default boolean isTerminator() {
        return false;
    }

    default int getFitnessSize() {
        return getFitness().size();
    }

    default double getFitness(int i) {
        return getFitness().get(i);
    }

}
