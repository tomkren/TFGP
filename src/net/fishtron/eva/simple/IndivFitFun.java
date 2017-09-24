package net.fishtron.eva.simple;


/** Created by tom on 21.11.2015.*/

public interface IndivFitFun<Indiv> {

    FitVal getIndivFitVal(Indiv indiv);

    default void    initGeneration(int gen) {}
    default boolean doRecomputeFitVal() {return false;}

}
