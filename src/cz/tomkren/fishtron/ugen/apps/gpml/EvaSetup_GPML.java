package cz.tomkren.fishtron.ugen.apps.gpml;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.apps.workflows.Workflows;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.*;
import cz.tomkren.fishtron.ugen.multi.operators.AppTreeMIGenerator;
import cz.tomkren.fishtron.ugen.multi.operators.MultiGenOpFactory;
import cz.tomkren.utils.*;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**Created by tom on 07.03.2017.*/

public class EvaSetup_GPML {

    private MultiEvaOpts<AppTreeMI> opts;
    private XmlRpcServer_MultiEvalManager<AppTreeMI> evalManager;

    EvaSetup_GPML(JSONObject config, Checker checker) throws XmlRpcException {

        int numEvaluations = Configs.getInt(config, Configs.numEvaluations, 32768);

        int numToGen        = Configs.getInt(config, Configs.numIndividualsToGenerate,   256);
        int minPopToOperate = Configs.getInt(config, Configs.minPopulationSizeToOperate, numToGen/2);
        int maxPopSize      = Configs.getInt(config, Configs.maxPopulationSize,          numToGen*4);

        //boolean saveBest = getBoolean(config,"saveBest", true);

        int generatingMaxTreeSize = Configs.getInt(config, Configs.generatingMaxTreeSize, 37);
        double tournamentBetterWinsProbability = Configs.getDouble(config, Configs.tournamentBetterWinsProbability, 0.8);

        /*Long seed = config.has(Configs.seed) ? config.getLong(Configs.seed) : null;
        if (checker == null) {checker = new Checker(seed);}
        if (seed    == null) {config.put(Configs.seed, checker.getSeed());}
        Random rand = checker.getRandom();*/
        Configs.handleRandomSeed(config, checker);

        String evalServerUrl   = Configs.getString(config, "serverUrl", "http://localhost:8080/");
        String datasetFilename = Configs.getString(config, "dataset",   "winequality-white.csv");

        int timeLimit  = Configs.getInt(config, Configs.timeLimit, Integer.MAX_VALUE);
        long sleepTime = Configs.getInt(config, Configs.sleepTime, 2000);

        boolean dummyFitnessMode = Configs.getBoolean(config, Configs.dummyFitness, false);


        JSONObject methods = config.getJSONObject("methods"); // todo .. dát defaultní

        AB<EvalLib,Gamma> libAndGamma = Workflows.mkLibAndGamma(methods);
        EvalLib lib = libAndGamma._1();
        Gamma gamma = libAndGamma._2();


        if (dummyFitnessMode) {
            evalManager = new DummyHistoryEvalManager<>(lib, "history_multiProblem.json");
        } else {
            evalManager = new DagMultiEvalManager<>(lib,"get_param_sets", "get_core_count", "submit", "get_evaluated", evalServerUrl, datasetFilename);
        }

        JSONObject allParamsInfo = evalManager.getAllParamsInfo(datasetFilename);
        Log.itln("allParamsInfo = "+ allParamsInfo);



        Log.it("Gamma = \n"+gamma);

        Gen gen = new Gen(gamma, checker);
        Type goal = Workflows.goal;

        IndivGenerator<AppTreeMI> generator = new AppTreeMIGenerator(goal, generatingMaxTreeSize, gen, allParamsInfo);
        MultiSelection<AppTreeMI> parentSelection = new MultiSelection.Tournament<>(tournamentBetterWinsProbability, checker.getRandom());

        JSONArray operatorsConfig = config.has("operators") ? config.getJSONArray("operators") : new JSONArray();
        Distribution<Operator<AppTreeMI>> operators;
        operators = MultiGenOpFactory.mkOperators(operatorsConfig, checker.getRandom(), gen, allParamsInfo);

        List<Boolean> isMaxims = Configs.getIsMaxims(config);


        opts = new BasicMultiEvaOpts<>(numEvaluations, numToGen, minPopToOperate, maxPopSize, /*saveBest,*/ timeLimit, sleepTime,
                generator, isMaxims, evalManager, parentSelection, operators, checker);
    }

    public MultiEvaOpts<AppTreeMI> getOpts() {
        return opts;
    }

    String quitServer() {
        return evalManager.quitServer();
    }


}
