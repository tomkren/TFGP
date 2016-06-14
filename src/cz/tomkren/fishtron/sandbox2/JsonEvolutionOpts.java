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

            lib = SmartLibrary.mk(classPrefix, allParamsInfo, config.getJSONArray("lib"));

            Type goalType = Types.parse(config.getString("goalType"));
            QuerySolver querySolver = new QuerySolver(lib, rand);

            generator = new RandomParamsPolyTreeGenerator(goalType, config.getInt("generatingMaxTreeSize"), querySolver);
            parentSelection = new Selection.Tournament<>(config.getDouble("tournamentBetterWinsProbability"), rand);
            operators = new Distribution<>(Arrays.asList(
                    new BasicTypedXover(config, rand),
                    new SameSizeSubtreeMutation(config, querySolver),
                    new OneParamMutation(config, rand),
                    new CopyOp<>(config)
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
