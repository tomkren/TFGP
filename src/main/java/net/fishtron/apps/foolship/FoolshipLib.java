package net.fishtron.apps.foolship;

import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.gen.Gen;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Gamma;
import net.fishtron.trees.Leaf;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import net.fishtron.utils.ABC;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.function.Function;

/**
 * Created by tom on 09.10.2017.
 */
class FoolshipLib {

    private static final String KEY_splitLeafAngle1 = "splitLeafAngle1";
    private static final String KEY_splitLeafAngle2 = "splitLeafAngle2";


    private static final String simpleDNA = "simpleDNA";

    static JSONObject mkAllParamsInfo() {
        return F.obj(
                simpleDNA, F.obj(
                        KEY_splitLeafAngle1, F.arr( 0, 23, 45, 60,  90, 120, 135, 150, 180, 200, 225, 250, 270, 300, 315, 340),
                        KEY_splitLeafAngle2, F.arr(10, 33, 55, 66, 100, 122, 145, 170, 190, 235, 255, 280, 305, 325)
                )
        );
    }

    static ABC<EvalLib,Gamma,Type> mkLibGammaGoal(JSONObject config) {

        Type TreeDNA = Types.parse("TreeDNA");

        Gamma gamma = Gamma.mk(
                simpleDNA, TreeDNA
        );

        EvalLib evalLib = EvalLib.mk(
                simpleDNA, new SimpleDNA()
        );

        return ABC.mk(evalLib, gamma, TreeDNA);
    }

    public static class LibPackage {
        public final Type goal;
        public final Gamma gamma;
        public final EvalLib evalLib;
        public final JSONObject allParamsInfo;

        public LibPackage(Type goal, Gamma gamma, EvalLib evalLib, JSONObject allParamsInfo) {
            this.goal = goal;
            this.gamma = gamma;
            this.evalLib = evalLib;
            this.allParamsInfo = allParamsInfo;
        }

        @Override
        public String toString() {
            return "LibPackage{" +
                    "goal=" + goal +
                    ", gamma=" + gamma +
                    ", evalLib=" + evalLib +
                    ", allParamsInfo=" + allParamsInfo +
                    '}';
        }
    }


    static LibPackage mkLibGammaGoal_new() {

        Type TreeDNA = Types.parse("TreeDNA");

        Type Angle = Types.parse("Angle");
        Type Ratio = Types.parse("Ratio");

        String basicTreeProgram = "basicTreeProgram";
        String ratio = "ratio";
        String angle = "angle";


        Gamma gamma = Gamma.mk(
                basicTreeProgram, Types.mk(Ratio, Ratio, Ratio, Angle, Angle, TreeDNA),
                ratio, Ratio,
                angle, Angle
        );

        EvalLib evalLib = EvalLib.mk(
                basicTreeProgram, new ReflexiveJsonLeaf(),
                ratio, new ReflexiveJsonParam(),
                angle, new ReflexiveJsonParam()
        );

        JSONObject allParamsInfo = F.obj(
                ratio, F.obj(DEFAULT_PARAM_NAME, F.arr(0, 0.25, 0.5, 0.8, 1)),
                angle, F.obj(DEFAULT_PARAM_NAME, F.arr( 0, 23, 45, 60,  90, 120, 135, 150, 180, 200, 225, 250, 270, 300, 315, 340))
        );

        return new LibPackage(TreeDNA, gamma, evalLib, allParamsInfo);
    }


    private static class ReflexiveJsonLeaf implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            int numArgs = Types.countNumArgs( leaf.getType() );
            return mkValue(numArgs, leaf.getSym());
        }
        private static Object mkValue(int numArgs, Object acc) {
            if (numArgs == 0) {
                return acc;
            } else {
                return (Function<Object,Object>) arg -> mkValue(numArgs-1, F.arr(acc, arg));
            }
        }
    }

    private static String DEFAULT_PARAM_NAME = "_";

    private static class ReflexiveJsonParam implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            return leaf.getParams().toJson().get(DEFAULT_PARAM_NAME);
        }
    }

    private static class SimpleDNA implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            return leaf.getParams().toJson();
        }
    }


    public static void main(String[] args) {
        Checker ch = new Checker();

        LibPackage lp = mkLibGammaGoal_new();
        F.log(lp, "\n");

        Type t = lp.gamma.getSymbols().get(0)._2();
        F.log(t+" has "+ Types.countNumArgs(t)+" args.");


        Gen gen = new Gen(lp.gamma, ch);

        for (int i = 0; i<10; i++) {
            AppTree tree = gen.genOne(6, lp.goal).randomizeParams(lp.allParamsInfo, ch.getRandom());
            F.log(tree);
            Object value = lp.evalLib.eval(tree);

            F.log(value);

        }


        ch.results();
    }
}
