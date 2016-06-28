package cz.tomkren.fishtron.mains;

import cz.tomkren.utils.AB;
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


    private static final String resultsDir = "results";


    public static void main(String[] args) {
        Checker checker = new Checker();

        String runDirPath = "C:/Users/pejsek/Desktop/wine-5/media/logs/tom/run_1";
        String experimentId = "wine-5";


        processLogs(runDirPath, experimentId);
        mkDerivedFiles(experimentId);


        checker.results();
    }

    private static void mkDerivedFiles(String experimentId) {
        mkDerivedFiles(experimentId, "fitness", 2);
        mkDerivedFiles(experimentId, "generator", 1);
        mkDerivedFiles(experimentId, "basicTypedXover", 1);
        mkDerivedFiles(experimentId, "sameSizeSubtreeMutation", 1);
        mkDerivedFiles(experimentId, "oneParamMutation", 1);
    }

    private static void mkDerivedFiles(String experimentId, String tableName, int dataColIndex) {

        String experimentPrefix = resultsDir+"/"+experimentId+"/";
        String dir = experimentPrefix +"derived";
        mkDir(dir);



        List<AB<Integer,Double>> fitness = readDataFile(experimentPrefix + tableName+".txt", dataColIndex);

        //Log.list(fitness);

        writeNumList(dir+"/"+tableName+"-best.txt", bestSoFar(fitness));
        writeNumList(dir+"/"+tableName+"-w1.txt", movingAvg(fitness,1));
        writeNumList(dir+"/"+tableName+"-w10.txt", movingAvg(fitness,10));
        writeNumList(dir+"/"+tableName+"-w100.txt", movingAvg(fitness,100));
        writeNumList(dir+"/"+tableName+"-w1000.txt", movingAvg(fitness,1000));
        writeNumList(dir+"/"+tableName+"-w10000.txt", movingAvg(fitness,10000));


    }

    private static void writeNumList(String path, List<AB<Integer,Double>> xs) {

        if (xs == null) {
            Log.it("  ! File "+path+" will not be created.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (AB<Integer,Double> x : xs) {

            int evalId = x._1();
            double val = x._2();

            sb.append(evalId).append("\t").append(val).append("\n");
        }

        F.writeFile(path, sb.toString());
    }


    private static List<AB<Integer,Double>> bestSoFar(List<AB<Integer,Double>> xs) {
        List<AB<Integer,Double>> ret = new ArrayList<>(xs.size());
        double best = - Double.MAX_VALUE;
        for (AB<Integer,Double> x : xs) {
            if (x._2() > best) {best = x._2();}
            ret.add(new AB<>(x._1(),best));
        }
        return ret;
    }

    private static List<AB<Integer,Double>> movingAvg(List<AB<Integer,Double>> xs, int window) {

        if (window >= xs.size()) {
            return null;
        }

        List<AB<Integer,Double>> ret = new ArrayList<>(xs.size()-window+1);
        for (int i = 0; i <= xs.size()-window; i++) {
            double sum = 0.0;
            for (int j=0; j<window; j++) {
                sum += xs.get(i+j)._2();
            }
            int firstEvalIdInWindow = xs.get(i)._1();
            ret.add(new AB<>(firstEvalIdInWindow,sum/window));
        }
        return ret;
    }

    private static List<AB<Integer,Double>> readDataFile(String path, int col) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            List<AB<Integer,Double>> ret = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.equals("") && line.charAt(0) != '#') {
                    String[] parts = line.split("\\s+");

                    int evalId = Integer.parseInt(parts[0]);
                    double val = Double.parseDouble(parts[col]);
                    ret.add(new AB<>(evalId,val));
                }
            }
            return ret;
        } catch (Exception e) {
            throw new Error(e);
        }
    }



    private static void processLogs(String runDirPath, String experimentId) {
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
