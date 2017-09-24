package net.fishtron.server.jobs;

import net.fishtron.server.Api;
import net.fishtron.server.EvaJob;
import net.fishtron.server.EvaJobProcess;
import cz.tomkren.utils.AA;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/** Created by tom on 05.03.2017. */

public class Test implements EvaJob {

    private String lastLog;
    private EvaJobProcess jobProcess;

    @Override
    public void runJob(JSONObject jobOpts, EvaJobProcess jobProcess) {

        this.jobProcess = jobProcess;

        int howMany = jobOpts.optInt("howMany", 100);
        jobOpts.put("howMany", howMany);

        Random rand = new Random();

        for (int i = 1; i <= howMany; i++) {
            long n = rand.nextLong();
            String log = "("+i+") "+n+" = " + String.join(" * ", F.map(packFactors(F.factorize(n)), p -> p._1()+(p._2()==1?"":"^"+p._2())));
            jobProcess.log(log);
            setLastLog(log);
        }

        jobProcess.log("Poslušně hlásim finiš!");
    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        if (query.getString(Api.JOB_CMD).equals("lastLog")) {

            jobProcess.log("Někdo chce last log tý vole.................");
            return Api.ok("lastLog", getLastLog());

        } else {

            jobProcess.log("Olol, někdo mi zavolal moje osobní Jobový apíčko, mě testovacího EvaJoba, jaká pocta!");
            return Api.ok("you asked maaan", F.obj("path",path, "query",query));

        }
    }

    private static List<AA<Long>> packFactors(List<Long> fs) {
        List<AA<Long>> ret = new ArrayList<>();
        long last = -2;
        long exponent = 0;
        for (long f : fs) {
            if (f == last) {
                exponent ++;
            } else {
                if (exponent > 0) {
                    ret.add(AA.mk(last, exponent));
                }
                last = f;
                exponent = 1;
            }
        }
        ret.add(AA.mk(last,exponent));
        return ret;
    }




    private synchronized String getLastLog() {return lastLog;}
    private synchronized void setLastLog(String lastLog) {this.lastLog = lastLog;}



}
