package com.martinpilat;

/** Created by Martin on 25.6.2015. */

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;


public class DagEvalInterface {

    XmlRpcClient server;

    /**
     * Constructor of the evaluator interface.
     *
     * @param url The URL of the evaluating server e.g. http://localhost:8080
     * @throws java.net.MalformedURLException
     */
    public DagEvalInterface(String url) throws java.net.MalformedURLException {

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(url));
        this.server = new XmlRpcClient();
        this.server.setConfig(config);

    }

    /**
     * Evaluates the list of machine learning workflows. Calls the Python server internally.
     *
     * @param jsonString The list of workflows encoded as a JSON string.
     * @param dataFile The dataset to use for evaluation
     * @return List of arrays of scores
     * @throws XmlRpcException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<double[]> eval(String jsonString, String dataFile) throws XmlRpcException {

        Vector params = new Vector();
        params.addElement(jsonString);
        params.addElement(dataFile);

        Object[] result = (Object[])this.server.execute("eval", params);

        ArrayList<double[]> res = new ArrayList<>();
        for (Object o : result) {
            Object[] oarr = (Object[])o;
            double[] darr = new double[oarr.length];
            for (int i = 0; i < oarr.length; i++) {
                darr[i] = (Double)oarr[i];
            }
            res.add(darr);
        }

        return res;
    }


    /**
     * Returns a JSON string with the possible values of parameters of all supported methods. Some parameters depend
     * on the dataset for which the methods will be applied.
     *
     * @param dataFile The name of the dataset for which the parameters should be generated.
     * @return The JSON string with the description of parameters.
     * @throws XmlRpcException
     */
    @SuppressWarnings("unchecked")
    public String getMethodParams(String dataFile) throws XmlRpcException {
        Vector params = new Vector();
        params.addElement(dataFile);

        return (String) this.server.execute("get_param_sets", params);
    }

    public void killServer() {
        try {
            Vector params = new Vector();
            this.server.execute("quit", params);
        } catch (Exception ignored) { }
    }

    public static void main(String[] args) {

        try {
            DagEvalInterface evaluator = new DagEvalInterface("http://127.0.0.1:8080");

            System.out.println(evaluator.getMethodParams("wilt.csv"));

            String dagStr   =  "[";
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], [\"SVC\", {\"C\": 8, \"gamma\": 0.0001}], [] ]},";
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], [\"lsdfogR\", {}], [] ]},"; //this should fail
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], \"DT\", [] ]},";
            dagStr          += "{\"input\" : [ [], \"input\", [\"1:0\"] ], \"1\" : [ [\"1:0\"], [\"gaussianNB\", {}], [] ]}";
            dagStr          += "]";

            ArrayList<double[]> scores = evaluator.eval(dagStr, "wilt.csv");

            evaluator.killServer();

            for (double[] darr : scores) {
                if (darr.length == 0)
                    System.out.println("Evaluation error");
                else
                    System.out.println(darr[0]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
