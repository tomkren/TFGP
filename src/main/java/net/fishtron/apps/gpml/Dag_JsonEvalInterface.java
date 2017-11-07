package net.fishtron.apps.gpml;

import net.fishtron.utils.Checker;
import net.fishtron.utils.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.json.JSONArray;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** Created by tom on 21.2.2016.*/

public class Dag_JsonEvalInterface {

    private XmlRpcClient client;

    public Dag_JsonEvalInterface(String evaluatorURL) {
        try {

            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(evaluatorURL));
            client = new XmlRpcClient();
            client.setConfig(config);

            Log.it("Client communicating with evaluator at "+evaluatorURL);

        } catch (java.net.MalformedURLException e) {
            System.err.println("JavaClient(1): " + e);
            //throw new Error(e);
        }
    }

    public String getMethodParams(String methodName, String datasetFilename) throws XmlRpcException {
        //try {
            Checker checker = new Checker(true);


            List<Object> params = Collections.singletonList(datasetFilename);
            Object result = client.execute(methodName, params);

            Log.it("getMethodParams completed in " + checker.getTime() + " seconds.");

            return (String) result;
    }

    public String quitServer() {
        try {
            return (String) client.execute("quit", Collections.emptyList());
        } catch (Exception ignored) {
            return "Server probably refused to be executed.";
        }
    }

    public Object submit(String methodName, JSONArray indivs, String datasetFilename) {
        try {
            Checker checker = new Checker(true);

            Log.it_noln("Sanding "+ (indivs==null ? "null" : indivs.length()) +" individuals to "+methodName+"  ... ");

            List<Object> params = Arrays.asList(indivs == null ? "[]" : indivs.toString(), datasetFilename);
            Object result = client.execute(methodName, params);

            Log.it("submit completed in "+ checker.getTime()+" seconds. Submit msg: "+result);

            return result;

        } catch (Exception e) {
            System.err.println("JavaClient(3): " + e);
            //throw new Error(e);
            return null;
        }
    }

    public JSONArray getEvaluated(String methodName) {
        try {

            List<Object> params = Collections.emptyList();
            String resultJsonStr = (String) client.execute(methodName, params);
            return new JSONArray(resultJsonStr);

        } catch (Exception e) {
            System.err.println("JavaClient(4): " + e);
            //throw new Error(e);
            return null;
        }
    }

    public <A> A get(String methodName, Function<Object,A> castFun) {
        return evalObject(methodName, null, castFun);
    }

    public int getInt(String methodName) {
        return get(methodName, x->Integer.parseInt((String)x));
    }

    private <A> A evalObject(String methodName, Object input, Function<Object, A> castFun) {
        try {
            Checker checker = new Checker(true);

            List<Object> params;

            if (input == null) {
                Log.it_noln("Getting property "+methodName+" ...");
                params = Collections.emptyList();
            } else {
                Log.it_noln("Sanding input object to "+methodName+" ...");
                params = Collections.singletonList(input);
            }

            Object result = client.execute(methodName, params);

            Log.it("Evaluation completed in "+ checker.getTime()+" seconds.");

            return castFun.apply(result);

        } catch (Exception e) {
            System.err.println("JavaClient(5): " + e);
            //throw new Error(e);
            return null;
        }
    }






}
