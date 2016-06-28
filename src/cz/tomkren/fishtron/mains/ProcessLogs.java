package cz.tomkren.fishtron.mains;

import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Created by tom on 28. 6. 2016.*/

public class ProcessLogs {

    public static void main(String[] args) throws IOException {

        String runDirPath = "C:\\Users\\user\\Desktop\\logs.wine.1\\media\\logs\\tom\\run_1";
        String evalsDirPath = runDirPath + "\\evals";

        StringBuilder fitnesses = new StringBuilder();

        for (int evalId = 1; evalId < 10; evalId++) {




            String filePath = evalsDirPath + "\\eval_" + evalId + ".json";

            JSONObject logJson = new JSONObject(readFile(filePath, StandardCharsets.UTF_8));

            double fitness = logJson.getJSONArray("evalResult").getJSONObject(0).getJSONObject("indivData").getDouble("fitness");

            fitnesses.append(evalId).append("\t").append(fitness).append("\n");
        }

        Log.it(fitnesses.toString());


    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
