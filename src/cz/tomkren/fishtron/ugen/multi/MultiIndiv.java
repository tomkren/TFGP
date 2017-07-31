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

    void addWin();
    void addLoss();

    int getNumWins();
    int getNumLosses();


    // TODO | zefektivnit aby se nemuselo pokazdy kontrolovat kolikanasobna je to optimalizace atd...
    // todo | asi tak, že selekci předáme compare funkci kterou bude furt volat

    static <Indiv extends MultiIndiv> int compare(Indiv i1, Indiv i2, List<Boolean> isMaxis) {
        return (isMaxis.size() > 1) ? multiCompare(i1,i2) : singleCompare(i1,i2, isMaxis.get(0));
    }

    // TODO dát do multi utils, tady je to zapomenutý dost nešikovně !!!!!!!!!!!!!!!!!!
    static <Indiv extends MultiIndiv> int multiCompare(Indiv i1, Indiv i2) {
        int front1 = i1.getFront();
        int front2 = i2.getFront();

        if (front1 == 0) {throw new Error("Unassigned front for i1: "+i1+", something went terribly wrong.");}
        if (front2 == 0) {throw new Error("Unassigned front for i2: "+i2+", something went terribly wrong.");}

        if (front1 < front2) {return -1;} // i1 wins
        if (front1 > front2) {return  1;} // i2 wins

        double dist1 = i1.getCrowdingDistance();
        double dist2 = i2.getCrowdingDistance();

        if (dist1 > dist2) {return -1;} // i1 wins
        if (dist1 < dist2) {return  1;} // i2 wins

        if (i1.getId() > i2.getId()) {return -1;} // i1 wins (i.e. prefer younger solution)
        return 1; // i2 wins, since id1 == id2 implies i1 == i2, so no need for tie.
        //return 0; // tie
    }

    static <Indiv extends MultiIndiv> int singleCompare(Indiv i1, Indiv i2, boolean isMaximization) {

        double f1 = i1.getFitness(0);
        double f2 = i2.getFitness(0);

        if (isMaximization) {
            if (f1 > f2) {return -1;} // i1 wins
            if (f1 < f2) {return  1;} // i2 wins
        } else {
            if (f1 < f2) {return -1;} // i1 wins
            if (f1 > f2) {return  1;} // i2 wins
        }

        return 0; // tie
    }

}
