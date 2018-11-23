package net.fishtron.apps.circuits;

import net.fishtron.eva.multi.*;
import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.eval.LibPackage;
import net.fishtron.server.api.Configs;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Gamma;
import net.fishtron.trees.GammaSym;
import net.fishtron.types.Type;
import net.fishtron.utils.AB;
import net.fishtron.utils.ABCD;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CircuitsSetup /*implements MultiEvaSetup*/ {

    public static final String SETUP_NAME = "circuits";

    //private final MultiEvaOpts<AppTreeMI> opts;
    //private final MultiLogger<AppTreeMI> logger;

    public CircuitsSetup(JSONObject jobConfigOpts, Checker checker) {

        // -- Settings ----------------------------------------------

        // Basic settings
        int numEvaluations = 10000;
        int numToGen = 1000;
        int minPopToOperate = numToGen/2;
        int maxPopSize = numToGen*4;

        // Time settings
        int timeLimit = Integer.MAX_VALUE;
        int sleepTime = 1000;

        // Generating settings
        int generatingMaxTreeSize = 128;

        // Operators settings
        JSONArray operatorsConfig = F.arr(
                F.obj("name","basicTypedXover", "probability",0.7, "maxTreeSize",1024),
                F.obj("name","sameSizeSubtreeMutation", "probability",0.3, "maxSubtreeSize",32)//,
                /*F.obj("name","oneParamMutation", "probability",0.3, "shiftsWithProbabilities",
                        F.arr(F.arr(-2,0.1), F.arr(-1, 0.4), F.arr(1, 0.4), F.arr(2, 0.1)))*/
        );

        // Evaluation settings
        FitnessSignature fitnessSignature = new FitnessSignature(F.arr(F.arr("min","error"), F.arr("min","size")));

        // Selection settings
        double tournamentBetterWinsProbability = 0.8;

        // -- Construction of evolution components -----------------------------------------------

        // Building parts of individuals
        LibPackage libPac = CircuitsLib.mkLibPack(jobConfigOpts);

        // TODO : The following code dealing with lambda-head prefix should be properly encapsulated!

        Gamma gamma_basic = libPac.getGamma();
        Type goal_basic = libPac.getGoal();
        EvalLib evalLib_basic = libPac.getEvalLib();
        JSONObject allParamsInfo = libPac.getAllParamsInfo();

        List<String> suggestedVarNames = Arrays.asList("x0", "x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8");
        ABCD<Type, Gamma, Function<AppTree, AppTree>,List<GammaSym>> gammaRes = gamma_basic.mkGammaWithGoalTypeVars(goal_basic, suggestedVarNames);

        Type goal = gammaRes._1();
        Gamma gamma = gammaRes._2();
        Function<AppTree, AppTree> addLambdas = gammaRes._3();
        List<GammaSym> varList = gammaRes._4();

        EvalLib evalLib_vars = new EvalLib(F.map(varList, var -> AB.mk(var.getSym(), new EvalCode.ReflexiveJsonLeaf())));
        EvalLib evalLib = EvalLib.union(evalLib_basic, evalLib_vars);

        Function<Object,Object> addJsonLambdas = acc -> {
            for (int i = varList.size()-1; i >= 0; i--) {
                acc = F.obj(varList.get(i).getSym(), acc);
            }
            return acc;
        };

    }

}
