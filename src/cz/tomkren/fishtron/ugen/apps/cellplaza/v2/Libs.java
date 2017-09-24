package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.apps.cellplaza.shared.CellEvalCodes;
import cz.tomkren.fishtron.ugen.eval.*;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Leaf;
import net.fishtron.utils.AB;
import cz.tomkren.utils.Checker;
import net.fishtron.utils.F;
//import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

        CellOpts opts = new CellOpts(2, "mini_100", F.arr(5), false);

        EvalTester.testLib(ch, k_max, mkLib(opts), gamma, goal_img, true, x->x, mkAllParamsInfo(opts, ch));
    }



    public static JSONObject mkAllParamsInfo(CellOpts opts, Checker ch) {

        int numStates = opts.getNumStates();
        String plazaDir = opts.getPlazaDir();

        JSONArray states = new JSONArray();
        for (int s = 0; s < numStates; s++) {states.put(s);}

        File coresDir = new File(CellPlaza.BASE_DIR+"/"+plazaDir+"/cores/");

        String[] coreFilenames = coresDir.list();
        JSONArray coresJson = F.jsonMap(coreFilenames == null ? Collections.emptyList() : Arrays.asList(coreFilenames));

        ch.it("coresJson: "+coresJson);

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


    private static final Type goal_img = Types.parse("Img");
    public static final Type goal_pair = Types.parse("P Img Rule");

    static final Type ruleType = Types.parse("Rule");

    public static final Gamma gamma = Gamma.mk(
            "bitRule", "Rule",
            "seedImg", "Img",
            "numSteps", "N",
            "runRule", "Rule -> (Img -> (N -> Img))",
            "pair",    "a -> (b -> (P a b))"
    );

    public static EvalLib mkLib(CellOpts opts) {
        return EvalLib.mk(
            "bitRule",  new BitRule(opts.getNumStates()),
            "seedImg",  new CellEvalCodes.SeedImg(),
            "numSteps", new CellEvalCodes.NumSteps(),
            "runRule",  new RunRule(opts),
            "pair", (Fun2) a -> b -> AB.mk(a,b)
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

        private final CellOpts opts;

        RunRule(CellOpts opts) {
            this.opts = opts;
        }

        @Override
        public Object apply3(Object ruleObj, Object imgNameObj, Object numStepsObj) {
            Rule rule = (Rule) ruleObj;
            String imgName = (String) imgNameObj;
            int numSteps = (int) numStepsObj;

            CellWorld w = new CellWorld(opts, imgName, rule, false);
            w.step(numSteps);
            String indivFilename = w.writeState(nextIndivId);
            nextIndivId ++;
            return indivFilename;
        }
    }

    public static List<String> genPhenotype(CellOpts opts, String seedFilename, Rule rule, int numFrames, String runDirName, int indivId, Checker ch) {

        List<String> frameFilenames = new ArrayList<>(numFrames);

        CellWorld w = new CellWorld(opts, seedFilename, rule, false);

        String frameFilename = w.writeState_eva(runDirName, indivId, 0);
        frameFilenames.add(frameFilename);
        ch.log(" >>> "+frameFilename);

        for (int s = 0; s < numFrames-1; s++) {
            w.step();
            frameFilename = w.writeState_eva(runDirName, indivId, s+1);
            frameFilenames.add(frameFilename);
            ch.log(" >>> "+ frameFilename);
        }

        return frameFilenames;
    }


}
