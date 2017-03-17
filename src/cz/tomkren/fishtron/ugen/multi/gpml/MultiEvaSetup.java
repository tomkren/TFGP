package cz.tomkren.fishtron.ugen.multi.gpml;

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

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

/**Created by tom on 07.03.2017.*/

public class MultiEvaSetup {

    private MultiEvaOpts<AppTreeMI> opts;
    private XmlRpcServer_MultiEvalManager<AppTreeMI> evalManager;

    MultiEvaSetup(JSONObject config, Checker checker) throws XmlRpcException {

        int numEvaluations = getInt(config,"numEvaluations", 32768);

        int numToGen        = getInt(config, "numIndividualsToGenerate",   256);
        int minPopToOperate = getInt(config, "minPopulationSizeToOperate", numToGen/2);
        int maxPopSize      = getInt(config, "maxPopulationSize",          numToGen*4);

        //boolean saveBest = getBoolean(config,"saveBest", true);

        int generatingMaxTreeSize = getInt(config, "generatingMaxTreeSize", 37);
        double tournamentBetterWinsProbability = getDouble(config, "tournamentBetterWinsProbability", 0.8);

        Long seed = config.has("seed") ? config.getLong("seed") : null;
        if (checker == null) {checker = new Checker(seed);}
        if (seed    == null) {config.put("seed", checker.getSeed());}
        Random rand = checker.getRandom();

        String evalServerUrl   = getString(config, "serverUrl", "http://localhost:8080/");
        String datasetFilename = getString(config, "dataset",   "winequality-white.csv");

        int timeLimit = getInt(config, "timeLimit", Integer.MAX_VALUE);
        long sleepTime = getInt(config, "sleepTime", 2000);

        boolean dummyFitnessMode = getBoolean(config, "dummyFitness", false);


        Type goal = Workflows.goal;

        JSONObject methods = config.getJSONObject("methods"); // todo .. dát defaultní

        AB<EvalLib,Gamma> libAndGamma = Workflows.mkLibAndGamma(methods);
        EvalLib lib = libAndGamma._1();
        Gamma gamma = libAndGamma._2();


        if (dummyFitnessMode) {
            evalManager = new DummyMultiEvalManager<>(lib);
        } else {
            evalManager = new DagMultiEvalManager<>(lib,"get_param_sets", "get_core_count", "submit", "get_evaluated", evalServerUrl, datasetFilename);
        }

        JSONObject allParamsInfo = evalManager.getAllParamsInfo(datasetFilename);
        Log.itln("allParamsInfo = "+ allParamsInfo);



        Log.it("Gamma = \n"+gamma);

        Gen gen = new Gen(gamma, rand);

        IndivGenerator<AppTreeMI> generator = new AppTreeMIGenerator(goal, generatingMaxTreeSize, gen, allParamsInfo);
        MultiSelection<AppTreeMI> parentSelection = new MultiSelection.Tournament<>(tournamentBetterWinsProbability, rand);

        JSONArray operatorsConfig = config.has("operators") ? config.getJSONArray("operators") : new JSONArray();
        Distribution<Operator<AppTreeMI>> operators;
        operators = MultiGenOpFactory.mkOperators(operatorsConfig, rand, gen, allParamsInfo);

        // todo udelat bezpečnějc dyštak
        List<Boolean> isMaxims = F.map(config.getJSONArray("isMaxims"), x->(boolean)x); //Arrays.asList(true, false); // performance maximization , time minimization

        opts = new BasicMultiEvaOpts<>(numEvaluations, numToGen, minPopToOperate, maxPopSize, /*saveBest,*/ timeLimit, sleepTime,
                generator, isMaxims, evalManager, parentSelection, operators, rand);
    }

    public MultiEvaOpts<AppTreeMI> getOpts() {
        return opts;
    }

    String quitServer() {
        return evalManager.quitServer();
    }





    private static String getString(JSONObject config, String key, String defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getString);
    }

    public static int getInt(JSONObject config, String key, int defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getInt);
    }

    private static double getDouble(JSONObject config, String key, double defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getDouble);
    }

    private static boolean getBoolean(JSONObject config, String key, boolean defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getBoolean);
    }

    private static <A> A getValue(JSONObject config, String key, A defaultValue, BiFunction<JSONObject, String, A> accessFun) {
        if (config.has(key)) {
            return accessFun.apply(config,key);
        } else {
            config.put(key, defaultValue);
            return defaultValue;
        }
    }

}
