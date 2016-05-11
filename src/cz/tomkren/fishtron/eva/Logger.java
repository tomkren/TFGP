package cz.tomkren.fishtron.eva;

import cz.tomkren.utils.Log;
import cz.tomkren.utils.Weighted;

import java.util.List;

public interface Logger<Indiv extends Weighted> {
    void logPop(int run, int generation, EvaledPop<Indiv> pop);
    void logErrorIndivs(int generation, List<Object> errorIndivs);
    default void logRun(int run) {}


    class Basic<Indiv extends Weighted> implements Logger<Indiv> {

        @Override
        public void logRun(int run) {
            Log.it("-- RUN "+run+" FINISHED -----------------------");
        }

        @Override
        public void logPop(int run, int generation, EvaledPop<Indiv> pop) {
            Indiv best = pop.getBestIndividual();
            Log.it("gen "+generation+", best "+best.getWeight());
            Log.itln("  "+best.toString());
        }

        @Override
        public void logErrorIndivs(int generation, List<Object> errorIndivs) {

            System.err.println("ERROR INDIVS !!!");

            for (Object errInd: errorIndivs) {
                System.err.println("  "+errInd.toString());
            }


        }
    }

}
