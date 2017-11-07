package net.todo.apps.workflows;

import java.io.*;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.json.JSONArray;
import org.json.JSONObject;

import net.fishtron.eva.simple.EvaledPop;
import net.fishtron.eva.simple.Logger;
import net.fishtron.eva.simple.EvalResult;
import net.fishtron.eva.simple.EvolutionOpts;
import net.fishtron.eva.simple.AppTreeIndiv;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;

import cz.tomkren.fishtron.workflows.TypedDag;



/**  Created by tom on 17. 2. 2017.*/

public class DagLogger implements Logger<AppTreeIndiv> {

    private final Checker checker;

    private final File runLogDir;
    private final File evalsLogDir;

    private EvolutionOpts<AppTreeIndiv> opts;

    private int bestEvalIdSoFar;
    private int bestIndivIdSoFar;
    private double bestValSoFar;


    DagLogger(JSONObject config, String logPath, Checker checker, EvolutionOpts<AppTreeIndiv> opts) {

        this.opts = opts;
        bestValSoFar = - Double.MAX_VALUE;
        bestEvalIdSoFar = -1;

        this.checker = checker;

        if (logPath == null) {
            runLogDir = null;
            evalsLogDir = null;
            return;
        }

        if (!(new File(logPath).exists())) {
            boolean success = new File(logPath).mkdirs();
            if (!success) {
                throw new Error("Unable to create log directory!");
            }
        }

        int i = 1;
        while (new File(logPath,"run_"+i).exists()) {i++;}

        runLogDir = new File(logPath,"run_"+i);
        boolean success1 = runLogDir.mkdir();
        if (!success1) {
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

    @Override
    public void iterativeLog(int run, int evalId, EvaledPop<AppTreeIndiv> pop, EvalResult<AppTreeIndiv> evalResult) {

        if (evalResult.isEmpty()) {
            Log.it_noln("=");
        } else {

            Log.it("\neval #"+ evalId + (opts==null ? "" : "/"+opts.getNumEvaluations() ) );

            AppTreeIndiv best = pop.getBestIndividual();
            String bestLabel = "previous best so far";
            boolean bestInThisResult = false;
            if (bestValSoFar < best.getWeight()) {
                bestInThisResult = true;
                bestLabel = "FOUND NEW BEST";
                bestValSoFar = best.getWeight();
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

    private static String showIndivRow(int ods, String label, AppTreeIndiv ind) {
        String indivCode = new JSONObject(((TypedDag) ind.computeValue()).toJson()).toString();
        return Strings.repeat(" ",ods)+ label +(label.length()>0?": ":"")+ ind.getWeight() + "\t" + indivCode;
    }

    private JSONArray evalResultToJson(EvalResult<AppTreeIndiv> evalResult) {
        return F.jsonMap(evalResult.getEvalResult(), p -> dagTreeToJson(p._1(), p._2()));
    }

    private static JSONObject dagTreeToJson(AppTreeIndiv tree, JSONObject indivData) {
        TypedDag dag = (TypedDag) tree.computeValue();
        return F.obj(
                "indivData", indivData,
                "descriptions", F.obj(
                        "fullTree", tree.toString().replace('"', '\''),
                        "shortTree", tree.toShortString(),
                        "evalJson", new JSONObject(dag.toJson()),
                        "kutilJson", dag.toKutilJson(0,0)
                )
        );
    }

    @Override
    public void logPop(int run, int generation, EvaledPop<AppTreeIndiv> pop) {
        Log.it("gen_" + generation);
        Log.it(pop);
        if (runLogDir != null) {
            JSONObject readable = evaledPopToJson(pop);
            writeToFile("gen_" + generation + ".json" , readable.toString(2));
        }
    }

    private JSONObject evaledPopToJson(EvaledPop<AppTreeIndiv> pop) {

        JSONObject readable = new JSONObject();
        readable.put("time", checker.getTime());

        List<AppTreeIndiv> sortedIndivs = F.sort(pop.getIndividuals().getList(), t -> -t.getFitVal().getVal());
        JSONArray readablePop = new JSONArray();
        for(JSONObject p:  F.map(sortedIndivs, DagLogger::dagTreeIndividualToJson)) {
            readablePop.put(p);
        }
        readable.put("population", readablePop);

        return readable;
    }

    private static JSONObject dagTreeIndividualToJson(AppTreeIndiv appTreeIndiv) {

        // AppTreeIndiv si počítá value nejvýše jednou, takže v pohodě ...
        TypedDag dag = (TypedDag) appTreeIndiv.computeValue();

        JSONObject readable = new JSONObject();

        readable.put("fit", appTreeIndiv.getFitVal().getVal());
        readable.put("short", appTreeIndiv.toShortString());
        readable.put("tree", appTreeIndiv.toString().replace('"', '\''));
        readable.put("kutil", dag.toKutilXML(100, 100));
        readable.put("json", dag.toJson().replace('"', '\'').replace("\n", "").replace(" ", ""));


        return readable;
    }

}
