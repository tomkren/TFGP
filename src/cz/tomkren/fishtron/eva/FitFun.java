package cz.tomkren.fishtron.eva;

import net.fishtron.eva.simple.FitVal;

public interface FitFun {

    FitVal getFitVal(Object o);

    default void    initGeneration(int gen) {}
    default boolean doRecomputeFitVal() {return false;}
}
