package cz.tomkren.fishtron.ugen.apps.workflows;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.eva.Selection;
import cz.tomkren.fishtron.sandbox2.BasicEvolutionOpts;
import cz.tomkren.fishtron.sandbox2.Dag_EvalManager;

import cz.tomkren.fishtron.sandbox2.Dag_IEvalManager;
import cz.tomkren.fishtron.sandbox2.EvolutionOpts;
import net.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eva.AppTreeIndiv;
import cz.tomkren.fishtron.ugen.eva.AppTreeIndivGenerator;
import cz.tomkren.fishtron.ugen.eva.GenOpFactory;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import net.fishtron.utils.AB;
import cz.tomkren.utils.Checker;

import net.fishtron.utils.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;
import java.util.function.BiFunction;

/** Created by tom on 17. 2. 2017. */

public class EvolutionSetup {

    private EvolutionOpts<AppTreeIndiv> opts;
    private Dag_IEvalManager<AppTreeIndiv> evalManager;

    EvolutionSetup(JSONObject config, Checker checker) throws XmlRpcException {

        int numEvaluations = getInt(config,"numEvaluations", 32768);

        int numToGen        = getInt(config, "numIndividualsToGenerate",   256);
        int minPopToOperate = getInt(config, "minPopulationSizeToOperate", numToGen/2);
        int maxPopSize      = getInt(config, "maxPopulationSize",          numToGen*4);

        boolean isUniquenessCheckedPerformed = getBoolean(config,"isUniquenessCheckPerformed", true);
        boolean saveBest = getBoolean(config,"saveBest", true);

        int generatingMaxTreeSize = getInt(config, "generatingMaxTreeSize", 37);
        double tournamentBetterWinsProbability = getDouble(config, "tournamentBetterWinsProbability", 0.8);


        Long seed = config.has("seed") ? config.getLong("seed") : null;
        if (checker == null) {checker = new Checker(seed);}
        if (seed    == null) {config.put("seed", checker.getSeed());}
        Random rand = checker.getRandom();


        String evalServerUrl   = getString(config, "serverUrl", "http://localhost:8080/");
        String datasetFilename = getString(config, "dataset",   "winequality-white.csv");

        int timeLimit = getInt(config, "timeLimit", Integer.MAX_VALUE);

        boolean dummyFitnessMode = getBoolean(config, "dummyFitness", false);

        if (dummyFitnessMode) {
            evalManager = new DummyManager<>();
        } else {
            evalManager = new Dag_EvalManager<>("get_param_sets", "get_core_count", "submit", "get_evaluated", evalServerUrl, datasetFilename);
        }

        JSONObject allParamsInfo = evalManager.getAllParamsInfo(datasetFilename);


        Type goal = Workflows.goal;

        JSONObject methods = config.getJSONObject("methods"); // TODO dát defaultní

        AB<EvalLib,Gamma> libAndGamma = Workflows.mkLibAndGamma(methods);

        EvalLib lib = libAndGamma._1(); //Workflows.lib;
        Gamma gamma = libAndGamma._2(); //Workflows.gamma;

        Log.it("Gamma = \n"+gamma);

        Gen gen = new Gen(gamma, checker);

        IndivGenerator<AppTreeIndiv> generator = new AppTreeIndivGenerator(goal, generatingMaxTreeSize, gen, lib, allParamsInfo);
        Selection<AppTreeIndiv> parentSelection = new Selection.Tournament<>(tournamentBetterWinsProbability, rand);

        JSONArray operatorsConfig = config.has("operators") ? config.getJSONArray("operators") : new JSONArray();
        Distribution<Operator<AppTreeIndiv>> operators;
        operators = GenOpFactory.mkOperators(operatorsConfig, rand, gen, allParamsInfo);

        opts = new BasicEvolutionOpts<>(
                numEvaluations, minPopToOperate, numToGen, maxPopSize, isUniquenessCheckedPerformed, saveBest, timeLimit,
                generator, evalManager, parentSelection, operators, rand
        );
    }

    public EvolutionOpts<AppTreeIndiv> getOpts() {
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
