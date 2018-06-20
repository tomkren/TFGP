package net.fishtron.apps.tomkraft;

import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.eva.multi.*;
import net.fishtron.eva.multi.operators.AppTreeMIGenerator;
import net.fishtron.eva.multi.operators.MultiGenOpFactory;
import net.fishtron.eval.LibPackage;
import net.fishtron.gen.Gen;
import net.fishtron.server.api.Configs;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Distribution;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

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
        LibPackage libPack = TomkraftLib.mkLibPack(jobConfigOpts);

        // Generating
        Gen gen = new Gen(libPack.getGamma(), checker); //TODO replace with: Gen.fromJson(generatorDumpPath, gamma, checker)
        IndivGenerator<AppTreeMI> generator = new AppTreeMIGenerator(
                libPack.getGoal(), generatingMaxTreeSize, gen, libPack.getAllParamsInfo());

        // Operators
        Distribution<Operator<AppTreeMI>> operators = MultiGenOpFactory.mkOperators(
                operatorsConfig, checker.getRandom(), gen, libPack.getAllParamsInfo());

        // Evaluation
        TomkraftEvalManager evalManager = new TomkraftEvalManager(libPack.getEvalLib(), checker, preferredBufferSize);

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
