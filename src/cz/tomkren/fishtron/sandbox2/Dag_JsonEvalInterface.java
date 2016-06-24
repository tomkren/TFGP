package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.json.JSONArray;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** Created by tom on 21.2.2016.*/

class Dag_JsonEvalInterface {

    private XmlRpcClient client;


    Dag_JsonEvalInterface(String evaluatorURL) {
        try {

            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(evaluatorURL));
            client = new XmlRpcClient();
            client.setConfig(config);

            Log.it("Client communicating with evaluator at "+evaluatorURL);

        } catch (java.net.MalformedURLException e) {
            System.err.println("JavaClient: " + e);
            //throw new Error(e);
        }
    }

    String getMethodParams(String methodName, String datasetFilename) {
        try {
            Checker checker = new Checker(true);


            List<Object> params = Collections.emptyList();
            Object result = client.execute(methodName, params);

            Log.it("getMethodParams completed in "+ checker.getTime()+" seconds.");

            return (String) result;

        } catch (Exception e) {
            System.err.println("JavaClient: " + e);
            //throw new Error(e);
            return null;
        }
    }

    Object submit(String methodName, JSONArray indivs) {
        try {
            Checker checker = new Checker(true);

            Log.it_noln("Sanding "+ (indivs==null ? "null" : indivs.length()) +" individuals to "+methodName+"  ... ");

            List<Object> params = Collections.singletonList(indivs == null ? "" : indivs.toString());
            Object result = client.execute(methodName, params);

            Log.it("Evaluation completed in "+ checker.getTime()+" seconds. Submit msg: "+result);

            return result;

        } catch (Exception e) {
            System.err.println("JavaClient: " + e);
            //throw new Error(e);
            return null;
        }
    }

    <A> List<A> getEvaluated(String methodName, Function<Object, A> castFun) {
        try {
            Checker checker = new Checker(true);

            Log.it_noln("Getting evaluated individuals back ... ");

            List<Object> params = Collections.emptyList();
            Object[] results = (Object[]) client.execute(methodName, params);

            Log.it("completed in "+ checker.getTime()+" seconds.");

            return F.map(results, castFun);

        } catch (Exception e) {
            System.err.println("JavaClient: " + e);
            //throw new Error(e);
            return null;
        }
    }

    public <A> A get(String methodName, Function<Object,A> castFun) {
        return evalObject(methodName, "", castFun);
    }

    public int getInt(String methodName) {
        return get(methodName, x->(int)x);
    }

    private <A> A evalObject(String methodName, Object input, Function<Object, A> castFun) {
        try {
            Checker checker = new Checker(true);

            if ("".equals(input)) {
                Log.it_noln("Getting property "+methodName+" ...");
            } else {
                Log.it_noln("Sanding input object to "+methodName+" ...");
            }

            List<Object> params = Collections.singletonList(input);
            Object result = client.execute(methodName, params);

            Log.it("Evaluation completed in "+ checker.getTime()+" seconds.");

            return castFun.apply(result);

        } catch (Exception e) {
            System.err.println("JavaClient: " + e);
            //throw new Error(e);
            return null;
        }
    }






}
