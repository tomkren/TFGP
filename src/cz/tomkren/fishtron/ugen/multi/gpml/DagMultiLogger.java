package cz.tomkren.fishtron.ugen.multi.gpml;

import com.google.common.base.Strings;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.*;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

/**Created by tom on 09.03.2017.*/

public class DagMultiLogger implements MultiLogger<AppTreeMI> {

    private final Checker checker;
    private MultiEvaOpts<AppTreeMI> opts;

    private final File runLogDir;
    private final File evalsLogDir;

    DagMultiLogger(JSONObject config, String logPath, Checker checker, MultiEvaOpts<AppTreeMI> opts) {

        this.opts = opts;
        this.checker = checker;


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

    // TODO pročesat to tu například geValue je tu dvakrát a přišlo mi letmo že by to šlo nějak pročistit

    @Override
    public void iterativeLog(int run, int evalId, MultiPopulation<AppTreeMI> pop, MultiEvalResult<AppTreeMI> evalResult) {

        if (evalResult.isEmpty()) {
            Log.it_noln("=");
            return;
        }

        Log.it();
        Log.it("eval #"+ evalId + (opts==null ? "" : "/"+opts.getNumEvaluations() ) );

        Log.it(" Evaluated individuals:");
        Log.list(F.map(evalResult.getIndividuals(), indiv -> showIndivRow(2,"",indiv) ));

        JSONObject evalInfo = F.obj(
                "evalId", evalId,
                "time", checker.getTime(),
                "evalResult", evalResultToJson(evalResult)
        );

        String evalLogFilename = "eval_" + evalId + ".json";
        writeToFile(evalsLogDir, evalLogFilename, evalInfo.toString(2));
    }

    private static String showIndivRow(int ods, String label, AppTreeMI ind) {
        String indivCode = new JSONObject(((TypedDag) ind.getValue()).toJson()).toString();
        return Strings.repeat(" ",ods)+ label +(label.length()>0?": ":"")+ "performance: "+ind.getFitness(0)+ ", time: "+ind.getFitness(1) + "\t" + indivCode;
    }

    private JSONArray evalResultToJson(MultiEvalResult<AppTreeMI> evalResult) {
        return F.jsonMap(evalResult.getEvalResult(), p -> dagTreeToJson(p._1(), p._2()));
    }

    private static JSONObject dagTreeToJson(AppTreeMI indiv, JSONObject indivData) {
        TypedDag dag = (TypedDag) indiv.getValue();
        return F.obj(
                "indivData", indivData,
                "descriptions", F.obj(
                        "fullTree", indiv.toString().replace('"', '\''),
                        "shortTree", indiv.toShortString(),
                        "evalJson", new JSONObject(dag.toJson()),
                        "kutilJson", dag.toKutilJson(0,0)
                )
        );
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


}
