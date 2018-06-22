package net.fishtron.apps.tomkraft;

import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.eva.multi.*;
import net.fishtron.eva.multi.operators.AppTreeMIGenerator;
import net.fishtron.eva.multi.operators.MultiGenOpFactory;
import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.eval.LibPackage;
import net.fishtron.gen.Gen;
import net.fishtron.server.api.Configs;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Gamma;
import net.fishtron.trees.GammaSym;
import net.fishtron.types.Type;
import net.fishtron.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TomkraftSetup implements MultiEvaSetup {

    public static final String SETUP_NAME = "tomkraft";

    private final MultiEvaOpts<AppTreeMI> opts;
    private final MultiLogger<AppTreeMI> logger;

    public TomkraftSetup(JSONObject jobConfigOpts, Checker checker) {

        // -- Settings ----------------------------------------------

        // Basic settings
        int numEvaluations = 1000;
        int numToGen = 40;
        int minPopToOperate = numToGen/2;
        int maxPopSize = numToGen*4;

        // Time settings
        int timeLimit = Integer.MAX_VALUE;
        int sleepTime = 1000;

        // Generating settings
        int generatingMaxTreeSize = 32;

        // Operators settings
        JSONArray operatorsConfig = F.arr(
                F.obj("name","basicTypedXover", "probability",0.4, "maxTreeSize",128),
                F.obj("name","sameSizeSubtreeMutation", "probability",0.3, "maxSubtreeSize",32),
                F.obj("name","oneParamMutation", "probability",0.3, "shiftsWithProbabilities",
                        F.arr(F.arr(-2,0.1), F.arr(-1, 0.4), F.arr(1, 0.4), F.arr(2, 0.1)))
        );

        // Evaluation settings
        int preferredBufferSize = Configs.get_int(jobConfigOpts, "preferredBufferSize", 32);
        FitnessSignature fitnessSignature = new FitnessSignature(F.arr(F.arr("max","rating")));

        // Selection settings
        double tournamentBetterWinsProbability = 0.8;


        // -- Construction of evolution components -----------------------------------------------

        // Building parts of individuals
        LibPackage libPac = TomkraftLib.mkLibPack(jobConfigOpts);


        // TODO : The following code dealing with lambda-head prefix should be properly encapsulated!

        Gamma gamma_basic = libPac.getGamma();
        Type goal_basic = libPac.getGoal();
        EvalLib evalLib_basic = libPac.getEvalLib();
        JSONObject allParamsInfo = libPac.getAllParamsInfo();

        List<String> suggestedVarNames = Arrays.asList("x", "z");
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

        // Generating
        Gen gen = new Gen(gamma, checker); //TODO replace with: Gen.fromJson(generatorDumpPath, gamma, checker)
        IndivGenerator<AppTreeMI> generator = new AppTreeMIGenerator(
                goal, generatingMaxTreeSize, gen, allParamsInfo);

        // Operators
        Distribution<Operator<AppTreeMI>> operators = MultiGenOpFactory.mkOperators(
                operatorsConfig, checker.getRandom(), gen, allParamsInfo);

        // Evaluation
        TomkraftEvalManager evalManager = new TomkraftEvalManager(evalLib, checker, preferredBufferSize, addJsonLambdas);

        // Selection
        MultiSelection<AppTreeMI> parentSelection = new MultiSelection.Tournament<>(tournamentBetterWinsProbability, checker.getRandom());

        opts = new BasicMultiEvaOpts<>(
                numEvaluations, numToGen,
                minPopToOperate, maxPopSize,
                timeLimit, sleepTime,
                generator, fitnessSignature,
                evalManager, parentSelection,
                operators,
                evalManager,
                checker
        );

        logger = TomkraftLogger.mk(jobConfigOpts, checker);
    }

    @Override public MultiEvaOpts<AppTreeMI> getEvaOpts() { return opts; }
    @Override public MultiLogger<AppTreeMI> getLogger() { return logger; }
}
