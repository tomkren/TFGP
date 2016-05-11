package cz.tomkren.fishtron.eva;

public interface FitFun {

    FitVal getFitVal(Object o);

    default void    initGeneration(int gen) {}
    default boolean doRecomputeFitVal() {return false;}
}
