package cz.tomkren.fishtron.eva;

import java.util.List;

/** Created by user on 1. 7. 2015. */

public interface TogetherFitFun {

    List<FitVal> getFitVals(List<Object> os);
    default void initGeneration(int gen) {}
    //default boolean doRecomputeFitVal() {return false;}
}
