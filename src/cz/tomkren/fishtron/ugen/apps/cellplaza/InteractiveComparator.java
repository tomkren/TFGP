package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.CellOpts;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Libs;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Rule;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.server.Api;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**Created by tom on 22.03.2017.*/

public class InteractiveComparator implements Api {

    private static final String CMD_GET_PAIR_TO_COMPARE = "getPairToCompare";
    private static final String CMD_OFFER_RESULT = "offerResult";

    private static final String I1_WINS = "i1wins";
    private static final String PAIR = "pair";
    private static final String RESULT = "result";

    private final Checker ch;
    private final EvalLib lib;
    private final CellOpts cellOpts;

    private final int numFrames;
    private final String runDirPath;

    private int nextId;

    private Queue<JSONArray> indivPairsToCompare;
    private Queue<JSONObject> comparedInivPairs;

    private long sleepTime;

    InteractiveComparator(EvalLib lib, CellOpts cellOpts, int numFrames, String runDirPath, long sleepTime, Checker ch) {
        this.lib = lib;
        this.ch = ch;

        this.cellOpts = cellOpts;
        this.numFrames = numFrames;
        this.runDirPath = runDirPath;

        this.sleepTime = sleepTime;

        nextId = 1;

        indivPairsToCompare = new ConcurrentLinkedQueue<>();
        comparedInivPairs = new ConcurrentLinkedQueue<>();
    }


    public boolean compare(AppTreeMI indiv1, AppTreeMI indiv2) {

        JSONObject indivJson1 = indivToJson(indiv1);
        JSONObject indivJson2 = indivToJson(indiv2);

        JSONArray indivPair = F.arr(indivJson1,indivJson2);
        indivPairsToCompare.offer(indivPair);

        JSONObject result = comparedInivPairs.poll();
        int i = 0;
        while (result == null) {
            if (i % 20 == 0) {ch.log("("+(i/20)+") Waiting for user to compare pair...");}
            F.sleep(sleepTime);
            result = comparedInivPairs.poll();
            i++;
        }

        indivPairsToCompare.remove(indivPair);

        if (!result.has(I1_WINS)) {throw new Error("Compare result must have "+I1_WINS+" key.");}
        Object i1wins = result.get(I1_WINS);
        if (!(i1wins instanceof Boolean)) {throw new Error(I1_WINS+" must be boolean.");}

        // todo reflect frame selection !

        return (boolean) i1wins;
    }

    private static boolean checkResult(JSONObject result) {
        if (!result.has(I1_WINS)) {return false;}
        Object i1wins = result.get(I1_WINS);
        return i1wins instanceof Boolean;
    }


    private JSONObject indivToJson(AppTreeMI indiv) {
        int indivId = nextId;
        nextId ++;

        Object indivValue = indiv.computeValue(lib); // INDIVIDUAL EVALUATION

        if (!(indivValue instanceof AB)) {throw new Error("Wrong indivValue type.");}
        AB p = (AB)indivValue;
        Object seedFilenameObj = p._1();
        Object ruleObj = p._2();
        if (!(seedFilenameObj instanceof String) || !(ruleObj instanceof Rule)) {throw new Error("Wrong sub-indivValue type.");}

        String seedFilename = (String) seedFilenameObj;
        Rule rule = (Rule) ruleObj;

        List<String> frames = Libs.genPhenotype(cellOpts, seedFilename, rule, numFrames, runDirPath, indivId, ch);

        return F.obj(
                "id", indivId,
                "pair", indiv.getTree().toString(),
                "frames", F.jsonMap(frames)
        );
    }



    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {

        String jobCmd = query.optString(Api.JOB_CMD, "WRONG FORMAT OR MISSING, MUST BE STRING");

        switch (jobCmd) {
            case CMD_GET_PAIR_TO_COMPARE:

                JSONArray pairToCompare = indivPairsToCompare.peek();

                return Api.ok(
                        PAIR, pairToCompare == null ? JSONObject.NULL : pairToCompare
                );


            case CMD_OFFER_RESULT:

                if (query.has(RESULT) && (query.get(RESULT) instanceof JSONObject)) {

                    JSONObject result = query.getJSONObject(RESULT);

                    if (checkResult(result)) {
                        comparedInivPairs.offer(result);
                        return Api.ok(MSG, "Thanks!");
                    } else {
                        return Api.error("Wrong (inner) result format.");
                    }

                } else {
                    return Api.error("Wrong result format.");
                }


            default:
                return Api.error("Unsupported " + Api.JOB_CMD + ": " + jobCmd);
        }
    }


}
