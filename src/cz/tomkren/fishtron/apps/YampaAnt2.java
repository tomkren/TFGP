package cz.tomkren.fishtron.apps;

import cz.tomkren.fishtron.eva.*;
import cz.tomkren.fishtron.operators.CopyOp;
import cz.tomkren.fishtron.operators.UntypedKozaXover;
import cz.tomkren.fishtron.operators.UntypedRampedHalfAndHalf;
import cz.tomkren.fishtron.sandbox2.*;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.utils.Checker;

import java.util.Arrays;
import java.util.Random;

/** Created by user on 13. 6. 2016. */

public class YampaAnt2 {

    public static void main(String[] args) {
        main2(args);
    }

    public static void main2(String[] args) {
        try {

            EvolutionOpts<PolyTree> opts = new JsonEvolutionOpts();
            Logger<PolyTree> logger = new Logger.Basic<>(opts);
            Evolution<PolyTree> eva = new Evolution<>(opts, logger);

            eva.startIterativeEvolution_old(1);
            //eva.startGenerationsEvolution(1);

        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void main1(String[] args) {

        Checker ch = new Checker(); // was nice seed : -3867518765423581920L
        Random r = ch.getRandom();

        String evalServerUrl = "http://localhost:4242/";

        SmartLibrary lib = AntLibs.koza;
        EvalManager<PolyTree> eval = new NetworkEvalManager<>("getEvalPoolSize","evalAnts_2", evalServerUrl, x->x);

        //int numRuns  = 1;
        int numGens  = 51;
        int popSize  = 1024;

        Selection<PolyTree> tournament7 = new Selection.Tournament2<>(7, r);

        IndivGenerator<PolyTree> generator = new UntypedRampedHalfAndHalf(lib, r, true);

        Distribution<Operator<PolyTree>> operators = new Distribution<>(Arrays.asList(
                new UntypedKozaXover(0.9, r),
                new CopyOp<>(0.1)
        ));


        EvolutionOpts<PolyTree> opts = new BasicEvolutionOpts<>(
                numGens*popSize, popSize/2, popSize, 4*popSize, true, true, -1,
                generator, eval, tournament7, operators, r
        );

        Logger<PolyTree> logger = new Logger.Basic<>(opts);

        Evolution<PolyTree> eva = new Evolution<>(opts, logger);
        //eva.startGenerationsEvolution(1);
        eva.startIterativeEvolution_old(1);

    }

}
