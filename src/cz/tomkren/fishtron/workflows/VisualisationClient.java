package cz.tomkren.fishtron.workflows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import cz.tomkren.utils.Log;

/** Created by Tom on 12. 5. 2016. */

public class VisualisationClient {

    public static void main(String[] args) throws Exception {

        VisualisationClient client = new VisualisationClient("127.0.0.1", 4223);
        Log.it(client.runCmd("add $main {}"));

    }

    private String serverUrl;
    private int port;

    private VisualisationClient(String serverUrl, int port) {
        this.serverUrl = serverUrl;
        this.port = port;
    }

    private String runCmd(String cmdStr) throws Exception {

        URL url = new URL("http://"+ serverUrl +":"+ port +"/?resultFormat=json&cmd=" + URLEncoder.encode(cmdStr, "UTF-8"));

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



}