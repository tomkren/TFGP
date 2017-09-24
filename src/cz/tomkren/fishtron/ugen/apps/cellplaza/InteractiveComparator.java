package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.apps.cellplaza.shared.PlazaImg;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.CellOpts;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Libs;
import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.Rule;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import net.fishtron.server.Api;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
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
    private static final String CMD_HISTORY = "history";
    private static final String CMD_HISTORY_VERSION = "historyVersion";
    private static final String CMD_ZOOM = "zoom";



    private static final String I1_WINS = "i1wins";
    private static final String PAIR = "pair";
    private static final String RESULT = "result";
    private static final String FRAME = "frame";
    private static final String IDS = "ids";
    private static final String INDIV_ID = "indivId";

    private final Checker ch;
    private final EvalLib lib;
    private final CellOpts cellOpts;

    private final int numFrames;
    private final String runDirPath;

    private int nextId;

    private Queue<JSONArray> indivPairsToCompare;
    private Queue<JSONObject> comparedInivPairs;

    private final History history;

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

        history = new History(cellOpts.getNumStates());
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

        if (!result.has(IDS)) {return false;}
        Object ids_obj = result.get(IDS);
        if (!(ids_obj instanceof JSONArray)) {return false;}
        JSONArray ids = (JSONArray) ids_obj;
        if (ids.length() != 2 || !(ids.get(0) instanceof Integer) || !(ids.get(1) instanceof Integer)) {return false;}

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
            case CMD_HISTORY:             return Api.addOk(history.toJson());
            case CMD_HISTORY_VERSION:     return Api.ok("version",history.getVersion());
            case CMD_ZOOM:                return api_zoom(query);

            default: return Api.error("Unsupported " + Api.JOB_CMD + ": " + jobCmd);
        }
    }

    private JSONObject api_zoom(JSONObject query) {

        String plazaPath = query.optString("plaza");
        JSONArray tilePaths  = query.optJSONArray("tiles");

        String dir = runDirPath +"/zooms";

        File plazaFile = new File(plazaPath);

        PlazaImg plazaImg = new PlazaImg(plazaFile);

        String newFilename = plazaFile.getName() +"_with_"+ String.join("_", F.map(tilePaths,x-> new File((String)x).getName() ))+".png";

        PlazaImg zoomedImg = plazaImg.zoom(tilePaths);

        zoomedImg.writeImage(dir, newFilename);



        return Api.ok("result", dir +"/"+ newFilename);
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



        String winnerFramePath = result.getString(FRAME);
        File source = new File(winnerFramePath);

        String winnerFramePath1px = runDirPath+"/frames/px1/"+source.getName();
        File source1px = new File(winnerFramePath1px);

        String destination = runDirPath + "/winners/";
        File target = new File(destination + source.getName());

        String destination1px = runDirPath + "/winners/px1/";
        File target1px = new File(destination1px + source.getName());

        try {
            Files.copy(source.toPath(),    target.toPath(),    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source1px.toPath(), target1px.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e) {
            System.err.println("Unable to save winner img: "+ e.getMessage());
        }

        JSONArray ids = result.getJSONArray(IDS);
        boolean i1wins = result.getBoolean(I1_WINS);
        int winnerId = ids.getInt(i1wins ? 0 : 1);
        int loserId  = ids.getInt(i1wins ? 1 : 0);

        history.addClickWinner(winnerFramePath, winnerFramePath1px, winnerId, loserId);

        return Api.ok(MSG, "Thanks!");
    }


}
