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
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**Created by tom on 22.03.2017.*/

public class InteractiveComparator implements Api {

    private static final String CMD_GET_PAIR_TO_COMPARE = "getPairToCompare";
    private static final String CMD_OFFER_RESULT = "offerResult";

    private static final String I1_WINS = "i1wins";
    private static final String PAIR = "pair";
    private static final String RESULT = "result";
    private static final String FRAME = "frame";

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
            if (i % 50 == 0) {ch.log("("+(i/50)+") Waiting for user to compare pair...");}
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
        if (!(i1wins instanceof Boolean)) {return false;}

        if (!result.has(FRAME)) {return false;}
        Object frame = result.get(FRAME);
        if (!(frame instanceof String)) {return false;}

        return true;
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

        List<String> frames = Libs.genPhenotype(cellOpts, seedFilename, rule, numFrames, runDirPath, indivId, ch); // GENERATING FRAMES

        return F.obj(
                "id", indivId,
                "pair", indiv.getTree().toString(),
                "frames", F.jsonMap(frames)
        );
    }


    static JSONObject mkInitializingResponse() {
        return F.obj(Api.STATUS, "initializing");
    }


    @Override
    public JSONObject processApiCall(JSONArray path, JSONObject query) {
        String jobCmd = query.optString(Api.JOB_CMD, "WRONG FORMAT OR MISSING, MUST BE STRING");
        switch (jobCmd) {

            case CMD_GET_PAIR_TO_COMPARE: return api_getPairToCompare();
            case CMD_OFFER_RESULT:        return api_offerResult(query);

            default: return Api.error("Unsupported " + Api.JOB_CMD + ": " + jobCmd);
        }
    }

    private JSONObject api_getPairToCompare() {
        JSONArray pairToCompare = indivPairsToCompare.peek();
        return Api.ok(
            PAIR, pairToCompare == null ? JSONObject.NULL : pairToCompare
        );
    }

    private JSONObject api_offerResult(JSONObject query) {
        if (!query.has(RESULT) || !(query.get(RESULT) instanceof JSONObject)) {return Api.error("Wrong result format.");}

        JSONObject result = query.getJSONObject(RESULT);
        if (!checkResult(result)) {return Api.error("Wrong (inner) result format.");}

        comparedInivPairs.offer(result);


        String framePath = result.getString(FRAME);

        File source    = new File(framePath);
        File source1px = new File(runDirPath+"/frames/px1/"+source.getName());

        String destination    = runDirPath + "/winners/";
        String destination1px = runDirPath + "/winners/px1/";

        File target    = new File(destination    + source.getName());
        File target1px = new File(destination1px + source.getName());

        try {
            Files.copy(source.toPath(),    target.toPath(),    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source1px.toPath(), target1px.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e) {
            System.err.println("Unable to save winner img: "+ e.getMessage());
        }

        Log.it(framePath +" -> "+ target.toString());

        return Api.ok(MSG, "Thanks!");
    }


}
