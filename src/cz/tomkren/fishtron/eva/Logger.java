package cz.tomkren.fishtron.eva;

import cz.tomkren.fishtron.sandbox2.EvalResult;
import cz.tomkren.fishtron.sandbox2.EvolutionOpts;
import net.fishtron.eva.simple.FitIndiv;
import net.fishtron.utils.Log;
import org.json.JSONObject;

import java.util.List;

public interface Logger<Indiv extends FitIndiv> {
    void logPop(int run, int generation, EvaledPop<Indiv> pop);
    void logErrorIndivs(int generation, List<Object> errorIndivs);
    default void logRun(int run) {}
    default void iterativeLog(int run, int numEvaluatedIndivs, EvaledPop<Indiv> pop, EvalResult<Indiv> evalResult) {}

    default void logCreation(int indivId, JSONObject creationInfo) {}

    class Basic<Indiv extends FitIndiv> implements Logger<Indiv> {

        private EvolutionOpts<Indiv> opts;

        public Basic(EvolutionOpts<Indiv> opts) {
            this.opts = opts;
        }

        public Basic() {
            this(null);
        }

        @Override
        public void iterativeLog(int run, int numEvaluatedIndivs, EvaledPop<Indiv> pop, EvalResult<Indiv> evalResult) {

            Indiv best = pop.getBestIndividual();
            Log.it("eval # "+ numEvaluatedIndivs + (opts==null ? "" : " / "+opts.getNumEvaluations() ) +", best :"+best.getWeight());
            Log.itln("  "+best.toString());

        }

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
