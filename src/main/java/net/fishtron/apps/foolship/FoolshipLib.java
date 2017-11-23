package net.fishtron.apps.foolship;

import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.eval.LibPackage;
import net.fishtron.gen.Gen;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Gamma;
import net.fishtron.trees.Leaf;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
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

    private static JSONObject mkAllParamsInfo() {
        return F.obj(
                simpleDNA, F.obj(
                        KEY_splitLeafAngle1, F.arr( 0, 23, 45, 60,  90, 120, 135, 150, 180, 200, 225, 250, 270, 300, 315, 340),
                        KEY_splitLeafAngle2, F.arr(10, 33, 55, 66, 100, 122, 145, 170, 190, 235, 255, 280, 305, 325)
                )
        );
    }

    static LibPackage mkLibGammaGoal_old(JSONObject jobConfigOpts) {

        Type TreeDNA = Types.parse("TreeDNA");

        Gamma gamma = Gamma.mk(
                simpleDNA, TreeDNA
        );

        EvalLib evalLib = EvalLib.mk(
                simpleDNA, new SimpleDNA()
        );

        return new LibPackage(TreeDNA, gamma, evalLib, mkAllParamsInfo());
    }


    static LibPackage mkLibPack(JSONObject jobConfigOpts) {

        Type TreeDNA = Types.parse("TreeDNA");

        Type Angle = Types.parse("Angle");
        Type Ratio = Types.parse("Ratio");

        Type UseSaveRatio = Types.parse("UseSaveRatio");
        Type ConsumeGiveRatio = Types.parse("ConsumeGiveRatio");


        String basicTreeProgram = "basicTreeProgram";
        String ratio = "ratio";
        String angle = "angle";

        String useSaveRatio = "useSaveRatio";
        String consumeGiveRatio = "consumeGiveRatio";


        Gamma gamma = Gamma.mk(
                basicTreeProgram, Types.mk(UseSaveRatio, ConsumeGiveRatio, ConsumeGiveRatio, Angle, Angle, TreeDNA),
                useSaveRatio, UseSaveRatio,
                consumeGiveRatio, ConsumeGiveRatio,
                ratio, Ratio,
                angle, Angle
        );

        EvalLib evalLib = EvalLib.mk(
                basicTreeProgram, new ReflexiveJsonLeaf(),
                useSaveRatio,     new ReflexiveJsonParam(),
                consumeGiveRatio, new ReflexiveJsonParam(),
                ratio,            new ReflexiveJsonParam(),
                angle,            new ReflexiveJsonParam()
        );

        JSONObject allParamsInfo = F.obj(
                useSaveRatio,     F.obj(DEFAULT_PARAM_NAME, F.arr(0.8)),
                consumeGiveRatio, F.obj(DEFAULT_PARAM_NAME, F.arr(0.25)),
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

        LibPackage lp = mkLibPack(null);
        F.log(lp, "\n");

        Type t = lp.getGamma().getSymbols().get(0)._2();
        F.log(t+" has "+ Types.countNumArgs(t)+" args.");


        Gen gen = new Gen(lp.getGamma(), ch);

        for (int i = 0; i<10; i++) {
            AppTree tree = gen.genOne(6, lp.getGoal()).randomizeParams(lp.getAllParamsInfo(), ch.getRandom());
            F.log(tree);

            Object value = lp.getEvalLib().eval(tree);
            F.log(value);
        }


        ch.results();
    }
}
