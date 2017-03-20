package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.apps.cellplaza.CellEvalCodes;
import cz.tomkren.fishtron.ugen.eval.*;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Leaf;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

/**Created by tom on 19.03.2017.*/

public class Libs {

    public static void main(String[] args) {
        Checker ch = new Checker();
        test_1(ch);
        ch.results();
    }

    private static void test_1(Checker ch) {
        int k_max = 100;
        int numStates = 2;
        String plazaDir = "mini_100";
        JSONArray pixelSizes = F.arr(5);
        EvalTester.testLib(ch, k_max, mkLib(numStates, plazaDir, pixelSizes), gamma, goal, true, x->x, mkAllParamsInfo(numStates,plazaDir));
    }



    static JSONObject mkAllParamsInfo(int numStates, String plazaDir) {

        JSONArray states = new JSONArray();
        for (int s = 0; s < numStates; s++) {states.put(s);}

        File coresDir = new File(CellPlaza.BASE_DIR+"/"+plazaDir+"/cores/");

        String[] coreFilenames = coresDir.list();
        JSONArray coresJson = F.jsonMap(coreFilenames == null ? Collections.emptyList() : Arrays.asList(coreFilenames));

        Log.it("coresJson: "+coresJson);

        return F.obj(
            "bitRule", F.obj("bits", F.obj(
                    "type", "list",
                    "length", Rule.numBits(numStates),
                    "values", states
            )),
            "seedImg",  F.obj("filename", coresJson),
            "numSteps", F.obj("n", F.arr(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42))
        );
    }


    private static final Type goal = Types.parse("Img");
    static final Type ruleType = Types.parse("Rule");

    static final Gamma gamma = Gamma.mk(
            "bitRule", "Rule",
            "seedImg", "Img",
            "numSteps", "N",
            "runRule", "Rule -> (Img -> (N -> Img))"
    );

    static EvalLib mkLib(int numStates, String plazaDir, JSONArray pixelSizes) {
        return EvalLib.mk(
            "bitRule",  new BitRule(numStates),
            "seedImg",  new CellEvalCodes.SeedImg(),
            "numSteps", new CellEvalCodes.NumSteps(),
            "runRule",  new RunRule(numStates, plazaDir, pixelSizes)
        );
    }


    private static class BitRule implements EvalCode {
        private final int numStates;
        BitRule(int numStates) {this.numStates = numStates;}

        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            JSONArray bits = leaf.getParams().toJson().getJSONArray("bits");
            return Rule.fromBits(bits, numStates);
        }
    }

    private static class RunRule implements F3 {
        private static int nextIndivId = 1;

        private final int numStates;
        private final String plazaDir;
        private final JSONArray pixelSizes;

        RunRule(int numStates, String plazaDir, JSONArray pixelSizes) {
            this.numStates = numStates;
            this.plazaDir = plazaDir;
            this.pixelSizes = pixelSizes;
        }

        @Override
        public Object apply3(Object ruleObj, Object imgNameObj, Object numStepsObj) {
            Rule rule = (Rule) ruleObj;
            String imgName = (String) imgNameObj;
            int numSteps = (int) numStepsObj;

            CellWorld w = new CellWorld(numStates, plazaDir, imgName, rule, false, pixelSizes);
            w.step(numSteps);
            String indivFilename = w.writeState(nextIndivId);
            nextIndivId ++;
            return indivFilename;
        }
    }



}
