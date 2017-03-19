package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.apps.cellplaza.CellEvalCodes;
import cz.tomkren.fishtron.ugen.eval.EvalCode;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.eval.EvalTester;
import cz.tomkren.fishtron.ugen.eval.Fun3;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Leaf;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

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
        EvalTester.testLib(ch, k_max, lib, gamma, goal, true, x->x, allParamsInfo);
    }

    static final JSONObject allParamsInfo = F.obj(
            "bitRule", F.obj("bits", F.obj(
                    "type", "list",
                    "length", Rule.numBits,
                    "values", F.arr(0,1)
            )),
            "seedImg",  F.obj("filename", F.arr("core01","core02","core03","core04","core05","core06","core07")),
            "numSteps", F.obj("n", F.arr(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42))
    );


    static final Type goal = Types.parse("Img");
    static final Type ruleType = Types.parse("Rule");

    static final Gamma gamma = Gamma.mk(
            "bitRule", "Rule",
            "seedImg", "Img",
            "numSteps", "N",
            "runRule", "Rule -> (Img -> (N -> Img))"
    );

    static final EvalLib lib = EvalLib.mk(
            "bitRule", new BitRule(),
            "seedImg", new CellEvalCodes.SeedImg(),
            "numSteps", new CellEvalCodes.NumSteps(),
            "runRule", (Fun3) rule -> img -> n -> runRule((Rule)rule, (String)img, (int)n)
    );


    private static class BitRule implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            JSONArray bits = leaf.getParams().toJson().getJSONArray("bits");
            return Rule.fromBits(bits);
        }
    }



    private static int nextIndivId = 1;

    private static String runRule(Rule rule, String imgName, int numSteps) {
        CellWorld w = new CellWorld("mini_100", imgName, rule, false);
        w.step(numSteps);
        String indivFilename = w.writeState(nextIndivId);
        nextIndivId ++;
        return indivFilename;
    }





}
