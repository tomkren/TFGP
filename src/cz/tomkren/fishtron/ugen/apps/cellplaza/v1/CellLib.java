package cz.tomkren.fishtron.ugen.apps.cellplaza.v1;

import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.apps.cellplaza.shared.CellEvalCodes;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.eval.*;
import cz.tomkren.fishtron.ugen.trees.Leaf;
import net.fishtron.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


/**Created by tom on 12.03.2017.*/

public class CellLib {

    public static void main(String[] args) {
        Checker ch = new Checker();

        //testBitVersion(ch);
        testBitVersion_2(ch);
        //testSimpleVarVersion(ch);

        ch.results();
    }






    private static final Type simpleVarGoal = Types.parse("S -> (N -> S)");
    private static final List<String> inputVarNames = Arrays.asList("s","nn");

    private static final EvalLib simpleVarLib = EvalLib.mk(
            "if",  new If(),
            "not", (IUtils.Bool1) x -> !x,
            "or",  (IUtils.Bool2) x -> y -> x || y,
            "and", (IUtils.Bool2) x -> y -> x && y,

            "eqs",  (Fun2) x -> y -> x == y,
            "eqn",  (Fun2) x -> y -> x == y,
            "<",    (Fun2) x -> y -> (int)x < (int)y

            //todo
    );

    private static final Gamma simpleVarGamma = Gamma.mk(
            "if",    "B -> (x -> (x -> x))",
            "not",   "B -> B",
            "or",    "B -> (B -> B)",
            "and",   "B -> (B -> B)",

            "eqs",    "S -> (S -> B)",
            "eqn",    "N -> (N -> B)",
            "<",      "N -> (N -> B)",

            "S","S",
            "N","N"
    );

    private static final JSONObject allParamsInfo_varVersion = F.obj(
            "N", F.obj("val", F.arr(0,1,2,3,4,5,6,7,8)),
            "S", F.obj("val", F.arr("alive","dead"))
    );

    private static void testSimpleVarVersion(Checker ch) {

        int k_max = 64;

        ABC<Type,Gamma,Function<AppTree,AppTree>> res = simpleVarGamma.mkGammaWithGoalTypeVars(simpleVarGoal, inputVarNames);
        Type newGoal   = res._1();
        Gamma newGamma = res._2();
        Function<AppTree,AppTree> addLams = res._3();

        Log.it(newGamma);

        EvalTester.testLib(ch, k_max, null, newGamma, newGoal, false, null, allParamsInfo_varVersion);
    }







    private static final EvalLib lib_sk = EvalLib.mk(
            "s", (IUtils.S) f -> g -> x -> f.apply(x).apply(g.apply(x)),
            "k", (Fun2) x -> y -> x,

            "if",  new If(),
            "not", (IUtils.Bool1) x -> !x,
            "or",  (IUtils.Bool2) x -> y -> x || y,  // todo pozor takhle neděla short circuiting
            "and", (IUtils.Bool2) x -> y -> x && y,  // todo dtto

            "==",  (IUtils.Eq)     eq  -> x -> y -> x == y,
            "<",   (IUtils.IntOrd) ord -> x -> y -> x < y, // todo misto dummy ord použít tem samotnej operator

            "alive", Cell.State.ALIVE, "dead",  Cell.State.DEAD,

            "0",0, "1",1, "2",2, "3",3, "4",4, "5",5, "6",6, "7",7, "8",8,

            "eqS",   null,
            "eqN",   null,
            "ordN",  null
    );

    private static final Gamma gamma_sk = Gamma.mk(
            "s", "(a -> (b -> c)) -> ((a -> b) -> (a -> c))",
            "k", "a -> (b -> a)",

            "if",    "B -> (x -> (x -> x))",
            "not",   "B -> B",
            "or",    "B -> (B -> B)",
            "and",   "B -> (B -> B)",

            "==",    "(Eq a) -> (a -> (a -> B))",
            "<",     "(Ord a) -> (a -> (a -> B))",

            "alive","S", "dead","S",

            "0","N", "1","N", "2","N", "3","N", "4","N", "5","N", "6","N", "7","N", "8","N",

            "eqS",   "Eq S",
            "eqN",   "Eq N",
            "ordN",  "Ord N"
    );

    private static final EvalLib simpleLib_sk = EvalLib.mk(
            "s", (IUtils.S) f -> g -> x -> f.apply(x).apply(g.apply(x)),
            "k", (Fun2) x -> y -> x,

            "if",  new If(),
            "not", (IUtils.Bool1) x -> !x,
            "or",  (IUtils.Bool2) x -> y -> x || y,
            "and", (IUtils.Bool2) x -> y -> x && y,

            "eqs",  (Fun2) x -> y -> x == y,
            "eqn",  (Fun2) x -> y -> x == y,
            "<",    (Fun2) x -> y -> (int)x < (int)y,

            "alive", Cell.State.ALIVE, "dead",  Cell.State.DEAD,

            "0",0, "1",1, "2",2, "3",3, "4",4, "5",5, "6",6, "7",7, "8",8
    );

    private static final Gamma simpleGamma_sk = Gamma.mk(
            "s", "(a -> (b -> c)) -> ((a -> b) -> (a -> c))",
            "k", "a -> (b -> a)",

            "if",    "B -> (x -> (x -> x))",
            "not",   "B -> B",
            "or",    "B -> (B -> B)",
            "and",   "B -> (B -> B)",

            "eqs",    "S -> (S -> B)",
            "eqn",    "N -> (N -> B)",
            "<",      "N -> (N -> B)",

            "alive","S", "dead","S",

            "0","N", "1","N", "2","N", "3","N", "4","N", "5","N", "6","N", "7","N", "8","N"
    );









    static final int bitIndivSize_0 = 40;

    static final Type bitGoal_0 = Types.parse("P Rule Img");

    static final Gamma bitGamma_0 = Gamma.mk(
            "0",       "B",
            "1",       "B",
            "[]",     "V a 0",
            ":",    "a -> ((V a n) -> (V a (S n)))",
            "rule",    "(V B "+unary(Rule.numBits)+") -> Rule",
            "seedImg", "Img",
            "pair",    "a -> (b -> (P a b))"
    );

    static final EvalLib bitLib_0 = EvalLib.mk(
            "0",    false,
            "1",    true,
            "[]",  Collections.emptyList(),
            ":", (Fun2) x -> xs -> cons_bool((boolean) x, (List<Boolean>)xs),
            "rule", (Fun) bits -> Rule.fromBits((List<Boolean>)bits),
            "seedImg", new CellEvalCodes.SeedImg(),
            "pair", (Fun2) a -> b -> AB.mk(a,b)
    );

    static final int bitIndivSize_1 = 40;

    static final Type bitGoal_1 = Types.parse("Img");

    static final Gamma bitGamma_1 = Gamma.mk(
            "0",       "B",
            "1",       "B",
            "[]",     "V a 0",
            ":",    "a -> ((V a n) -> (V a (S n)))",
            "mkRule",    "(V B "+unary(Rule.numBits)+") -> Rule",
            "seedImg", "Img",
            "numSteps", "N",
            "runRule", "Rule -> (Img -> (N -> Img))"
    );

    static final EvalLib bitLib_1 = EvalLib.mk(
            "0",    false,
            "1",    true,
            "[]",  Collections.emptyList(),
            ":", (Fun2) x -> xs -> cons_bool((boolean) x, (List<Boolean>)xs),
            "mkRule", (Fun) bits -> Rule.fromBits((List<Boolean>)bits),
            "seedImg", new  CellEvalCodes.SeedImg(),
            "numSteps", new  CellEvalCodes.NumSteps(),
            "runRule", (Fun3) rule -> img -> n -> MiniPlaza.runRule((Rule)rule, (String)img, (int)n)
    );

    static final JSONObject allParamsInfo_bitVersion_1 = F.obj(
            "seedImg",  F.obj("filename", F.arr("core01","core02","core03","core04","core05","core06","core07")),
            "numSteps", F.obj("n", F.arr(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42))
    );


    // -- bit V2 -----------------------------------------------------

    private static void testBitVersion_2(Checker ch) {
        int k_max = 100;
        EvalTester.testLib(ch, k_max, bitLib_2, bitGamma_2, bitGoal_2, true, CellLib::showRule_2, allParamsInfo_bitVersion_2);
    }

    static final JSONObject allParamsInfo_bitVersion_2 = F.obj(
            "bitRule", F.obj("bits", F.obj(
                    "type", "list",
                    "length", Rule.numBits,
                    "values", F.arr(0,1)
            )),
            "seedImg",  F.obj("filename", F.arr("core01","core02","core03","core04","core05","core06","core07")),
            "numSteps", F.obj("n", F.arr(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42))
    );


    static final Type bitGoal_2 = Types.parse("Img");

    static final Gamma bitGamma_2 = Gamma.mk(
            "bitRule", "Rule",
            "seedImg", "Img",
            "numSteps", "N",
            "runRule", "Rule -> (Img -> (N -> Img))"
    );

    static final EvalLib bitLib_2 = EvalLib.mk(
            "bitRule", new BitRule(),
            "seedImg", new CellEvalCodes.SeedImg(),
            "numSteps", new CellEvalCodes.NumSteps(),
            "runRule", (Fun3) rule -> img -> n -> MiniPlaza.runRule((Rule)rule, (String)img, (int)n)
    );

    private static Object showRule_2(Object ruleObj) {
        if (ruleObj instanceof String) {
            return ruleObj;
        } else {
            throw new Error("ruleObj should be a String");
        }
    }



    private static class BitRule implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            JSONArray bits = leaf.getParams().toJson().getJSONArray("bits");
            return Rule.fromBits(bits);
        }
    }


    private static List<Boolean> cons_bool(boolean x, List<Boolean> xs) {
        List<Boolean> ret = new ArrayList<>(1+xs.size());
        ret.add(x);
        ret.addAll(xs);
        return ret;
    }

    //private static JSONObject

    private static String unary(int n) {
        if (n == 0) {return "0";}
        return "(S "+ unary(n-1) +")";
    }





    private static void testBitVersion(Checker ch) {
        int k_max = 84;
        EvalTester.testLib(ch, k_max, bitLib_1, bitGamma_1, bitGoal_1, true, CellLib::showRule3, allParamsInfo_bitVersion_1);
    }

    private static String showRule3(Object indivObj) {
        return (String) indivObj;
    }

    private static String showRule2(Object indivObj) {
        AB<Object,Object> indivPair = (AB<Object,Object>) indivObj;
        Object ruleObj = indivPair._1();
        Rule rule = (Rule) ruleObj;

        return showCase2(rule, Cell.State.DEAD) + "| "+ showCase2(rule, Cell.State.ALIVE);
    }

    private static String showCase2(Rule rule, Cell.State s) {
        String ret = "";

        Cell dummyAlive = new Cell(Cell.State.ALIVE, 1.0);
        Cell dummyDead  = new Cell(Cell.State.DEAD, 1.0);

        for (int n = 0; n < Rule.neighbourCases; n++) {

            Cell cell = new Cell(s, 1.0);
            int numNeighbours = 8;
            Cell[] dummyNeighbors = new Cell[numNeighbours];

            for (int i = 0; i < n; i++) {
                dummyNeighbors[i] = dummyAlive;
            }

            for (int i = 0; i < numNeighbours - n; i++) {
                dummyNeighbors[n+i] = dummyDead;
            }

            cell.setNeighbours(dummyNeighbors);

            Cell.State nextState = rule.nextState(cell);

            ret += (nextState == Cell.State.ALIVE ? 1 : 0) + " ";
        }

        return ret;
    }


    private static String showRule(Object ruleObj) {
        Function<Cell.State, Function<Integer, Cell.State>> preRule = (Function<Cell.State, Function<Integer, Cell.State>>) ruleObj;
        BiFunction<Cell.State, Integer, Cell.State> rule = (s,n) -> preRule.apply(s).apply(n);
        return showCase(rule, Cell.State.DEAD) + "| "+ showCase(rule, Cell.State.ALIVE);
    }

    private static String showCase(BiFunction<Cell.State, Integer, Cell.State> rule, Cell.State s) {
        String ret = "";
        for (int n = 0; n < Rule.neighbourCases; n++) {
            ret += (rule.apply(s,n) == Cell.State.ALIVE ? 1 : 0) + " ";
        }
        return ret;
    }



}
