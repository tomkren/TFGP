package cz.tomkren.fishtron.workflows;

import com.google.common.base.Strings;
import cz.tomkren.fishtron.eva.EvaledPop;
import cz.tomkren.fishtron.eva.Logger;
import cz.tomkren.fishtron.sandbox2.EvalResult;
import cz.tomkren.fishtron.sandbox2.EvolutionOpts;
import cz.tomkren.fishtron.terms.PolyTree;

import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;

import com.google.common.base.Joiner;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.io.*;

/** Created by tom on 6.7.2015. */

public class DagEvolutionLogger implements Logger<PolyTree> {

    private final File runLogDir;
    private File evalsLogDir;

    //private final File parsableSubDir; // TODO zase zprovoznit !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private final Checker checker;

    private EvolutionOpts<PolyTree> opts;

    private int bestEvalIdSoFar;
    private int bestIndivIdSoFar;
    private double bestValSoFar;

    public DagEvolutionLogger(JSONObject config, String logPath, Checker checker) {
        this(config, logPath, checker, null);
    }

    public DagEvolutionLogger(JSONObject config, String logPath, Checker checker, EvolutionOpts<PolyTree> opts) {

        this.opts = opts;
        bestValSoFar = - Double.MAX_VALUE;
        bestEvalIdSoFar = -1;


        this.checker = checker;

        if (logPath == null) {
            runLogDir = null;
            //parsableSubDir = null;
            return;
        }

        if (!(new File(logPath).exists())) {
            boolean success = new File(logPath).mkdirs();
            if (!success) {
                throw new Error("Unable to create log directory!");
            }
        }

        int i = 1;

        while (new File(logPath,"run_"+i).exists()) {
            i++;
        }

        runLogDir = new File(logPath,"run_"+i); // + parsableSubDir = new File(runLogDir, "parsable");
        boolean success1 = runLogDir.mkdir();   // + boolean success2 = parsableSubDir.mkdir();
        if (!success1) {                        // + || !success2
            throw new Error("Unable to create log directory!");
        }


        Log.it("log directory for this run: "+runLogDir);

        writeToFile("config.json", config.toString(2));


        evalsLogDir = mkDir("evals");
    }

    private File mkDir(String dirName) {
        File dir = new File(runLogDir,dirName);
        boolean success = dir.mkdir();
        if (!success) {
            throw new Error("Unable to create dir "+dirName+" inside run-log directory!");
        }
        return dir;
    }

    @Override
    public void logErrorIndivs(int generation, List<Object> errorIndiv) {
        String errorLogStr = Joiner.on("\n\n").join( F.map(errorIndiv, o->((TypedDag)o).toJson()) );
        writeToFile("ERROR_malformed_in_generation_" + generation + ".json", errorLogStr);
    }

    private void writeToFile(String filename, String str) {
        writeToFile(runLogDir, filename, str);
    }

    private void writeToFile(File dir, String filename, String str) {
        writeToFile(new File(dir, filename),str);
    }

    private void writeToFile(File file, String str) {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            writer.write(str);
            writer.write('\n');
        } catch (IOException e) {
            throw new Error("Logging error: "+ e.getMessage());
        }

    }

    public DagEvolutionLogger() {
        this(null,null,null);
    }


    @Override
    public void iterativeLog(int run, int evalId, EvaledPop<PolyTree> pop, EvalResult<PolyTree> evalResult) {

        if (evalResult.isEmpty()) {
            Log.it_noln("=");
        } else {

            Log.it("\neval #"+ evalId + (opts==null ? "" : "/"+opts.getNumEvaluations() ) );

            PolyTree best = pop.getBestIndividual();
            String bestLabel = "previous best so far";
            boolean bestInThisResult = false;
            if (bestValSoFar < best.getWeight()) {
                bestValSoFar = best.getWeight();
                bestLabel = "FOUND NEW BEST";
                bestInThisResult = true;
                bestIndivIdSoFar =evalResult.getBestInResult()._2().getInt("id");
                bestEvalIdSoFar = evalId;
            }

            Log.it(showIndivRow(1, bestLabel, best));
            Log.it(" Evaluated individuals:");
            Log.list(F.map(evalResult.getIndividuals(), indiv -> showIndivRow(2,"",indiv) ));


            JSONObject evalInfo = F.obj(
                    "evalId", evalId,
                    "bestSoFar", F.obj(
                            "indivId", bestIndivIdSoFar,
                            "evalId", bestEvalIdSoFar,
                            "fitness", bestValSoFar,
                            "isInThisResult", bestInThisResult
                    ),
                    "evalResult", evalResultToJson(evalResult)
            );

            writeToFile(evalsLogDir, "eval_" + evalId + ".json" , evalInfo.toString(2));

        }
    }

    private static String showIndivRow(int ods, String label, PolyTree ind) {
        String indivCode = new JSONObject(((TypedDag) ind.computeValue()).toJson()).toString();
        return Strings.repeat(" ",ods)+ label +(label.length()>0?": ":"")+ ind.getWeight() + "\t" + indivCode;
    }

    private JSONArray evalResultToJson(EvalResult<PolyTree> evalResult) {
        return F.jsonMap(evalResult.getEvalResult(), p -> dagPolyTreeToJson(p._1(), p._2()));
    }

    private static JSONObject dagPolyTreeToJson(PolyTree tree, JSONObject indivData) {

        TypedDag dag = (TypedDag) tree.computeValue();

        return F.obj(
            "indivData", indivData,
            "descriptions", F.obj(
                "fullTree", tree.toString().replace('"', '\''),
                "shortTree", tree.toStringWithoutParams(),
                "evalJson", new JSONObject(dag.toJson()),
                "kutilJson", dag.toKutilJson(0,0)
            )
        );
    }






    @Override
    public void logPop(int run, int generation, EvaledPop<PolyTree> pop) {
        PolyTree best = pop.getBestIndividual();
        Log.it("gen_" + generation);
        Log.it(pop);

        if (runLogDir != null) {

            //AA<JSONObject> twoJsons = evaledPopToJson(pop);
            JSONObject readable = evaledPopToJson(pop); //twoJsons._1();
            //JSONObject parsable = twoJsons._2();

            writeToFile("gen_" + generation + ".json" , readable.toString(2));
            //writeToFile(new File(parsableSubDir, "gen_" + generation +  "_dense.json"), parsable.toString());
            //writeToFile(new File(parsableSubDir, "gen_" + generation + "_indent.json"), parsable.toString(2));
        }



        /*for (PolyTree tree : pop.getIndividuals().getList()) {
            Log.it(  );
        }*/

    }

    public JSONObject evaledPopToJson(EvaledPop<PolyTree> pop) {

        JSONArray readablePop = new JSONArray();
        //JSONArray parsablePop = new JSONArray();

        for(JSONObject p:  F.map(F.sort(pop.getIndividuals().getList(), t -> -t.getFitVal().getVal()), DagEvolutionLogger::dagTreeIndividualToJson)) {
            readablePop.put(p);
            //parsablePop.put(p._2());
        }

        JSONObject readable = new JSONObject();
        //JSONObject parsable = new JSONObject();

        readable.put("time", checker.getTime());
        readable.put("population", readablePop);

        //parsable.put("time", checker.getTime());
        //parsable.put("population", parsablePop);

        return /*new AA<>(*/readable/*,parsable)*/;
    }

    public static JSONObject dagTreeIndividualToJson(PolyTree tree) {

        //TODO trochu neefektivní počítat znova, ale snad to přežijem
        TypedDag dag = (TypedDag)tree.computeValue();

        JSONObject readable = new JSONObject();
        //JSONObject parsable = new JSONObject();

        readable.put("fit", tree.getFitVal().getVal());
        readable.put("short", tree.toStringWithoutParams());
        readable.put("tree", tree.toString().replace('"', '\''));
        readable.put("json", dag.toJson().replace('"', '\'').replace("\n", "").replace(" ", ""));
        readable.put("kutil", dag.toKutilXML(100, 100));

        //parsable.put("fit", tree.getFitVal().getVal());
        //parsable.put("json", new JSONObject(dag.toJson()) );

        return readable; //new AA<>(readable, parsable);
    }


}
