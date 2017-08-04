package cz.tomkren.fishtron.mains;

import com.google.common.base.Joiner;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.fishtron.ugen.multi.MultiUtils;
import cz.tomkren.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/** Created by tom on 28. 6. 2016.*/

public class ProcessLogs {

    private static final String resultsDir = "results";
    private static final String resultsStatsDir = resultsDir+"/stats";


    private static final List<String> prefixes = Arrays.asList("allOperators", "generator", "basicTypedXover", "sameSizeSubtreeMutation", "oneParamMutation");
    private static final List<String> middles = Arrays.asList(/*"1",*/"2");

    private static final List<String> avg_prefixes = Arrays.asList("allOperators", /*"generator", todo */ "basicTypedXover", "sameSizeSubtreeMutation", "oneParamMutation");


    public static void main(String[] args) {
        Checker checker = new Checker();

        List<String> experimentKinds = Arrays.asList("multi-size-cache", "multi-time-nocache");
        List<String> experimentBatches = Arrays.asList("wilt" ,"wine" ,"magic" ,"ml-prove");

        //String experimentBatch = "ml-prove";
        //String experimentKind = "multi-time-nocache";
        //String experimentKind = "multi-size-cache";

        StringBuilder results_all = new StringBuilder();
        results_all.append("# performance\t\tevals\trun_time\t\ttime_per_eval\n\n");

        for (String experimentKind : experimentKinds) {
            for (String experimentBatch: experimentBatches) {
                String resultFileStr = processBatch(experimentBatch, experimentKind);
                results_all.append(resultFileStr).append("\n\n");
            }
        }

        F.writeFile(resultsDir+"/R_all.txt", results_all.toString());

        checker.results();
    }

    private static String processBatch(String experimentBatch, String experimentKind) {

        Map<String,List<String>> experimentNamesMap = new HashMap<>();
        experimentNamesMap.put("wilt", mkExperimentNames("wilt"));
        experimentNamesMap.put("wine", mkExperimentNames("wine"));
        experimentNamesMap.put("magic", mkExperimentNames("magic"));
        experimentNamesMap.put("ml-prove", mkExperimentNames("ml-prove"));


        int window = 2000;

        if (experimentBatch.equals("magic")) { window = 500; }

        if (experimentKind.equals("multi-size-cache")) {
            if (experimentBatch.equals("ml-prove")) { window = 1000; }
        }

        List<String> experimentNames = experimentNamesMap.get(experimentBatch);

        List<ABC<Double,Integer,Double>> runInfos = new ArrayList<>(experimentNames.size());

        for (String experimentName : experimentNames) {
            ABC<Double,Integer,Double> runInfo = mkExperimentStats(experimentName, experimentKind, window);
            runInfos.add(runInfo);
        }

        Log.it("\n\n==============================================================================\n\n");

        mkAveragedStats(experimentNames, experimentBatch, experimentKind, window);

        String batchId = experimentBatch +"-"+ experimentKind;

        String gnuplotScript_all =
                "experiments = '"+experimentBatch+"'\n"+
                "kind = '"+experimentKind+"'\n"+
                "load '_settings.plt'\n"+
                "window = '"+window+"'\n"+
                "load '_main_averaged.plt'";
        F.writeFile(resultsDir+"/A_all_"+batchId+".plt", gnuplotScript_all);

        String gnuplotScript_operators =
                "experiments = '"+experimentBatch+"'\n"+
                        "kind = '"+experimentKind+"'\n"+
                        "load '_settings.plt'\n"+
                        "window = '"+window+"'\n"+
                        "dir = 'stats/averaged/'.experiments.'-'.kind\n"+
                        "load '_operators_averaged.plt'";
        F.writeFile(resultsDir+"/A_ops_"+batchId+".plt", gnuplotScript_operators);

        String gnuplotScript_front1s =
                        "experiments = '"+experimentBatch+"'\n" +
                        "kind = '"+experimentKind+"'\n" +
                        "load '_front1_averaged.plt'";
        F.writeFile(resultsDir+"/A_frs_"+batchId+".plt", gnuplotScript_front1s);

        double sum_fitness = 0;
        int sum_numEvals = 0;
        double sum_runTime = 0;
        Log.it("\n"+batchId+":");
        for (ABC<Double,Integer,Double> runInfo : runInfos) {
            Log.it("     "+runInfo._1()+"\t"+runInfo._2()+"\t"+runInfo._3());
            sum_fitness  += runInfo._1();
            sum_numEvals += runInfo._2();
            sum_runTime  += runInfo._3();
        }
        double meanBestFitVal = sum_fitness / runInfos.size();
        double meanNumEvals = sum_numEvals / runInfos.size();
        double meanRunTime  = sum_runTime / runInfos.size();

        Log.it("mean:"+meanBestFitVal+"\t"+meanNumEvals+"\t"+meanRunTime);

        String resultFileStr = batchId+":\n"+
                Joiner.on("\n").join(F.map(runInfos, ri -> ri._1()+"\t"+ri._2()+"\t"+ri._3()+"\t"+( ri._3() / ri._2() ) ))+"\n"+
                "mean:\n"+
                meanBestFitVal+"\t"+meanNumEvals+"\t"+meanRunTime+"\t"+(meanRunTime/meanNumEvals);

        F.writeFile(resultsDir+"/R_"+batchId+".txt", resultFileStr);

        return resultFileStr;
    }

    private static List<String> mkExperimentNames(String experimentBatch) {
        return mkExperimentNames(experimentBatch, 5);
    }

    private static List<String> mkExperimentNames(String experimentBatch, int numExperiments) {
        List<String> ret = new ArrayList<>(numExperiments);
        for (int i = 1; i <= numExperiments; i++) {
            ret.add(experimentBatch+"-"+i);
        }
        return ret;
    }

    private static ABC<Double,Integer,Double> mkExperimentStats(String experimentName, String experimentKind, int window) {

        String runDirPath = resultsDir+"/raw/dageva-outputs-"+experimentKind+"/"+experimentName+"/tom/run_1";
        String experimentId = experimentName+"-"+experimentKind;


        processLogs(runDirPath, experimentId);
        AB<Double,Integer> runInfo = mkDerivedFiles(experimentId, window);

        String gnuplotScript =
                        "experiment = '"+experimentName+"'\n"+
                        "kind = '"+experimentKind+"'\n"+
                        "load '_settings.plt'\n"+
                        "window = '"+window+"'\n"+
                        "load '_main.plt'";
        F.writeFile(resultsDir+"/S_"+experimentName+"-"+experimentKind+".plt", gnuplotScript);

        // TODO hax
        String evalFilePath = runDirPath + "/evals/eval_" + runInfo._2() + ".json";
        String evalFileStr = readFile(evalFilePath);
        if (evalFileStr == null) { throw new Error("No eval id "+runInfo._2()+" !!!"); }
        JSONObject logJson = new JSONObject(evalFileStr);
        double runTime = logJson.getDouble("time");

        return ABC.mk(runInfo._1(),runInfo._2(), runTime);
    }

    private static void mkAveragedStats(List<String> experimentNames, String experimentBatch, String experimentKind, int window) {

        String batchId = experimentBatch +"-"+ experimentKind;

        for (String prefix : avg_prefixes) {
            for (String middle : middles) {
                String tableName = prefix+"_"+middle;

                List<List<AB<Integer, Double>>> datas = new ArrayList<>(experimentNames.size());

                for (String experimentName : experimentNames) {
                    String experimentId = experimentName+"-"+experimentKind;
                    String experimentDir = resultsStatsDir +"/"+experimentId+"/";
                    String derivedDir = experimentDir +"derived";

                    List<AB<Integer, Double>> data_full = readDataFile(derivedDir + "/" + tableName + "-w" + window + "_full.txt", 1);
                    datas.add(addFrontNulls(data_full));
                }

                List<AB<Integer, Double>> datas_averaged = new ArrayList<>();

                boolean someNonEmpty = true;
                int i = 1;
                while (someNonEmpty) {

                    int num = 0;
                    double sum = 0;
                    someNonEmpty = false;
                    for (List<AB<Integer, Double>> data : datas) {
                        if (data.size() > i) {
                            someNonEmpty = true;
                            AB<Integer, Double> point = data.get(i);
                            if (point._2() != null) {
                                sum += point._2();
                                num++;
                            }
                        }
                    }

                    if (num > 0) {
                        datas_averaged.add(AB.mk(i, sum / num));
                    }

                    i++;
                }
                String averagedDir = resultsStatsDir + "/averaged";
                String batchDir = averagedDir + "/" + batchId;
                mkDir(averagedDir);
                mkDir(batchDir);

                writeNumList(batchDir +"/"+tableName+"-w"+window+"_averaged.txt", datas_averaged);

            }
        }

    }


    private static List<AB<Integer,Double>> addFrontNulls(List<AB<Integer,Double>> xs_full) {
        int firstX = xs_full.get(0)._1();
        List<AB<Integer,Double>> ret = new ArrayList<>();
        for (int i = 1; i < firstX; i++) {
            ret.add(AB.mk(i, null));
        }
        ret.addAll(xs_full);
        return ret;
    }


    private static AB<Double,Integer> mkDerivedFiles(String experimentId, int window) {

        String experimentDir = resultsStatsDir +"/"+experimentId+"/";
        String derivedDir = experimentDir +"derived";
        mkDir(derivedDir);

        List<AB<Integer,Double>> fitness = readDataFile(experimentDir + "fitness.txt", 2);
        List<AB<Integer,Double>> fitness_bestSoFar = bestSoFar(fitness);
        writeNumList(derivedDir+"/fitness-best.txt", fitness_bestSoFar);
        writeNumList(derivedDir+"/fitness-w"+window+".txt", movingAvg(fitness,1));


        int numEvaluatedIndividuals = fitness.size();
        double bestFitVal = fitness_bestSoFar.get(fitness_bestSoFar.size()-1)._2();


        for (String prefix : prefixes) {
            for (String middle : middles) {
                String tableName = prefix+"_"+middle;

                List<AB<Integer,Double>> data = readDataFile(experimentDir + tableName+".txt", 1);
                writeNumList(derivedDir+"/"+tableName+"-best.txt", bestSoFar(data));

                List<AB<Integer,Double>> movingData = movingAvg(data,window);
                writeNumList(derivedDir+"/"+tableName+"-w"+window+".txt", movingData);

                List<AB<Integer,Double>> movingData_full = fullResample(movingData);
                writeNumList(derivedDir+"/"+tableName+"-w"+window+"_full.txt", movingData_full);

                // todo zbytecne se pocita znova
                List<AB<Integer,Double>> baseData = readDataFile(experimentDir+"allOperators"+"_"+middle+".txt", 1);
                List<AB<Integer,Double>> movingBase = movingAvg(baseData,window);
                List<AB<Integer,Double>> normalizedData = div(movingData, movingBase);
                writeNumList(derivedDir+"/"+tableName+"-w"+window+"_normalized.txt", normalizedData);
            }
        }

        return AB.mk(bestFitVal,numEvaluatedIndividuals);
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


    private static List<AB<Integer,Double>> fullResample(List<AB<Integer,Double>> xs) {
        if (xs == null) { return null; }
        if (xs.isEmpty()) {return new ArrayList<>(0);}

        List<AB<Integer,Double>> ret = new ArrayList<>();

        int lastIndex = xs.size() - 1;

        int start  = xs.get(0)._1();
        int finish = xs.get(lastIndex)._1();

        AB<Integer,Double> prevPoint = xs.get(0);
        int prevPointIndex = 0;
        ret.add(prevPoint);


        for (int currentX = start+1; currentX < finish; currentX++) {

            AB<Integer,Double> nextPoint = xs.get(prevPointIndex+1);

            if (nextPoint._1() == currentX) {
                prevPoint = nextPoint;
                prevPointIndex++;
                ret.add(nextPoint);
            } else {

                int    prevX = prevPoint._1();
                double prevY = prevPoint._2();

                int    dx = nextPoint._1() - prevX;
                double dy = nextPoint._2() - prevY;

                double fac = ((double)(currentX - prevX)) / dx;
                double newValue = prevY + (fac * dy);

                ret.add(AB.mk(currentX, newValue));
            }
        }

        if (lastIndex != 0) {
            ret.add(xs.get(lastIndex));
        }

        return ret;
    }

    private static List<AB<Integer,Double>> div(List<AB<Integer,Double>> xs, List<AB<Integer,Double>> ys) {
        if (xs == null || ys == null) { return null; }

        List<AB<Integer,Double>> ret = new ArrayList<>(xs.size());

        AB<Integer,Double> y_a,y_b;
        int y_i = 0;
        y_a = ys.get(0);
        y_b = ys.get(0);

        for (AB<Integer,Double> x : xs) {

            int x1 = x._1();

            while (y_b._1() < x1 && y_i + 1 < ys.size()) {
                y_a = ys.get(y_i);
                y_b = ys.get(y_i + 1);
                y_i ++;
            }

            double y_avg = (y_a._2() + y_b._2()) * 0.5;

            ret.add(AB.mk(x1, x._2() / y_avg));

        }

        return ret;
    }

    private static List<AB<Integer,Double>> movingAvg(List<AB<Integer,Double>> xs, int window) {

        if (window >= xs.size()) { return null; }

        List<AB<Integer,Double>> ret = new ArrayList<>(xs.size()-window+1);
        for (int i = 0; i <= xs.size()-window; i++) {
            double sum_1 = 0.0;
            double sum_2 = 0.0;
            for (int j=0; j<window; j++) {
                AB<Integer,Double> point = xs.get(i+j);
                sum_1 += point._1();
                sum_2 += point._2();
            }
            //int firstEvalIdInWindow = xs.get(i)._1();
            ret.add(new AB<>(/*firstEvalIdInWindow*/(int)Math.round(sum_1/window), sum_2/window));
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

        String experimentDir = resultsStatsDir +"/"+experimentId;

        if (new File(experimentDir).exists()) {
            Log.it("Skipping processLog, folder is already there...\n\n");
            return;
        }


        String evalsDirPath = runDirPath + "/evals";
        String configFilePath = runDirPath + "/config.json";

        JSONObject config = new JSONObject(readFile(configFilePath));
        int numEvaluations = config.getInt("numEvaluations");

        Map<String,AA<StringBuilder>> operators = new HashMap<>();


        AA<StringBuilder> allOperators = AA.mk(new StringBuilder(), new StringBuilder());
        allOperators._1().append("#evalId\timprovement\n");
        allOperators._2().append("#evalId\tP_improvement\n");

        StringBuilder fitnesses = new StringBuilder();
        fitnesses.append("#evalId\tindivId\tfitness\n");

        StringBuilder fitnesses2 = new StringBuilder();
        fitnesses2.append("#evalId\tindivId\tfitness1\tfitness2..\n");

        List<MultiIndiv> indivs = new ArrayList<>();

        for (int evalId = 1; evalId <= numEvaluations; evalId++) {

            String evalFilePath = evalsDirPath + "/eval_" + evalId + ".json";

            String evalFileStr = readFile(evalFilePath);

            if (evalFileStr == null) {
                Log.it("\n\nNO eval-id "+evalId+" !!!\n\n");
                break;
            }

            JSONObject logJson = new JSONObject(evalFileStr);
            JSONArray evalResult = logJson.getJSONArray("evalResult");


            for (int i = 0; i < evalResult.length(); i++) {

                JSONObject indivData = evalResult.getJSONObject(i).getJSONObject("indivData");
                int    id      = indivData.getInt("id");
                double fitness = getSingeFitness(indivData);
                List<Double> fitnessList = getFitnessList(indivData);

                fitnesses.append(evalId).append("\t").append(id).append("\t").append(fitness).append("\n");
                fitnesses2.append(evalId).append("\t").append(id).append("\t").append(Joiner.on("\t").join(fitnessList)).append("\n");

                // construct fake indiv for front computation
                AppTreeMI fakeIndiv = new AppTreeMI(null);
                fakeIndiv.setFitness(fitnessList);
                fakeIndiv.setId(id);
                indivs.add(fakeIndiv);


                JSONObject operatorData = indivData.getJSONObject("operator");
                String operatorName = operatorData.getString("name");

                JSONArray parentsData = indivData.getJSONArray("parents");



                AA<StringBuilder> operatorSbPair = operators.get(operatorName);
                if (operatorSbPair == null) {

                    StringBuilder sb_1 = new StringBuilder();
                    StringBuilder sb_2 = new StringBuilder();

                    operatorSbPair = AA.mk(sb_1, sb_2);
                    operators.put(operatorName, operatorSbPair);

                    if (parentsData.length() == 0) {
                        sb_1.append("#evalId\tfitness\n");
                    } else {
                        sb_1.append("#evalId\timprovement\n");
                        sb_2.append("#evalId\tP_improvement\n");
                    }
                }


                if (parentsData.length() == 0) {
                    operatorSbPair._1().append(evalId).append("\t").append(fitness).append("\n");

                } else {

                    double p_improvement = 0.0;
                    double avgParentFitness = 0.0;

                    int numParents = parentsData.length();
                    double p_delta = 1.0 / numParents;

                    for (int p = 0; p<numParents; p++) {
                        JSONObject parent = parentsData.getJSONObject(p);
                        double parentFitness = getSingeFitness(parent);

                        avgParentFitness += parentFitness;

                        if (fitness > parentFitness) {
                            p_improvement += p_delta;
                        } else if (fitness == parentFitness) {
                            p_improvement += 0.5 * p_delta;
                        }
                    }

                    avgParentFitness /= numParents;

                    double improvement = fitness - avgParentFitness;

                    StringBuilder sb_1 = operatorSbPair._1();
                    StringBuilder sb_2 = operatorSbPair._2();

                    sb_1.append(evalId).append("\t").append(improvement).append("\n");
                    sb_2.append(evalId).append("\t").append(p_improvement).append("\n");

                    allOperators._1().append(evalId).append("\t").append(improvement).append("\n");
                    allOperators._2().append(evalId).append("\t").append(p_improvement).append("\n");
                }

            }



            if (evalId % 100 == 0) {
                Log.it(evalId+" log files read..");
            }
        }

        //Log.it(fitnesses.toString());

        mkDir(resultsStatsDir);

        mkDir(experimentDir);

        F.writeFile(experimentDir+"/fitness.txt", fitnesses.toString());
        F.writeFile(experimentDir+"/fitness2.txt", fitnesses2.toString());


        for (Map.Entry<String,AA<StringBuilder>> e : operators.entrySet()) {
            String operatorName = e.getKey();

            AA<StringBuilder> sbPair = e.getValue();

            String tableStr_1 = sbPair._1().toString();
            String tableStr_2 = sbPair._2().toString();

            F.writeFile(experimentDir+"/"+operatorName+"_1.txt", tableStr_1);
            F.writeFile(experimentDir+"/"+operatorName+"_2.txt", tableStr_2);
        }

        F.writeFile(experimentDir+"/allOperators_1.txt", allOperators._1().toString());
        F.writeFile(experimentDir+"/allOperators_2.txt", allOperators._2().toString());



        Log.it("Starts computing fronts...");
        AB<MultiIndiv,Integer> assignRes = MultiUtils.assignFrontsAndDistances_martin(indivs, Arrays.asList(true, false)); // TODO načíst isMaxis pořádně !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!§
        int numFronts = assignRes._2();
        Log.it("numFronts="+numFronts);

        StringBuilder front1 = new StringBuilder();
        front1.append("id \t fitness1 \t fitness2..\n");

        for (MultiIndiv mi : indivs) {
            int front = mi.getFront();
            if (front == 0) {
                throw new Error("WTF front 0!");
            }
            if (front == 1) {
                front1.append(mi.getId()).append("\t").append(Joiner.on("\t").join(mi.getFitness())).append("\n");
            }
        }

        F.writeFile(experimentDir+"/front1.txt", front1.toString());

    }

    private static double sanitizeFitness(double rawFitVal) {
        return Math.max(rawFitVal, -0.1);
    }

    private static double getSingeFitness(JSONObject indivData) {
        Object fitVal = indivData.get("fitness");
        if (fitVal instanceof Double) {
            return sanitizeFitness((double) fitVal);
        } else if (fitVal instanceof JSONArray) {
            JSONArray fitArr = (JSONArray) fitVal;
            return sanitizeFitness(fitArr.getDouble(0));
        } else {
            throw new Error("Unsupported fitness format.");
        }
    }

    private static List<Double> getFitnessList(JSONObject indivData) {
        return F.map(indivData.getJSONArray("fitness"), x->Double.valueOf(x.toString()));
    }


    private static String readFile(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
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
