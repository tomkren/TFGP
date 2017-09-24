package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import net.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.CellOpts;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Libs;
import cz.tomkren.fishtron.ugen.compare.BasicCompareOpts;
import cz.tomkren.fishtron.ugen.compare.CompareOpts;
import cz.tomkren.fishtron.ugen.compare.CompareSelection;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.*;
import cz.tomkren.fishtron.ugen.multi.operators.AppTreeMIGenerator;
import cz.tomkren.fishtron.ugen.multi.operators.MultiGenOpFactory;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;


/** Created by tom on 20.03.2017.*/

class EvaSetup_CellEva {

    private CompareOpts<AppTreeMI> opts;
    private InteractiveComparator interactiveComparator;
    private EvaLogger<AppTreeMI> logger;

    EvaSetup_CellEva(JSONObject jobOpts, JSONObject config, String logPath, Checker ch) {

        int numEvaluations  = Configs.getInt(config,  Configs.numEvaluations, Integer.MAX_VALUE);

        int numToGen        = Configs.getInt(config,  Configs.numIndividualsToGenerate, 64);
        int maxPopSize      = Configs.getInt(config, Configs.maxPopulationSize, numToGen*4);

        int generatingMaxTreeSize = Configs.getInt(config, Configs.generatingMaxTreeSize, 10);
        double tournamentBetterWinsProbability = Configs.getDouble(config, Configs.tournamentBetterWinsProbability, 1.0);

        Configs.handleRandomSeed(config, ch);

        int timeLimit  = Configs.getInt(config, Configs.timeLimit, Integer.MAX_VALUE);
        long sleepTime = Configs.getInt(config, Configs.sleepTime, 2000);


        String plazaDir = jobOpts.optString("plazaDir", "mini_50"); //config.getJSONObject("cellPlaza").getString("evaPlaza");
        JSONObject plazaConfig = config.getJSONObject("cellPlaza").getJSONObject("plazas").getJSONObject(plazaDir);

        ch.it("plazaConfig: "+ plazaConfig);

        int numStates = Configs.getInt(plazaConfig, "numStates", 3);
        JSONArray pixelSizes = plazaConfig.getJSONArray("pixelSizes");


        CellOpts cellOpts = new CellOpts(numStates, plazaDir, pixelSizes, true);

        Gamma gamma = Libs.gamma;
        EvalLib lib = Libs.mkLib(cellOpts);
        JSONObject allParamsInfo = Libs.mkAllParamsInfo(cellOpts, ch);

        Gen gen = new Gen(gamma, ch);
        Type goal = Libs.goal_pair;

        IndivGenerator<AppTreeMI> generator = new AppTreeMIGenerator(goal, generatingMaxTreeSize, gen, allParamsInfo);
        CompareSelection<AppTreeMI> parentSelection = new CompareSelection.Tournament<>(tournamentBetterWinsProbability, ch.getRandom());

        JSONArray operatorsConfig = config.has("operators") ? config.getJSONArray("operators") : new JSONArray();
        Distribution<Operator<AppTreeMI>> operators;
        operators = MultiGenOpFactory.mkOperators(operatorsConfig, ch.getRandom(), gen, allParamsInfo);


        logger = new EvaLogger<>(config, logPath, ch, new CellShower(), F.arr("frames", "winners", "winners/px1", "zooms"));


        //boolean dummyFitnessMode = Configs.getBoolean(config, Configs.dummyFitness, false);
        int numFrames = config.optInt("numFrames", 16);
        //evalManager = new CellEvalManager(lib, cellOpts, numFrames, logger.getRunDirPath(), ch, jobProcess);

        interactiveComparator = new InteractiveComparator(lib, cellOpts, numFrames, logger.getRunDirPath(), sleepTime, ch);

        opts = new BasicCompareOpts<>(interactiveComparator::compare, numEvaluations, numToGen, maxPopSize, timeLimit, sleepTime, generator, parentSelection, operators, ch);
    }

    CompareOpts<AppTreeMI> getOpts() {return opts;}
    //CellEvalManager getEvalManager() {return evalManager;}
    InteractiveComparator getInteractiveComparator() {return interactiveComparator;}
    MultiLogger<AppTreeMI> getLogger() {return logger;}
}
