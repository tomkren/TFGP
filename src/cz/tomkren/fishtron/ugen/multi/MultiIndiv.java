package cz.tomkren.fishtron.ugen.multi;

import java.util.List;

/**Created by tom on 06.03.2017.*/

public interface MultiIndiv {

    Object computeValue();

    List<Double> getFitness();
    void setFitness(List<Double> fitness);

    int getId();
    void setId(int id);

    int getFront();
    void setFront(int front);

    double getCrowdingDistance();
    void setCrowdingDistance(double crowdingDistance);


    default boolean isTerminator() {return false;} // todo asi lépe udělat

    default int getFitnessSize() {return getFitness().size();}

    default double getFitness(int i) {return getFitness().get(i);}

}
