package cz.tomkren.fishtron.server;

import cz.tomkren.fishtron.eva.EvaledPop;
import cz.tomkren.fishtron.eva.Logger;
import cz.tomkren.fishtron.sandbox2.Evolution;
import cz.tomkren.fishtron.sandbox2.EvolutionOpts;
import cz.tomkren.fishtron.sandbox2.JsonEvolutionOpts;
import cz.tomkren.fishtron.terms.PolyTree;
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

        setStatus(Status.beforeStart);
        output = new StringBuffer();

        opts = new JsonEvolutionOpts(jsonOpts).getDirectOpts();
        //Logger<PolyTree> logger = new Logger.Basic<>(opts);

        eva = new Evolution<>(opts, this);

    }

    public void start() {

        setStatus(Status.running);

        (new Thread(()->{
            eva.startIterativeEvolution(1);
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
    public void iterativeLog(int run, int evaluationIndex, EvaledPop<PolyTree> pop) {

        PolyTree best = pop.getBestIndividual();
        log("eval # "+evaluationIndex+ (opts==null ? "" : " / "+opts.getNumEvaluations() ) +", best: "+ best.getWeight());
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
