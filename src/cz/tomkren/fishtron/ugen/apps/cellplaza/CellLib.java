package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.eval.*;
import cz.tomkren.utils.Checker;

import java.util.function.BiFunction;
import java.util.function.Function;


/**Created by tom on 12.03.2017.*/

public class CellLib {

    private  static final Type goal = Types.parse("S -> (N -> S)");


    private static final EvalLib lib = EvalLib.mk(
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

    private static final Gamma gamma = Gamma.mk(
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

    private static final EvalLib simpleLib = EvalLib.mk(
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

    private static final Gamma simpleGamma = Gamma.mk(
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

    public static void main(String[] args) {
        Checker ch = new Checker();

        int k_max = 20;

        EvalTester.testLib(ch, k_max, simpleLib, simpleGamma, goal, true, CellLib::showRule);

        ch.results();
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
