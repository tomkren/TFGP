package cz.tomkren.fishtron.mains;

import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 28. 6. 2016.*/

public class ProcessLogs {



    public static void main_(String[] args) {

        List<Double> fs = readDataFile("fitnesses.txt",1);

        writeNumList("fitnesses-best.txt",bestSoFar(fs));

    }

    private static void writeNumList(String path, List<Double> xs) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < xs.size(); i++) {
            sb.append(i+1).append("\t").append(xs.get(i)).append("\n");
        }

        F.writeFile(path, sb.toString());
    }


    private static List<Double> bestSoFar(List<Double> xs) {
        List<Double> ret = new ArrayList<>(xs.size());
        double best = - Double.MAX_VALUE;
        for (double x : xs) {
            if (x > best) {best = x;}
            ret.add(best);
        }
        return ret;
    }

    private static List<Double> movingAvg(List<Double> xs, int window) {
        List<Double> ret = new ArrayList<>(xs.size()-window+1);
        for (int i = 0; i <= xs.size()-window; i++) {
            double sum = 0.0;
            for (int j=0; j<window; j++) {
                sum += xs.get(i+j);
            }
            ret.add(sum/window);
        }
        return ret;
    }

    private static List<Double> readDataFile(String path, int col) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            List<Double> ret = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.equals("") && line.charAt(0) != '#') {
                    String[] parts = line.split("\\s+");
                    ret.add(Double.parseDouble(parts[col]));
                }
            }
            return ret;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) {
        Checker checker = new Checker();

        String runDirPath = "C:/Users/user/Desktop/logs.wine.1/media/logs/tom/run_1";

        String experimentId = "wine-1";

        process(runDirPath, experimentId);

        checker.results();
    }

    private static void process(String runDirPath, String experimentId) {
        String evalsDirPath = runDirPath + "/evals";
        String configFilePath = runDirPath + "/config.json";

        JSONObject config = new JSONObject(readFile(configFilePath));

        Map<String,StringBuilder> operators = new HashMap<>();

        int numEvaluations = config.getInt("numEvaluations");

        StringBuilder fitnesses = new StringBuilder();
        fitnesses.append("#evalId\tindivId\tfitness\n");

        for (int evalId = 1; evalId <= numEvaluations; evalId++) {

            String evalFilePath = evalsDirPath + "/eval_" + evalId + ".json";

            JSONObject logJson = new JSONObject(readFile(evalFilePath));
            JSONArray evalResult = logJson.getJSONArray("evalResult");


            for (int i = 0; i < evalResult.length(); i++) {

                JSONObject indivData = evalResult.getJSONObject(i).getJSONObject("indivData");
                int    id      = indivData.getInt("id");
                double fitness = indivData.getDouble("fitness");
                fitnesses.append(evalId).append("\t").append(id).append("\t").append(fitness).append("\n");


                JSONObject operatorData = indivData.getJSONObject("operator");
                String operatorName = operatorData.getString("name");

                JSONArray parentsData = indivData.getJSONArray("parents");

                StringBuilder operatorSb = operators.get(operatorName);
                if (operatorSb == null) {
                    operatorSb = new StringBuilder();
                    operators.put(operatorName, operatorSb);

                    if (parentsData.length() == 0) {
                        operatorSb.append("#evalId\tfitness\n");
                    } else {
                        operatorSb.append("#evalId\timprovement\n");
                    }
                }


                if (parentsData.length() == 0) {

                    operatorSb.append(evalId).append("\t").append(fitness).append("\n");

                } else {

                    double avgParentFitness = 0.0;
                    for (int p = 0; p<parentsData.length(); p++) {
                        JSONObject parent = parentsData.getJSONObject(p);
                        avgParentFitness += parent.getDouble("fitness");
                    }
                    avgParentFitness /= parentsData.length();

                    double improvement = fitness - avgParentFitness;

                    operatorSb.append(evalId).append("\t").append(improvement).append("\n");
                }

            }



            if (evalId % 100 == 0) {
                Log.it(evalId+" log files read..");
            }
        }

        //Log.it(fitnesses.toString());

        String resultsDir = "results";
        mkDir(resultsDir);

        String experimentDir = resultsDir+"/"+experimentId;
        mkDir(experimentDir);

        F.writeFile(experimentDir+"/fitness.txt", fitnesses.toString());

        for (Map.Entry<String,StringBuilder> e : operators.entrySet()) {
            String operatorName = e.getKey();
            String tableStr = e.getValue().toString();

            F.writeFile(experimentDir+"/"+operatorName+".txt", tableStr);
        }

    }

    private static String readFile(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private static File mkDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean success = dir.mkdir();
            if (!success) {throw new Error("Unable to create dir " + dirName + " inside run-log directory!");}
            return dir;
        } else {
            return null;
        }
    }

}
