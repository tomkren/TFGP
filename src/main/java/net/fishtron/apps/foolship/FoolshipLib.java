package net.fishtron.apps.foolship;

import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.eval.LibPackage;
import net.fishtron.gen.Gen;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Gamma;
import net.fishtron.trees.GammaSym;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import net.fishtron.utils.*;
import org.json.JSONObject;


import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by tom on 09.10.2017.
 */
class FoolshipLib {

    /*
    private static final String KEY_splitLeafAngle1 = "splitLeafAngle1";
    private static final String KEY_splitLeafAngle2 = "splitLeafAngle2";
    private static final String simpleDNA = "simpleDNA";
    */

    /*private static JSONObject mkAllParamsInfo() {
        return F.obj(
                simpleDNA, F.obj(
                        KEY_splitLeafAngle1, F.arr( 0, 23, 45, 60,  90, 120, 135, 150, 180, 200, 225, 250, 270, 300, 315, 340),
                        KEY_splitLeafAngle2, F.arr(10, 33, 55, 66, 100, 122, 145, 170, 190, 235, 255, 280, 305, 325)
                )
        );
    }*/

    /*static LibPackage mkLibGammaGoal_old(JSONObject jobConfigOpts) {

        Type TreeDNA = Types.parse("TreeDNA");

        Gamma gamma = Gamma.mk(
                simpleDNA, TreeDNA
        );

        EvalLib evalLib = EvalLib.mk(
                simpleDNA, new SimpleDNA()
        );

        return new LibPackage(TreeDNA, gamma, evalLib, mkAllParamsInfo());
    }*/


    static LibPackage mkLibPack(JSONObject jobConfigOpts) {

        Type TreeInput = Types.parse("TreeInput");

        Type List = Types.parse("List");
        Type TreeCmd = Types.parse("TreeCmd");
        Type CmdList = Types.mkTerm(List, TreeCmd);

        Type TreeProgram = Types.mk(TreeInput, CmdList);

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
                basicTreeProgram, Types.mk(UseSaveRatio, ConsumeGiveRatio, ConsumeGiveRatio, Angle, Angle, TreeProgram),
                useSaveRatio, UseSaveRatio,
                consumeGiveRatio, ConsumeGiveRatio,
                ratio, Ratio,
                angle, Angle
        );

        EvalLib evalLib = EvalLib.mk(
                basicTreeProgram, new EvalCode.ReflexiveJsonLeaf(),
                useSaveRatio,     new EvalCode.ReflexiveJsonParam(),
                consumeGiveRatio, new EvalCode.ReflexiveJsonParam(),
                ratio,            new EvalCode.ReflexiveJsonParam(),
                angle,            new EvalCode.ReflexiveJsonParam()
        );

        JSONObject allParamsInfo = F.obj(
                useSaveRatio,     F.obj(EvalCode.DEFAULT_PARAM_NAME, F.arr(0.8)),
                consumeGiveRatio, F.obj(EvalCode.DEFAULT_PARAM_NAME, F.arr(0.25)),
                ratio, F.obj(EvalCode.DEFAULT_PARAM_NAME, F.arr(0, 0.25, 0.5, 0.8, 1)),
                angle, F.obj(EvalCode.DEFAULT_PARAM_NAME, F.arr( 0, 23, 45, 60,  90, 120, 135, 150, 180, 200, 225, 250, 270, 300, 315, 340))
        );

        return new LibPackage(TreeProgram, gamma, evalLib, allParamsInfo);
    }

    /* Moved to net.fishtron.eval.EvalCode
       TODO ensure that it hasn't been broken by the move.
    private static class ReflexiveJsonLeaf implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun, int numArgs) {
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
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun, int numArgs) {
            return leaf.getParams().toJson().get(DEFAULT_PARAM_NAME);
        }
    }
    */




    /*
    private static class SimpleDNA implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun, int numArgs) {
            return leaf.getParams().toJson();
        }
    }
    */


    public static void main(String[] args) {
        Checker ch = new Checker();

        LibPackage lp = mkLibPack(null);
        F.log(lp, "\n");



        Gamma gamma_basic = lp.getGamma();
        Type goal_basic = lp.getGoal();
        EvalLib evalLib_basic = lp.getEvalLib();

        List<String> suggestedVarNames = Collections.singletonList("info");

        ABCD<Type, Gamma, Function<AppTree, AppTree>,List<GammaSym>> gammaRes = gamma_basic.mkGammaWithGoalTypeVars(goal_basic, suggestedVarNames);

        Type goal = gammaRes._1();
        Gamma gamma = gammaRes._2();
        Function<AppTree, AppTree> addLambdas = gammaRes._3();
        List<GammaSym> varList = gammaRes._4();

        EvalLib evalLib_vars = new EvalLib(F.map(varList, var -> AB.mk(var.getSym(), new EvalCode.ReflexiveJsonLeaf())));
        EvalLib evalLib = EvalLib.union(evalLib_basic, evalLib_vars);

        Type t = gamma.getSymbols().get(0)._2();
        F.log(t+" has "+ Types.countNumArgs(t)+" args.");

        Function<Object,Object> addJsonLambdas = acc -> {
            for (int i = varList.size()-1; i >= 0; i--) {
                acc = F.obj(varList.get(i).getSym(), acc);
            }
            return acc;
        };

        Gen gen = new Gen(gamma, ch);

        for (int i = 0; i<10; i++) {
            AppTree tree = gen.genOne(7, goal).randomizeParams(lp.getAllParamsInfo(), ch.getRandom());

            AppTree treeWithLams = addLambdas.apply(tree);

            F.log(treeWithLams);

            Object value = evalLib.eval(tree);
            value = addJsonLambdas.apply(value);
            F.log(value);
        }


        ch.results();
    }
}
