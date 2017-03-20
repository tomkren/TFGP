package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Libs;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.*;
import cz.tomkren.fishtron.ugen.multi.operators.AppTreeMIGenerator;
import cz.tomkren.fishtron.ugen.multi.operators.MultiGenOpFactory;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/** Created by tom on 20.03.2017.*/

class EvaSetup_CellEva {

    private MultiEvaOpts<AppTreeMI> opts;
    private MultiEvalManager<AppTreeMI> evalManager; // todo zřejmě bude potřeba zobecnit pro komparativní selekci

    EvaSetup_CellEva(JSONObject config, Checker checker) {

        int numEvaluations  = Configs.getInt(config,  Configs.numEvaluations, Integer.MAX_VALUE);

        int numToGen        = Configs.getInt(config,  Configs.numIndividualsToGenerate, 64);
        int minPopToOperate = Configs.getInt(config, Configs.minPopulationSizeToOperate, 0); // todo zjistit jak se chova pro nula
        int maxPopSize      = Configs.getInt(config, Configs.maxPopulationSize, numToGen*4);

        int generatingMaxTreeSize = Configs.getInt(config, Configs.generatingMaxTreeSize, 10);
        double tournamentBetterWinsProbability = Configs.getDouble(config, Configs.tournamentBetterWinsProbability, 1.0);

        Random rand = Configs.handleRandomSeed(config, checker);

        int timeLimit  = Configs.getInt(config, Configs.timeLimit, Integer.MAX_VALUE);
        long sleepTime = Configs.getInt(config, Configs.sleepTime, 2000);


        String plazaDir = config.getJSONObject("cellPlaza").getString("evaPlaza");
        JSONObject plazaConfig = config.getJSONObject("cellPlaza").getJSONObject("plazas").getJSONObject(plazaDir);

        Log.it("plazaConfig: "+ plazaConfig);

        int numStates = Configs.getInt(plazaConfig, "numStates", 3);
        JSONArray pixelSizes = plazaConfig.getJSONArray("pixelSizes");


        Gamma gamma = Libs.gamma;
        EvalLib lib = Libs.mkLib(numStates, plazaDir, pixelSizes);
        JSONObject allParamsInfo = Libs.mkAllParamsInfo(numStates, plazaDir);

        Gen gen = new Gen(gamma, rand);
        Type goal = Libs.goal;

        IndivGenerator<AppTreeMI> generator = new AppTreeMIGenerator(goal, generatingMaxTreeSize, gen, allParamsInfo);
        MultiSelection<AppTreeMI> parentSelection = new MultiSelection.Tournament<>(tournamentBetterWinsProbability, rand);

        JSONArray operatorsConfig = config.has("operators") ? config.getJSONArray("operators") : new JSONArray();
        Distribution<Operator<AppTreeMI>> operators;
        operators = MultiGenOpFactory.mkOperators(operatorsConfig, rand, gen, allParamsInfo);

        List<Boolean> isMaxims = Configs.getIsMaxims(config);

        boolean dummyFitnessMode = Configs.getBoolean(config, Configs.dummyFitness, false);
        evalManager = new CellEvalManager<>(lib, dummyFitnessMode);

        opts = new BasicMultiEvaOpts<>(numEvaluations, numToGen, minPopToOperate, maxPopSize, /*saveBest,*/ timeLimit, sleepTime,
                generator, isMaxims, evalManager, parentSelection, operators, rand);
    }

    public MultiEvaOpts<AppTreeMI> getOpts() {
        return opts;
    }


}
