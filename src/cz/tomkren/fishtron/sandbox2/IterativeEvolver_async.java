package cz.tomkren.fishtron.sandbox2;


import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.utils.Log;
import cz.tomkren.utils.TODO;


/** Created by user on 2. 6. 2016. */

public class IterativeEvolver_async<Indiv extends FitIndiv> implements EvalCallback<Indiv> {

    private EvolverOpts<Indiv> opts;
    private Population<Indiv> population;
    private int populationMaxSize;

    public IterativeEvolver_async(EvolverOpts<Indiv> opts) {
        this.opts = opts;
        populationMaxSize = opts.maxPopulationSize();
    }

    /*
    public void run() {

        // TODO : načíst numEvaluations a oříznout podle toho evalPoolSize

        population = new Population<>(opts.getPopSize());
        opts.getGenerator().generate(opts.getEvalPoolSize()).forEach(this::evalIndividual);
    }*/


    private void evalIndividual(Indiv indiv) {
        opts.evalIndividual_async(indiv, this);
    }

    @Override
    public void handleEvalResult(Indiv indiv) {
        population.addIndividual(indiv);
        if (population.size() > populationMaxSize) {
            population.removeWorstIndividual();
        }

        // TODO : rozhodnout se kdy provýst novej push, nejjednoduší je když mi příde eval result,
        // todo | tzn tady: (a to udělat jen když zbejvá ještě dost evaluací)

        throw new TODO();
    }


    @Override
    public void handleEvalError(Indiv indiv, Throwable error) {
        Log.err(error);
        throw new Error(error);
    }
}
