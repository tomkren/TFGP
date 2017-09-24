package cz.tomkren.fishtron.apps;

import cz.tomkren.fishtron.eva.*;
import cz.tomkren.fishtron.operators.CopyOp;
import cz.tomkren.fishtron.operators.UntypedKozaXover;
import cz.tomkren.fishtron.operators.UntypedRampedHalfAndHalf;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Distribution;
import net.fishtron.utils.Log;
import ec.util.MersenneTwisterFast;

import java.util.Arrays;
import java.util.Random;

/**  Created by tom on 22.2.2016.*/

public class YampaAnt {
    public enum ProblemVariant { Eat, Build, Build_Eat  };



    public static final ProblemVariant variant = ProblemVariant.Eat;



    public static void main(String[] args) {
        Checker ch = new Checker();
        Random r = ch.getRandom();

        long mersenneSeed = r.nextLong();
        Log.itln("MersenneTwisterFast seed: "+ mersenneSeed);
        MersenneTwisterFast rMersenne = new MersenneTwisterFast(mersenneSeed);


        String serverUrl = "http://localhost:4242";


        SmartLibrary lib;
        TogetherFitFun fitness;

        switch (variant) {
            case Eat:
                lib = AntLibs.koza;
                fitness = new YampaAntFitness("evalAnts", serverUrl);
                break;
            case Build:
                lib = AntLibs.buildAnt;
                fitness = new YampaAntFitness("evalBuildAnts", serverUrl);
                break;
            case Build_Eat:
                lib = AntLibs.buildAnt;
                fitness = new YampaAntFitness("evalBuildAntsByEatAnt", serverUrl);
                break;
            default: throw new Error("Unknown problem variant.");
        }


        int numRuns  = 1;
        int numGens  = 51;
        int popSize  = 1024; //2000; // 1024

        Selection<PolyTree> tournament2 = new Selection.Tournament<>(0.8, r);

        Selection<PolyTree> tournament7 = new Selection.Tournament2<>(7, r);
        Selection<PolyTree> roulette    = new Selection.Roulette<>(r);

        IndivGenerator<PolyTree> generator = new UntypedRampedHalfAndHalf(lib, r, true);

        Distribution<Operator<PolyTree>> operators = new Distribution<>(Arrays.asList(
                new UntypedKozaXover(0.9, r),
                new CopyOp<>(0.1)
        ));


        EvoOpts evoOpts = new EvoOpts(numRuns, numGens, popSize, !false  /*false = elitism is off*/ );
        Logger<PolyTree> logger = new Logger.Basic<>();

        Evolver<PolyTree> evolver = new Evolver.Opts<>(fitness, null, evoOpts, generator, operators, tournament7, logger, r).mk();
        evolver.startRun();

        ch.results();
        Log.it("MersenneTwisterFast seed: "+ mersenneSeed);
    }

}
