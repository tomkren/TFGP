package cz.tomkren.fishtron.server;

import net.fishtron.eva.simple.EvaledPop;
import net.fishtron.eva.simple.Logger;
import net.fishtron.eva.simple.EvalResult;
import net.fishtron.eva.simple.Evolution;
import net.fishtron.eva.simple.EvolutionOpts;
import cz.tomkren.fishtron.sandbox2.JsonEvolutionOpts;
import cz.tomkren.fishtron.terms.PolyTree;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 13.6.2016.*/

public class EvolutionJob implements Logger<PolyTree> {
    private enum Status {beforeStart, running, finished};


    private Evolution<PolyTree> eva;
    private EvolutionOpts<PolyTree> opts;

    private Status status;
    private StringBuffer output;


    public EvolutionJob(JSONObject jsonOpts) {

        try {

            setStatus(Status.beforeStart);
            output = new StringBuffer();

            opts = new JsonEvolutionOpts(jsonOpts).getDirectOpts();
            //Logger<PolyTree> logger = new Logger.Basic<>(opts);

            eva = new Evolution<>(opts, this);

        } catch (XmlRpcException e) {
            throw new Error(e);
        }

    }

    public void start() {

        setStatus(Status.running);

        (new Thread(()->{
            eva.startIterativeEvolution_old(1);
            setStatus(Status.finished);
        })).start();

    }

    public synchronized Status getStatus() {return status;}
    private synchronized void setStatus(Status newStatus) {status = newStatus;}

    public String getLog() {
        return output.toString();
    }



    private void log(Object x) {
        output.append(x).append('\n');
    }

    private void logErr(Object x) {
        output.append("!!! ERROR !!! : ").append(x).append('\n');
        System.err.println(x);
    }


    @Override
    public void iterativeLog(int run, int numEvaluatedIndivs, EvaledPop<PolyTree> pop, EvalResult<PolyTree> evalResult) {

        PolyTree best = pop.getBestIndividual();
        log("eval # "+ numEvaluatedIndivs + (opts==null ? "" : " / "+opts.getNumEvaluations() ) +", best: "+ best.getWeight());
        log("  "+best.toString()+"\n");

    }

    @Override
    public void logRun(int run) {
        log("-- RUN "+run+" FINISHED -----------------------");
    }

    @Override
    public void logPop(int run, int generation, EvaledPop<PolyTree> pop) {
        PolyTree best = pop.getBestIndividual();
        log("gen "+generation+", best "+best.getWeight());
        log("  "+best.toString()+"\n");
    }

    @Override
    public void logErrorIndivs(int generation, List<Object> errorIndivs) {
        logErr("ERROR INDIVS !!!");
        for (Object errInd: errorIndivs) {
            logErr("  "+errInd.toString());
        }
    }
}
