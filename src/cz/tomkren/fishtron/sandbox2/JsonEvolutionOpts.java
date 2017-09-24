package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.apps.AntLibs;
import cz.tomkren.fishtron.eva.*;
import cz.tomkren.fishtron.operators.*;
import cz.tomkren.fishtron.reusegen.QuerySolver;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.utils.Checker;
import net.fishtron.utils.F;
import cz.tomkren.utils.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;

/** Created by tom on 13.6.2016 */

public class JsonEvolutionOpts implements EvolutionOpts<PolyTree>  {

    // todo narychlo hax aby šlo vypínat server
    private EvalManager<PolyTree> evalManager;

    private BasicEvolutionOpts<PolyTree> opts;

    public JsonEvolutionOpts() throws XmlRpcException {
        this(new JSONObject());
    }

    public JsonEvolutionOpts(JSONObject config) throws XmlRpcException  {
        this(config, null);
    }

    public JsonEvolutionOpts(JSONObject config, Checker checker) throws XmlRpcException {

        String evalServerUrl = getString(config, "serverUrl", "http://localhost:8080/");

        // for default purposes...
        int numGens  = getInt(config, "numGenerations", 51);
        int popSize  = getInt(config, "populationSize", 1024);


        int numEvaluations             = getInt(config,"numEvaluations",             popSize * numGens );
        int minPopulationSizeToOperate = getInt(config,"minPopulationSizeToOperate", popSize / 2       );
        int numIndividualsToGenerate   = getInt(config,"numIndividualsToGenerate",   popSize           );
        int maxPopulationSize          = getInt(config,"maxPopulationSize",          popSize * 4       );

        boolean isUniquenessCheckPerformed  = getBoolean(config,"isUniquenessCheckPerformed", true);
        boolean saveBest                    = getBoolean(config,"saveBest", true);

        Long seed = config.has("seed") ? config.getLong("seed") : null;
        if (checker == null) {checker = new Checker(seed);}
        if (seed    == null) {config.put("seed", checker.getSeed());}
        Random rand = checker.getRandom();

        //--------------------------------------------

        String problem = getString(config, "problem", "GP-ML"); // todo TEMPORARY HAX

        SmartLibrary lib;
        IndivGenerator<PolyTree> generator;
        //EvalManager<PolyTree> evalManager;
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

            String datasetFilename = getString(config, "dataset", "winequality-white.csv");

            Dag_EvalManager<PolyTree> dagEvalManager = new Dag_EvalManager<>(
                    "get_param_sets", "get_core_count","submit", "get_evaluated", evalServerUrl, datasetFilename);

            //evalManager = new NetworkEvalManager<>("getEvalPoolSize","fakeIterativeEval", evalServerUrl, x->x);
            evalManager = dagEvalManager;


            //JSONObject allParamsInfo = DagEvaTester.testParamsInfo;
            JSONObject allParamsInfo = dagEvalManager.getAllParamsInfo(datasetFilename);

            String classPrefix = "cz.tomkren.fishtron.workflows."; //TODO přesunout do configu

            String goalTypeStr = getString(config, "goalType", "D => LD");

            JSONArray jsonLib = getJSONArray(config, "lib", defaultLibJson);

            int generatingMaxTreeSize = getInt(config, "generatingMaxTreeSize", 37);
            double tournamentBetterWinsProbability = getDouble(config, "tournamentBetterWinsProbability", 0.8);




            lib = SmartLibrary.mk(classPrefix, allParamsInfo, jsonLib);

            Type goalType = Types.parse(goalTypeStr);
            QuerySolver querySolver = new QuerySolver(lib, rand);

            generator = new RandomParamsPolyTreeGenerator(goalType, generatingMaxTreeSize, querySolver);
            parentSelection = new Selection.Tournament<>(tournamentBetterWinsProbability, rand);

            if (config.has("operators")) {
              operators = OperatorFactory.mkOperators(config.getJSONArray("operators"), rand, querySolver);
            } else {
                operators = mkOperators_oldSchool(config, rand, querySolver);
                Log.err("\n!!!\nWarning! You are using deprecated format of operators !!!\n!!!\n\n");
            }

        }

        int timeLimit = getInt(config, "timeLimit", -1);

        opts = new BasicEvolutionOpts<>(
                numEvaluations, minPopulationSizeToOperate, numIndividualsToGenerate, maxPopulationSize, isUniquenessCheckPerformed, saveBest,
                timeLimit, generator, evalManager, parentSelection, operators, rand);
    }

    private Distribution<Operator<PolyTree>> mkOperators_oldSchool(JSONObject config, Random rand, QuerySolver qs) {
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

        return new Distribution<>(Arrays.asList(
            new BasicTypedXover(rand, basicTypedXoverOpts),
            new SameSizeSubtreeMutation(qs, sameSizeSubtreeMutationOpts),
            new OneParamMutation(rand, oneParamMutationOpts),
            CopyOp.mk(copyOpOpts)
        ));
    }

    public String quitServer() {
        if (evalManager instanceof Dag_EvalManager) {

            return ((Dag_EvalManager)evalManager).quitServer();

        } else {
            return "Unsupported quitServer call.";
        }
    }

    private static final JSONArray defaultLibJson = F.arr(
            "TypedDag.dia( TypedDag: D => D , TypedDag: D => (V LD n an) , TypedDag: (V LD n an) => LD ) : D => LD",
            "TypedDag.dia0( TypedDag: D => (V LD n an) , TypedDag: (V LD n an) => LD ) : D => LD",
            "TypedDag.split( TypedDag: D => (V D n an) , MyList: V (D => LD) n an ) : D => (V LD n an)",
            "MyList.cons( Object: a , MyList: V a n an ) : V a (S n) an",
            "MyList.nil : V a 0 an",

            "PCA   : D => D",
            "kBest : D => D",
            "kMeans : D => (V D (S(S n)) Disj)",
            "copy   : D => (V D (S(S n)) Copy)",
            "SVC        : D => LD",
            "logR       : D => LD",
            "gaussianNB : D => LD",
            "DT         : D => LD",
            "vote : (V LD (S(S n)) an) => LD",

            "TypedDag.stacking( TypedDag: (V LD n Copy) => D , TypedDag: D => LD ) : (V LD n Copy) => LD",
            "stacker : (V LD (S(S n)) Copy) => D",

            "TypedDag.boosting( TypedDag: D => Boo , MyList: V (Boo => Boo) (S(S n)) an , TypedDag : Boo => LD ) : D => LD",
            "booBegin : D => Boo",
            "TypedDag.booster( TypedDag : D => LD ) : Boo => Boo",
            "booEnd   : Boo => LD"
    );

    //public



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
    @Override public int getTimeLimit()                              {return opts.getTimeLimit();}
    @Override public IndivGenerator<PolyTree> getGenerator()         {return opts.getGenerator();}
    @Override public EvalManager<PolyTree> getEvalManager()          {return opts.getEvalManager();}
    @Override public Selection<PolyTree> getParentSelection()        {return opts.getParentSelection();}
    @Override public Distribution<Operator<PolyTree>> getOperators() {return opts.getOperators();}
    @Override public Random getRandom()                              {return opts.getRandom();}

}
