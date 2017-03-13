package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.fishtron.ugen.eval.EvalLib;

import java.util.List;

/**Created by tom on 06.03.2017.*/

public interface MultiIndiv {

    Object computeValue(EvalLib lib);
    Object getValue();

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



    static <Indiv extends MultiIndiv> int compare(Indiv i1, Indiv i2) {

        int front1 = i1.getFront();
        int front2 = i2.getFront();

        if (front1 < front2) {return -1;} // i1 wins
        if (front1 > front2) {return  1;} // i2 wins

        double dist1 = i1.getCrowdingDistance();
        double dist2 = i2.getCrowdingDistance();

        if (dist1 > dist2) {return -1;} // i1 wins
        if (dist1 < dist2) {return  1;} // i2 wins

        return 0; // tie
    }

}
