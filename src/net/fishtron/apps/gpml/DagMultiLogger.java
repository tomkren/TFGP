package net.fishtron.apps.gpml;

import com.google.common.base.Strings;
import net.fishtron.eva.multi.*;
import cz.tomkren.fishtron.workflows.TypedDag;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**Created by tom on 09.03.2017.*/

public class DagMultiLogger implements MultiLogger<AppTreeMI> {

    private final Checker checker;
    private MultiEvaOpts<AppTreeMI> opts;

    private final File runLogDir;
    private final File evalsLogDir;

    DagMultiLogger(JSONObject config, String logPath, Checker checker, MultiEvaOpts<AppTreeMI> opts) {

        this.opts = opts;
        this.checker = checker;

        initBestSoFar();

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

    // TODO pročesat to tu například getValue je tu dvakrát a přišlo mi letmo že by to šlo nějak pročistit

    @Override
    public void log(int run, int evalId, MultiEvalResult<AppTreeMI> evalResult) {

        if (evalResult.isEmpty()) {
            Log.it_noln("=");
            return;
        }

        // stdout logging :
        Log.it();
        Log.it("eval #"+ evalId + (opts==null ? "" : "/"+opts.getNumEvaluations() ) );
        JSONObject bestInfo = logBest(evalId, evalResult);
        Log.it(" Evaluated individuals:");
        Log.list(F.map(evalResult.getIndividuals(), indiv -> showIndivRow(2,"",indiv) ));

        // to json file logging :
        JSONObject evalInfo = F.obj(
                "evalId", evalId,
                "time", checker.getTime(),
                "evalResult", evalResultToJson(evalResult),
                "bestInfo", bestInfo
        );
        String evalLogFilename = "eval_" + evalId + ".json";
        writeToFile(evalsLogDir, evalLogFilename, evalInfo.toString(2));
    }


    private AppTreeMI bestIndivSoFar;
    private int bestEvalIdSoFar;
    private int bestIndivIdSoFar;
    private List<Double> bestFitnessSoFar;

    private void initBestSoFar() {
        List<Boolean> isMaxims = opts.getFitnessSignature().getIsMaximizationList();

        bestIndivSoFar = null;
        bestEvalIdSoFar = -1;
        bestIndivIdSoFar = -1;
        bestFitnessSoFar = new ArrayList<>(isMaxims.size());

        for (Boolean isMaxim : isMaxims) {
            bestFitnessSoFar.add(isMaxim ? -Double.MAX_VALUE : Double.MAX_VALUE);
        }
    }

    private JSONObject logBest(int evalId, MultiEvalResult<AppTreeMI> evalResult) {
        boolean bestInThisResult = false;

        for (AB<AppTreeMI,JSONObject> evalRes : evalResult.getEvalResult()) {
            AppTreeMI indiv = evalRes._1();
            List<Double> newFitness = indiv.getFitness();
            if (isNewBest(newFitness)) {
                bestFitnessSoFar = newFitness;
                bestIndivSoFar = indiv;
                bestEvalIdSoFar  = evalId; // TODO pokud je evalResult víc nežjeden jedinec tak se chová trochu divně
                bestIndivIdSoFar = evalRes._2().getInt("id");
                bestInThisResult = true;
            }
        }

        JSONObject bestInfo = F.obj(
                "isInThisResult", bestInThisResult,
                "fitness", F.jsonMap(bestFitnessSoFar),
                "indivId", bestIndivIdSoFar,
                "evalId", bestEvalIdSoFar
        );

        String bestLabel = bestInThisResult ? "FOUND NEW BEST" : "previous best so far";

        Log.it(showIndivRow(1, bestLabel, bestIndivSoFar));

        return bestInfo;
    }

    private boolean isNewBest(List<Double> newFitness) {
        List<Boolean> isMaxims = opts.getFitnessSignature().getIsMaximizationList();
        for (int iFitness = 0; iFitness < isMaxims.size(); iFitness++) {
            double oldVal = bestFitnessSoFar.get(iFitness);
            double newVal = newFitness.get(iFitness);
            boolean isNewBetter = isMaxims.get(iFitness) ? newVal > oldVal : newVal < oldVal;

            if (isNewBetter)      {return true;}
            if (oldVal != newVal) {return false;}
        }

        // reachable only for the same fitness values in every sub-fitness
        return true;
    }



    private String showIndivRow(int ods, String label, AppTreeMI ind) {
        String indivCode = new JSONObject(((TypedDag) ind.getValue()).toJson()).toString();
        List<String> fitnessLabels = opts.getFitnessSignature().getFitnessLabels();
        return Strings.repeat(" ",ods)+ label +(label.length()>0?": ":"")+
                fitnessLabels.get(0)+": "+ind.getFitness(0)+ ", "+
                fitnessLabels.get(1)+": "+ind.getFitness(1) + "\t" + indivCode;
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
