package net.fishtron.apps.tomkraft;

import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.eva.compare.*;
import net.fishtron.eva.multi.AppTreeMI;
import net.fishtron.eva.multi.MultiLogger;
import net.fishtron.eva.multi.operators.AppTreeMIGenerator;
import net.fishtron.eva.multi.operators.MultiGenOpFactory;
import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.eval.LibPackage;
import net.fishtron.gen.Gen;
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

/**
 * Created by tom on 09.07.2018.
 */
public class TkCompareSetup implements CompareEvaSetup {

    // TODO: merge this class with TomkraftSetup

    public static final String SETUP_NAME = "tomkraft_interactive";


    private final CompareOpts<AppTreeMI> opts;
    private final IndivComparator<AppTreeMI> interactiveComparator;
    private final MultiLogger<AppTreeMI> logger;

    public TkCompareSetup(JSONObject jobConfigOpts, Checker checker) {

        // Basic settings
        int numEvaluations = 10000;
        int numToGen = 100;
        int maxPopSize = numToGen*4;

        // Time settings
        int timeLimit = Integer.MAX_VALUE;
        int sleepTime = 100;

        // Generating settings
        int generatingMaxTreeSize = 16;

        // Operators settings
        JSONArray operatorsConfig = F.arr(
                F.obj("name","basicTypedXover", "probability",0.4, "maxTreeSize",128),
                F.obj("name","sameSizeSubtreeMutation", "probability",0.3, "maxSubtreeSize",32),
                F.obj("name","oneParamMutation", "probability",0.3, "shiftsWithProbabilities",
                        F.arr(F.arr(-2,0.1), F.arr(-1, 0.4), F.arr(1, 0.4), F.arr(2, 0.1)))
        );

        // Selection & Evaluation settings
        int numParentCandidates = 9;
        boolean removeGivenTasksImmediately = !true;

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
        interactiveComparator = new TkInteractiveComparator(evalLib, sleepTime, removeGivenTasksImmediately, addJsonLambdas, checker);

        // Parent Selection
        CompareSelection<AppTreeMI> parentSelection = new CompareSelection.StrictMultiTournament<>(numParentCandidates, checker.getRandom());

        logger = TomkraftLogger.mk(jobConfigOpts, checker);
        opts = new BasicCompareOpts<>(interactiveComparator, numEvaluations, numToGen, maxPopSize, timeLimit, sleepTime, generator, parentSelection, operators, checker);
    }


    @Override public CompareOpts<AppTreeMI> getOpts() {return opts;}
    @Override public IndivComparator<AppTreeMI> getIndivComparator() {return interactiveComparator;}
    @Override public MultiLogger<AppTreeMI> getLogger() {return logger;}

}
