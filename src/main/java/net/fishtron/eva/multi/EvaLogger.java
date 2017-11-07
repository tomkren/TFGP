package net.fishtron.eva.multi;

import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Objects;

/**Created by tom on 20.03.2017.*/

public class EvaLogger<Indiv extends MultiIndiv> implements MultiLogger<Indiv> {

    private final Checker checker;
    //private MultiEvaOpts<Indiv> opts;

    private final File runLogDir;
    private final File evalsLogDir;

    private final IndivShower<Indiv> indivShower;

    //private final String runDirName;

    public EvaLogger(JSONObject config, String logPath, Checker checker, IndivShower<Indiv> indivShower, JSONArray runSubdirNames) {


        this.checker = checker;
        this.indivShower = indivShower;

        //initBestSoFar(); //todo

        // todo dát do F
        if (!(new File(logPath).exists())) {
            if (!new File(logPath).mkdirs()) {throw new Error("Unable to create log directory!");}
        }

        int i = 1;
        while (new File(logPath,"run_"+i).exists()) {i++;}



        runLogDir = new File(logPath,"run_"+i);
        if (!runLogDir.mkdir()) {throw new Error("Unable to create log directory!");}

        stdout("log directory for this run: "+runLogDir);
        writeToFile("config.json", config.toString(2));
        evalsLogDir = mkDir("evals");

        if (runSubdirNames != null) {
            F.each(runSubdirNames, runSubdirName -> mkDir((String) runSubdirName));
        }
    }

    public String getRunDirPath() {
        return runLogDir.getPath().replace('\\','/');
    }

    private void stdout(Object x) {checker.it(x);}
    private void stdout() {checker.it();}

    @Override
    public void log(int run, int evalId, MultiEvalResult<Indiv> evalResult) {

        if (evalResult.isEmpty()) {
            return;
        }

        // stdout logging :
        stdout();
        stdout("eval #"+ evalId); //+(opts==null ? "" : "/"+opts.getNumEvaluations()));
        //JSONObject bestInfo = logBest(evalId, evalResult); //todo
        stdout(" Evaluated individuals:");
        for (Indiv indiv : evalResult.getIndividuals()) {
            stdout(showIndivRow(indiv));
        }

        // to json file logging :
        JSONObject evalInfo = F.obj(
                "evalId", evalId,
                "time", checker.getTime(),
                "evalResult", F.jsonMap(evalResult.getEvalResult(), this::indivWithInfoToJson)
                //"bestInfo", bestInfo //todo
        );
        String evalLogFilename = "eval_" + evalId + ".json";
        writeToFile(evalsLogDir, evalLogFilename, evalInfo.toString(2));
    }


    private String showIndivRow(Indiv ind) {
        return "  fitness: "+ Objects.toString(ind.getFitness())+ "\t" + indivShower.indivToStdout(ind);
    }

    private JSONObject indivWithInfoToJson(AB<Indiv,JSONObject> indivWithInfo) {
        return F.obj(
                "indiv", indivShower.indivToJson(indivWithInfo._1()),
                "info",  indivWithInfo._2()
        );
    }


    // -- Files manipulations ---------------------

    private File mkDir(String dirName) {
        File dir = new File(runLogDir,dirName);
        if (!dir.mkdir()) {throw new Error("Unable to create dir "+dirName+" inside run-log directory!");}
        return dir;
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
