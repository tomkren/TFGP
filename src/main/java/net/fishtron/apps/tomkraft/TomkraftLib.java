package net.fishtron.apps.tomkraft;


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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


public class TomkraftLib {

    static LibPackage mkLibPack(JSONObject jobConfigOpts) {

        Type R = Types.parse("R"); // float
        Type B = Types.parse("B"); // bool

        Type R2 = Types.mk(R, R, R);
        Type R1 = Types.mk(R, R);

        Type Compare_R = Types.mk(R,R,B);
        Type If_R = Types.mk(B,R,R,R);

        //Type IfakBody_R = Types.mk(B,R);
        //Type Ifak_R = Types.mk(B,IfakBody_R,IfakBody_R,R);

        String SYM_perlin = "perlin";
        String SYM_c   =    "c";
        String SYM_add =    "+";
        String SYM_sub =    "-";
        String SYM_mul =    "*";
        String SYM_div =    "/";
        String SYM_sin =    "sin";
        String SYM_cos =    "cos";
        String SYM_leq =    "<=";
        String SYM_ifak =   "ifak";
        String SYM_s_if =   "s_if";

        Gamma gamma = Gamma.mk(
                SYM_c       , R,
                SYM_perlin  , R2,
                SYM_add     , R2,
                SYM_sub     , R2,
                SYM_mul     , R2,
                SYM_div     , R2,
                SYM_sin     , R1,
                SYM_cos     , R1,
                SYM_leq     , Compare_R,
                //SYM_ifak    , Ifak_R,
                SYM_s_if    , If_R
        );


        /*
          Q: Should we use just ReflexiveJsons, or implement it here also?
          A: Quicker way is reflexive, "also here" is can be done later.
        */

        EvalLib evalLib = EvalLib.mk(
                SYM_c       , new EvalCode.ReflexiveJsonParam(),
                SYM_add     , new EvalCode.ReflexiveJsonLeaf(),
                SYM_sub     , new EvalCode.ReflexiveJsonLeaf(),
                SYM_mul     , new EvalCode.ReflexiveJsonLeaf(),
                SYM_div     , new EvalCode.ReflexiveJsonLeaf(),
                SYM_sin     , new EvalCode.ReflexiveJsonLeaf(),
                SYM_cos     , new EvalCode.ReflexiveJsonLeaf(),
                SYM_perlin  , new EvalCode.ReflexiveJsonLeaf(),
                SYM_leq     , new EvalCode.ReflexiveJsonLeaf(),
                SYM_ifak    , new EvalCode.ReflexiveJsonLeaf(),
                SYM_s_if    , new EvalCode.ReflexiveJsonLeaf()
        );

        double golden_ratio = 0.5 * (1.0 + Math.sqrt(5));
        double golden_ratio_inverse = golden_ratio - 1;

        JSONObject allParamsInfo = F.obj(
                SYM_c, F.obj(EvalCode.DEFAULT_PARAM_NAME, F.arr(
                        -1, 0, 0.25, 0.333, 0.5, golden_ratio_inverse,
                        1, Math.sqrt(2), 0.5*Math.PI, golden_ratio, 2, 3,
                        Math.PI, 4, 8, 16, 32
                ))
        );

        return new LibPackage(R2, gamma, evalLib, allParamsInfo);
    }

    public static void main(String[] args) {
        Checker ch = new Checker();

        LibPackage lp = mkLibPack(null);
        F.log(lp, "\n");

        Gamma gamma_basic = lp.getGamma();
        Type goal_basic = lp.getGoal();
        EvalLib evalLib_basic = lp.getEvalLib();

        List<String> suggestedVarNames = Arrays.asList("x", "z");

        ABCD<Type, Gamma, Function<AppTree, AppTree>,List<GammaSym>> gammaRes = gamma_basic.mkGammaWithGoalTypeVars(goal_basic, suggestedVarNames);

        Type goal = gammaRes._1();
        Gamma gamma = gammaRes._2();
        Function<AppTree, AppTree> addLambdas = gammaRes._3();
        List<GammaSym> varList = gammaRes._4();

        F.log("raw_goal = ", goal);

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

            F.log(addLambdas.apply(tree));

            Object value = evalLib.eval(tree);
            value = addJsonLambdas.apply(value);
            F.log(value);
        }



        ch.results();
    }
}
