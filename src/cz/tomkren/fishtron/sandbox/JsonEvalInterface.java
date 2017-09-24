package cz.tomkren.fishtron.sandbox;

import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import org.json.JSONArray;

import java.net.URL;
import java.util.*;
import java.util.function.Function;

/** Created by tom on 21.2.2016.*/

public class JsonEvalInterface {

    private XmlRpcClient client;

    /**
     * Constructor of the evaluator interface.
     *
     * @param evaluatorURL The URL of the evaluating server e.g. http://localhost:8080
     */
    public JsonEvalInterface(String evaluatorURL) {
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

    public <A> List<A> eval(String methodName, JSONArray indivs, Function<Object,A> castFun) {
        try {
            Checker checker = new Checker(true);

            Log.it_noln("Sanding "+ (indivs==null ? "null" : indivs.length()) +" individuals to "+methodName+" ... ");

            List<Object> params = Collections.singletonList(indivs == null ? "" : indivs.toString());
            Object[] results = (Object[]) client.execute(methodName, params);

            Log.it("Evaluation completed in "+ checker.getTime()+" seconds.");

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

    public <A> A evalObject(String methodName, Object input, Function<Object,A> castFun) {
        try {
            Checker checker = new Checker(true);

            if ("".equals(input)) {
                Log.it_noln("Getting property "+methodName+" ... ");
            } else {
                Log.it_noln("Sanding input object to "+methodName+" ... ");
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

    /* examples of castFuns..
       x->(int)x
       x->(double)(int)x
       public static double toIntToDouble(Object i) {return (double)(int)i;}
       public static int toInt(Object i) {return (int)i;} */

    public static void main (String [] args) {
        Checker ch = new Checker();


        JsonEvalInterface evaluator = new JsonEvalInterface("http://localhost:8080");

        String antStr1 = "[\"ifa\", \"m\", \"r\"]";
        String antStr2 = "[\"ifa\", \"m\", [\"pr3\", \"l\", [\"pr2\", [\"ifa\", \"m\", \"r\"], [\"pr2\", \"r\", [\"pr2\", \"l\", \"r\"]]], [\"pr2\", [\"ifa\", \"m\", \"l\"], \"m\"]]]";

        JSONArray antsJson = new JSONArray("["+antStr2+","+antStr1+","+antStr2+"]");

        List<Integer> results = evaluator.eval("evalAnts", antsJson, x->(int)x);

        ch.it(results, "[89, 11, 89]");

        ch.results();
    }




}
