package cz.tomkren.fishtron.workflows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/** Created by Tom on 12. 5. 2016. */

public class VisualisationClient {

    public static void main(String[] args) throws Exception {

        VisualisationClient client = new VisualisationClient("127.0.0.1", 4223);
        //Log.it(client.addArray(F.arr(F.obj("pos","10 10"), F.obj("pos","20 20"))));

        //String workflowStr = "[{\"shape\":\"f 1 1 DT\",\"pos\":\"38 32\",\"id\":\"$v_1\"}]";
        //String workflowStr = "[{\"shape\":\"f 1 2 kMeans $v_11:0 $v_12:0\",\"pos\":\"70 32\",\"id\":\"$v_10\"},{\"shape\":\"f 1 1 logR $v_13:0\",\"pos\":\"38 96\",\"id\":\"$v_11\"},{\"shape\":\"f 1 1 logR $v_13:1\",\"pos\":\"102 96\",\"id\":\"$v_12\"},{\"shape\":\"f 2 1 vote\",\"pos\":\"70 160\",\"id\":\"$v_13\"}]\n";
        //String workflowStr = "[{\"shape\":\"f 1 7 kMeans $v_943:0 $v_944:0 $v_945:0 $v_946:0 $v_947:0 $v_948:0 $v_949:0\",\"pos\":\"230 32\",\"id\":\"$v_942\"},{\"shape\":\"f 1 1 logR $v_955:1\",\"pos\":\"102 192\",\"id\":\"$v_944\"},{\"shape\":\"f 1 1 booBegin $v_950:0\",\"pos\":\"166 96\",\"id\":\"$v_945\"},{\"shape\":\"f 1 1 logR $v_955:3\",\"pos\":\"230 192\",\"id\":\"$v_946\"},{\"shape\":\"f 1 1 logR $v_955:4\",\"pos\":\"294 192\",\"id\":\"$v_947\"},{\"shape\":\"f 1 1 logR $v_955:5\",\"pos\":\"358 192\",\"id\":\"$v_948\"},{\"shape\":\"f 1 1 DT $v_955:6\",\"pos\":\"422 192\",\"id\":\"$v_949\"},{\"shape\":\"f 1 1 logR $v_955:0\",\"pos\":\"38 192\",\"id\":\"$v_943\"},{\"shape\":\"f 1 1 booster:#000000 $v_952:0\",\"pos\":\"166 160\",\"id\":\"$v_950\",\"inside\":[{\"shape\":\"f 1 1 logR\",\"pos\":\"138 132\",\"id\":\"$v_951\"}]},{\"shape\":\"f 7 1 vote\",\"pos\":\"230 352\",\"id\":\"$v_955\"},{\"shape\":\"f 1 1 booster:#000000 $v_954:0\",\"pos\":\"166 224\",\"id\":\"$v_952\",\"inside\":[{\"shape\":\"f 1 1 logR\",\"pos\":\"138 132\",\"id\":\"$v_953\"}]},{\"shape\":\"f 1 1 booEnd $v_955:2\",\"pos\":\"166 288\",\"id\":\"$v_954\"}]";
        String workflowStr = "[{\"shape\":\"f 1 1 booBegin $v_1414:0\",\"pos\":\"38 32\",\"id\":\"$v_1431\"},{\"shape\":\"f 1 1 booster:#999999 $v_1416:0\",\"pos\":\"38 96\",\"id\":\"$v_1414\",\"inside\":[{\"shape\":\"f 1 1 logR\",\"pos\":\"138 132\",\"id\":\"$v_1415\"}]},{\"shape\":\"f 1 1 booster:#424242 $v_1418:0\",\"pos\":\"38 160\",\"id\":\"$v_1416\",\"inside\":[{\"shape\":\"f 1 1 logR\",\"pos\":\"138 132\",\"id\":\"$v_1417\"}]},{\"shape\":\"f 1 1 booster:#424242 $v_1420:0\",\"pos\":\"38 224\",\"id\":\"$v_1418\",\"inside\":[{\"shape\":\"f 1 1 logR\",\"pos\":\"138 132\",\"id\":\"$v_1419\"}]},{\"shape\":\"f 1 1 booster:#424242 $v_1427:0\",\"pos\":\"38 288\",\"id\":\"$v_1420\",\"inside\":[{\"shape\":\"f 1 1 booBegin $v_1422:0\",\"pos\":\"138 132\",\"id\":\"$v_1421\"},{\"shape\":\"f 1 1 booster:#424242 $v_1424:0\",\"pos\":\"138 196\",\"id\":\"$v_1422\",\"inside\":[{\"shape\":\"f 1 1 DT\",\"pos\":\"138 132\",\"id\":\"$v_1423\"}]},{\"shape\":\"f 1 1 booster:#424242 $v_1426:0\",\"pos\":\"138 260\",\"id\":\"$v_1424\",\"inside\":[{\"shape\":\"f 1 1 logR\",\"pos\":\"138 132\",\"id\":\"$v_1425\"}]},{\"shape\":\"f 1 1 booEnd\",\"pos\":\"138 324\",\"id\":\"$v_1426\"}]},{\"shape\":\"f 1 1 booster:#424242 $v_1429:0\",\"pos\":\"38 352\",\"id\":\"$v_1427\",\"inside\":[{\"shape\":\"f 1 1 DT\",\"pos\":\"138 132\",\"id\":\"$v_1428\"}]},{\"shape\":\"f 1 1 booster:#424242 $v_1432:0\",\"pos\":\"38 416\",\"id\":\"$v_1429\",\"inside\":[{\"shape\":\"f 1 1 DT\",\"pos\":\"138 132\",\"id\":\"$v_1430\"}]},{\"shape\":\"f 1 1 booEnd\",\"pos\":\"38 480\",\"id\":\"$v_1432\"}]";

        JSONArray workflowKutilJson = new JSONArray(workflowStr);

        Log.it(client.addArray(workflowKutilJson));
    }

    private String serverUrl;
    private int port;

    public VisualisationClient(String serverUrl, int port) {
        this.serverUrl = serverUrl;
        this.port = port;
    }

    private String addObject(JSONObject kutilObject) throws Exception {
        return add(kutilObject);
    }

    private String addArray(JSONArray kutilObjects) throws Exception {
        return add(kutilObjects);
    }

    private String add(Object o) throws Exception {

        JSONObject cmdObject = F.obj(
            "cmd", "add",
            "parentId", "$main",
            "object", o
        );

        return sendCmd("json "+cmdObject);
    }

    private String sendCmd(String cmdStr) throws Exception {

        URL url = new URL("http://"+ serverUrl +":"+ port +
                "/?emptyResponse=true&resultFormat=json&cmd=" + URLEncoder.encode(cmdStr, "UTF-8"));

        URLConnection conn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder serverState = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            serverState.append(inputLine).append('\n');
        }

        in.close();
        return serverState.toString();
    }



    public void showDags(List<TypedDag> dags) {



        int width = (int)( 18000 / Math.sqrt(1000) * Math.sqrt(dags.size()) ) ; //16000;
        int okraj = 20;
        int init  = 3*okraj;

        int x = init;
        int y = init;

        int maxHeight = 0;

        try {

            sendCmd("clearInside $main");

            for (TypedDag dag : dags) {

                addArray( dag.toKutilJson(x,y) );

                x += dag.getPxWidth() + okraj;

                if (dag.getPxHeight() > maxHeight) {
                    maxHeight = dag.getPxHeight();
                }

                if (x > width) {
                    x = init;
                    y += maxHeight + okraj;
                }
            }

        } catch (Exception e) {
            Log.err("(!!!) VisualisationClint exception : " + e.getMessage());
        }
    }


}