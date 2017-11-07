package net.fishtron.server.jobs.templates;

import net.fishtron.server.api.Api;
import net.fishtron.server.jobs.EvaJob;
import net.fishtron.server.jobs.JobContainer;
import net.fishtron.utils.AA;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by tom on 26.08.2017.
 */
public class TestFactorization implements EvaJob {

    public static String getJobName() {return "TestFactorization";}

    private JobContainer jobContainer;
    private String lastLog;

    private synchronized String getLastLog() {return lastLog;}
    private synchronized void setLastLog(String lastLog) {this.lastLog = lastLog;}

    @Override
    public JSONObject runJob(EvaJob.Config jobConfig, JobContainer jobContainer) {

        this.jobContainer = jobContainer;

        int howMany = jobConfig.getOpts().optInt("howMany", 42);
        jobConfig.json().put("howMany", howMany);

        Random rand = new Random();

        for (int i = 1; i <= howMany; i++) {
            long n = rand.nextLong();
            String log = "("+i+") "+n+" = " + String.join(" * ", F.map(packFactors(F.factorize(n)), p -> p._1()+(p._2()==1?"":"^"+p._2())));
            jobContainer.log(log);
            setLastLog(log);
        }

        return Api.ok("msg", "Poslušně hlásim finiš!");
    }

    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        if (query.getString(Api.KEY_jobCmd).equals("lastLog")) {
            jobContainer.log("Někdo chce last log tý vole.................");
            return Api.ok("lastLog", getLastLog());
        } else {
            jobContainer.log("Olol, někdo mi zavolal moje osobní Jobový apíčko, mě testovacího EvaJoba, jaká pocta!");
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

}
