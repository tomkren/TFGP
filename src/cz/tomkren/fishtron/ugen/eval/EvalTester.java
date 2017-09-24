package cz.tomkren.fishtron.ugen.eval;

import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import net.fishtron.utils.Checker;
import net.fishtron.utils.Log;
import org.json.JSONObject;

import java.util.function.Function;

/** Created by user on 14. 2. 2017. */

public class EvalTester {

    public static void main(String[] args) {
        Checker ch = new Checker();

        EvalLib lib = EvalLib.mk(
                "0", 0.0,
                "s", (Fun)  x -> (double)x + 1,
                "+", (Fun2) x -> (y -> (double)x + (double)y)
        );

        Gamma gamma = Gamma.mk(
                "0", "Num",
                "s", "Num -> Num",
                "+", "Num -> (Num -> Num)"
        );

        Gamma gamma2 = Gamma.mk(
                "0",      "Zero",
                "s",      "n -> (S n)",
                "++",     "(Plus x y z) -> (x -> (y -> z))",
                "plus_0", "Plus x Zero x",
                "plus_n", "(Plus x y z) -> (Plus x (S y) (S z))"
        );


        testLib(ch, 64, lib, gamma, "Num", true);
        testLib(ch, 4, lib, gamma2, "(S (S Zero))", true); // todo pro typ "n" to spadne na asserci v genOne že rawType je stejnej jako toho stromu (což je dobře ale vyřešit)


        ch.results();
    }

    public static void testLib(Checker ch, int k_max, EvalLib lib, Gamma gamma, Type goal) {
        testLib(ch, k_max, lib, gamma, goal, true, x->x, null);
    }

    private static void testLib(Checker ch, int k_max, EvalLib lib, Gamma gamma, String goalStr, boolean testEvaluation) {
        testLib(ch, k_max, lib, gamma, Types.parse(goalStr), testEvaluation, x->x, null);
    }

    public static void testLib(Checker ch, int k_max, EvalLib lib, Gamma gamma, Type goal, boolean testEvaluation, Function<Object,Object> transformResult) {
        testLib(ch, k_max, lib, gamma, goal, testEvaluation, transformResult, null);
    }

    public static void testLib(Checker ch, int k_max, EvalLib lib, Gamma gamma, Type goal, boolean testEvaluation, Function<Object,Object> transformResult, JSONObject allParamsInfo) {

        Log.it("Goal = "+goal);

        Gen gen = new Gen(gamma, ch);
        boolean allTreesAreWellTyped = true;

        for (int k = 1; k <= k_max; k++) {
            AppTree tree = gen.genOne(k, goal);

            if (tree != null) {

                if (!tree.isStrictlyWellTyped(gamma)) {
                    ch.fail("Tree is not well-typed: "+tree);
                    allTreesAreWellTyped = false;
                }

                if (allParamsInfo != null) {
                    tree = tree.randomizeParams(allParamsInfo, ch.getRandom());
                }

                String resultStr = "";
                if (testEvaluation) {
                    Object result = lib.eval(tree);
                    Object transformedResult = transformResult.apply(result);
                    resultStr = transformedResult + " \t = \t ";
                }

                Log.it("("+k+") \t "+ resultStr + tree);

            } else {
                Log.it("("+k+") \t N/A");
            }


        }

        Log.it();
        ch.is(allTreesAreWellTyped, "All trees are well-typed.");
        Log.it("----------------------------------------------");
        Log.it();
    }

}
