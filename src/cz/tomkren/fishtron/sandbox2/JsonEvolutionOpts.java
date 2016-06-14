package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.apps.AntLibs;
import cz.tomkren.fishtron.eva.*;
import cz.tomkren.fishtron.mains.DagEvaTester;
import cz.tomkren.fishtron.operators.*;
import cz.tomkren.fishtron.reusegen.QuerySolver;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;

/** Created by tom on 13.6.2016 */

public class JsonEvolutionOpts implements EvolutionOpts<PolyTree>  {

    private BasicEvolutionOpts<PolyTree> opts;

    public JsonEvolutionOpts() {
        this(new JSONObject());
    }

    public JsonEvolutionOpts(JSONObject config) {

        String evalServerUrl = getString(config, "evalServerUrl", "http://localhost:4242/");


        // for default purposes...
        int numGens  = 51;
        int popSize  = 1024;

        int numEvaluations             = getInt(config,"numEvaluations",             popSize * numGens );
        int minPopulationSizeToOperate = getInt(config,"minPopulationSizeToOperate", popSize / 2       );
        int numIndividualsToGenerate   = getInt(config,"numIndividualsToGenerate",   popSize           );
        int maxPopulationSize          = getInt(config,"maxPopulationSize",          popSize * 4       );

        boolean isUniquenessCheckPerformed  = getBoolean(config,"isUniquenessCheckPerformed", true);
        boolean saveBest                    = getBoolean(config,"saveBest", true);

        Long seed = config.has("seed") ? config.getLong("seed") : null;
        Checker checker = new Checker(seed);
        if (seed == null) {config.put("seed", checker.getSeed());}
        Random rand = checker.getRandom();

        // TODO !!! ....................................................................................................

        String problem = getString(config, "problem", "YampaAnt"); // todo TEMPORARY HAX


        SmartLibrary lib;
        IndivGenerator<PolyTree> generator;
        EvalManager<PolyTree> evalManager;
        Selection<PolyTree> parentSelection;
        Distribution<Operator<PolyTree>> operators;

        if (problem.equals("YampaAnt")) {

            evalManager = new NetworkEvalManager<>("getEvalPoolSize","evalAnts_2", evalServerUrl, x->x);

            lib = AntLibs.koza;
            generator = new UntypedRampedHalfAndHalf(lib, rand, true);
            parentSelection = new Selection.Tournament2<>(7, rand);
            operators = new Distribution<>(Arrays.asList(
                    new UntypedKozaXover(0.9, rand),
                    new CopyOp<>(0.1)
            ));

        } else {

            // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            evalManager = new NetworkEvalManager<>("getEvalPoolSize","fakeIterativeEval", evalServerUrl, x->x);


            String classPrefix = "cz.tomkren.fishtron.workflows."; //TODO přesunout do configu
            JSONObject allParamsInfo = DagEvaTester.testParamsInfo; // TODO !!!!  fake data need to download

            String goalTypeStr = getString(config, "goalType", "D => LD");

            JSONArray jsonLib = getJSONArray(config, "lib", F.arr(
                    "TypedDag.dia( TypedDag: D => D , TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
                    "TypedDag.dia0( TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
                    "TypedDag.split( TypedDag: D => (V D n) , MyList: V (D => LD) n ) : D => (V LD n)",
                    "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
                    "MyList.nil : V a 0",

                    "PCA : D => D",
                    "kBest : D => D",
                    "kMeans : D => (V D (S(S n)))",
                    "copy : D => (V D (S(S n)))",
                    "SVC        : D => LD",
                    "logR       : D => LD",
                    "gaussianNB : D => LD",
                    "DT         : D => LD",
                    "vote : (V LD (S(S n))) => LD",

                    "TypedDag.stacking( TypedDag: (V LD n) => D , TypedDag: D => LD ) : (V LD n) => LD",
                    "stacker : (V LD (S(S n))) => D",

                    "TypedDag.boosting( TypedDag: D => Boo , MyList: V (Boo => Boo) (S(S n)) , TypedDag : Boo => LD ) : D => LD",
                    "booBegin : D => Boo",
                    "TypedDag.booster( TypedDag : D => LD ) : Boo => Boo",
                    "booEnd   : Boo => LD"
            ));

            int generatingMaxTreeSize = getInt(config, "generatingMaxTreeSize", 37);
            double tournamentBetterWinsProbability = getDouble(config, "tournamentBetterWinsProbability", 0.8);

            JSONObject basicTypedXoverOpts = getJSONObject(config, "basicTypedXover", F.obj(
                    "probability", 0.3,
                    "maxTreeSize", 50
            )); // TODO předělat na xoverOpts, typ nemusí bejt takle debilně v názvu položky přece, obdobně u následujících několika

            JSONObject sameSizeSubtreeMutationOpts = getJSONObject(config, "sameSizeSubtreeMutation", F.obj(
                    "probability", 0.3,
                    "maxSubtreeSize", 10
            ));

            JSONObject oneParamMutationOpts = getJSONObject(config, "oneParamMutation", F.obj(
                    "probability", 0.3,
                    "shiftsWithProbabilities", F.arr(F.arr(-2, 0.1), F.arr(-1, 0.4), F.arr(1, 0.4), F.arr(2, 0.1))
            ));

            JSONObject copyOpOpts = getJSONObject(config, "copyOp", F.obj(
                    "probability", 0.1
            ));


            lib = SmartLibrary.mk(classPrefix, allParamsInfo, jsonLib);

            Type goalType = Types.parse(goalTypeStr);
            QuerySolver querySolver = new QuerySolver(lib, rand);

            generator = new RandomParamsPolyTreeGenerator(goalType, generatingMaxTreeSize, querySolver);
            parentSelection = new Selection.Tournament<>(tournamentBetterWinsProbability, rand);
            operators = new Distribution<>(Arrays.asList(
                    new BasicTypedXover(rand, basicTypedXoverOpts),
                    new SameSizeSubtreeMutation(querySolver, sameSizeSubtreeMutationOpts),
                    new OneParamMutation(rand, oneParamMutationOpts),
                    CopyOp.mk(copyOpOpts)
            ));

        }



        opts = new BasicEvolutionOpts<>(
                numEvaluations, minPopulationSizeToOperate, numIndividualsToGenerate, maxPopulationSize, isUniquenessCheckPerformed, saveBest,
                generator, evalManager, parentSelection, operators, rand);
    }

    public EvolutionOpts<PolyTree> getDirectOpts() {
        return opts;
    }

    private static String getString(JSONObject config, String key, String defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getString);
    }

    private static int getInt(JSONObject config, String key, int defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getInt);
    }

    private static double getDouble(JSONObject config, String key, double defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getDouble);
    }

    private static boolean getBoolean(JSONObject config, String key, boolean defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getBoolean);
    }

    private static JSONObject getJSONObject(JSONObject config, String key, JSONObject defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getJSONObject);
    }

    private static JSONArray getJSONArray(JSONObject config, String key, JSONArray defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getJSONArray);
    }

    private static <A> A getValue(JSONObject config, String key, A defaultValue, BiFunction<JSONObject, String, A> accessFun) {
        if (config.has(key)) {
            return accessFun.apply(config,key);
        } else {
            config.put(key, defaultValue);
            return defaultValue;
        }
    }




    @Override public int getNumEvaluations()                         {return opts.getNumEvaluations();}
    @Override public int getNumIndividualsToGenerate()               {return opts.getNumIndividualsToGenerate();}
    @Override public int getMinPopulationSizeToOperate()             {return opts.getMinPopulationSizeToOperate();}
    @Override public int getMaxPopulationSize()                      {return opts.getMaxPopulationSize();}
    @Override public boolean isUniquenessCheckPerform()              {return opts.isUniquenessCheckPerform();}
    @Override public boolean saveBest()                              {return opts.saveBest();}
    @Override public IndivGenerator<PolyTree> getGenerator()         {return opts.getGenerator();}
    @Override public EvalManager<PolyTree> getEvalManager()          {return opts.getEvalManager();}
    @Override public Selection<PolyTree> getParentSelection()        {return opts.getParentSelection();}
    @Override public Distribution<Operator<PolyTree>> getOperators() {return opts.getOperators();}
    @Override public Random getRandom()                              {return opts.getRandom();}

}
