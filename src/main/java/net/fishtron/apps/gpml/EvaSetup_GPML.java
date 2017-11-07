package net.fishtron.apps.gpml;

import net.fishtron.utils.Distribution;
import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.types.Type;
import net.fishtron.trees.Gamma;
import net.fishtron.gen.Gen;
import net.fishtron.eval.EvalLib;
import net.fishtron.eva.multi.*;
import net.fishtron.eva.multi.operators.AppTreeMIGenerator;
import net.fishtron.eva.multi.operators.MultiGenOpFactory;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;

import net.fishtron.apps.gpml.dag.NewSimpleWorkflows;
//import n et.fishtron.apps.workflows.Workflows;


/**Created by tom on 07.03.2017.*/

public class EvaSetup_GPML {

    private MultiEvaOpts<AppTreeMI> opts;
    private XmlRpcServer_MultiEvalManager<AppTreeMI> evalManager;

    EvaSetup_GPML(JSONObject config, Checker checker) throws XmlRpcException {

        int numEvaluations = Configs.getInt(config, Configs.numEvaluations, 32768);

        int numToGen        = Configs.getInt(config, Configs.numIndividualsToGenerate,   256);
        int minPopToOperate = Configs.getInt(config, Configs.minPopulationSizeToOperate, numToGen/2);
        int maxPopSize      = Configs.getInt(config, Configs.maxPopulationSize,          numToGen*4);


        int generatingMaxTreeSize = Configs.getInt(config, Configs.generatingMaxTreeSize, 37);
        double tournamentBetterWinsProbability = Configs.getDouble(config, Configs.tournamentBetterWinsProbability, 0.8);

        Configs.handleRandomSeed(config, checker);

        String evalServerUrl   = Configs.getString(config, "serverUrl", "http://localhost:8080/");
        String datasetFilename = Configs.getString(config, "dataset",   "winequality-white.csv");

        int timeLimit  = Configs.getInt(config, Configs.timeLimit, Integer.MAX_VALUE);
        long sleepTime = Configs.getInt(config, Configs.sleepTime, 2000);

        boolean isFitnessDummy = Configs.getBoolean(config, Configs.dummyFitness, false);

        String generatorDumpPath = null;
        if (config.has("generatorDump")) {
            Object gd = config.get("generatorDump");
            if (gd instanceof String) {
                generatorDumpPath = (String) gd;
            }
        }

        if (generatorDumpPath == null) {
            Log.it("No generator dump.");
        } else {
            Log.it("USING GENERATOR DUMP: "+generatorDumpPath);
        }

        JSONObject methods = config.getJSONObject("methods"); // todo .. dát defaultní

        AB<EvalLib,Gamma> libAndGamma = NewSimpleWorkflows.mkLibAndGamma(methods);
        EvalLib lib = libAndGamma._1();
        Gamma gamma = libAndGamma._2();

        JSONArray fitnessSignatureJson = config.getJSONArray("fitness");
        FitnessSignature fitnessSignature = new FitnessSignature(fitnessSignatureJson);

        Log.it("FITNESS SIGNATURE: "+ fitnessSignature);
        Log.it(" labels   = "+ fitnessSignature.getFitnessLabels());
        Log.it(" isMaxims = "+ fitnessSignature.getIsMaximizationList());

        if (isFitnessDummy) {
            //evalManager = new DummyHistoryEvalManager<>(lib, "configs/multi_gpml/history_multiProblem.json");
            evalManager = new DummyMultiEvalManager(lib, false, checker);
        } else {
            evalManager = new DagMultiEvalManager<>(fitnessSignature, lib, "get_param_sets", "get_core_count", "submit", "get_evaluated", evalServerUrl, datasetFilename);
        }

        JSONObject allParamsInfo = evalManager.getAllParamsInfo(datasetFilename);
        Log.itln("allParamsInfo = "+ allParamsInfo);



        Log.it("Gamma = \n"+gamma);

        Gen gen = Gen.fromJson(generatorDumpPath, gamma, checker);
        Type goal = NewSimpleWorkflows.goal;

        IndivGenerator<AppTreeMI> generator = new AppTreeMIGenerator(goal, generatingMaxTreeSize, gen, allParamsInfo);
        MultiSelection<AppTreeMI> parentSelection = new MultiSelection.Tournament<>(tournamentBetterWinsProbability, checker.getRandom());

        JSONArray operatorsConfig = config.has("operators") ? config.getJSONArray("operators") : new JSONArray();
        Distribution<Operator<AppTreeMI>> operators;
        operators = MultiGenOpFactory.mkOperators(operatorsConfig, checker.getRandom(), gen, allParamsInfo);



        //List<Boolean> isMaxims = Configs.getIsMaxims(config);
        //List<String> fitnessLabels = Arrays.asList("performance", "time/size");



        opts = new BasicMultiEvaOpts<>(numEvaluations, numToGen, minPopToOperate, maxPopSize, timeLimit, sleepTime,
                generator, fitnessSignature, evalManager, parentSelection, operators, null, checker);
    }

    public MultiEvaOpts<AppTreeMI> getOpts() {
        return opts;
    }

    String quitServer() {
        return evalManager.quitServer();
    }


}
