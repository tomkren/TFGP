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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


public class TomkraftLib {

    static LibPackage mkReflexivePack(Type goal, Object... args) {

        if (args.length % 3 != 0) {throw new Error("args.length % 4 != 0");}

        int numSymbols = args.length / 3;

        List<GammaSym> gammaSymList = new ArrayList<>(numSymbols);
        List<AB<String,Object>> defs = new ArrayList<>(numSymbols);

        JSONObject allParamsInfo = new JSONObject();

        for (int i = 0; i < numSymbols; i++) {

            int i_name = 3*i;
            int i_type = i_name + 1;
            int i_para = i_name + 2;

            if (!(args[i_name] instanceof String)) {
                throw new Error("Arg #"+i_name+" '"+args[i_name]+"' is not a String.");
            }
            if (!(args[i_type] instanceof Type) && !(args[i_type] instanceof String)) {
                throw new Error("Arg #"+i_type+ " '" +args[i_type]+"' is not a Type.");
            }
            if (args[i_para] != null && !(args[i_para] instanceof JSONArray)) {
                throw new Error("Arg #"+i_para+" '"+args[i_para]+"' is not a param description.");
            }

            String name = (String) args[i_name];
            Type   type = (args[i_type] instanceof Type) ? (Type) args[i_type] : Types.parse((String)args[i_type]);

            boolean isParam = args[i_para] != null;

            gammaSymList.add(new GammaSym(name, type, false));
            defs.add(AB.mk(name, isParam ? new EvalCode.ReflexiveJsonParam() : new EvalCode.ReflexiveJsonLeaf()));

            if (isParam) {
                JSONArray paramValues = (JSONArray) args[i_para];
                allParamsInfo.put(name, F.obj(EvalCode.DEFAULT_PARAM_NAME, paramValues));
            }
        }

        Gamma gamma = new Gamma(gammaSymList);
        EvalLib evalLib = new EvalLib(defs);

        return new LibPackage(goal, gamma, evalLib, allParamsInfo);
    }

    static LibPackage mkLibPack(JSONObject jobConfigOpts) {



        Type R = Types.parse("R"); // float
        Type B = Types.parse("B"); // bool

        Type R2 = Types.mk(R, R, R);
        Type R1 = Types.mk(R, R);

        Type Compare_R = Types.mk(R,R,B);
        Type If_R = Types.mk(B,R,R,R);

        Type IfakBody_R = Types.mk(B,R);
        Type Ifak_R = Types.mk(B,IfakBody_R,IfakBody_R,R);


        double golden_ratio = 0.5 * (1.0 + Math.sqrt(5));
        double golden_ratio_inverse = golden_ratio - 1;
        JSONArray c_params = F.arr(-1, 0, 0.25, 0.333, 0.5, golden_ratio_inverse, 1, Math.sqrt(2), 0.5*Math.PI,
                golden_ratio, 2, 3, Math.PI, 4, 8, 16, 32);

        double PI = Math.PI;

        LibPackage libPackage_01 = mkReflexivePack(R2,
                "c",        R, c_params,
                "perlin",   R2, null,
                "+",        R2, null,
                "-",        R2, null,
                "*",        R2, null,
                "/",        R2, null,
                "sin",      R1, null,
                "cos",      R1, null,
                "<=",       Compare_R, null,
                //"ifak",     Ifak_R, null,
                "s_if",     If_R, null
        );

        Type Terrain = Types.parse("T");
        Type TerrainBinOp = Types.mk(Terrain,Terrain,Terrain);
        Type Move_1 = Types.parse("M");
        Type PerlinDY = Types.parse("Dy");
        Type Scale = Types.parse("S");
        Type Height = Types.parse("H");
        Type Rot_1 = Types.parse("R");
        Type Sigma_1 = Types.parse("Si");



        Type PerlinType = Types.mk(Height, PerlinDY, Move_1, Move_1, Scale, Terrain);
        Type GaussType  = Types.mk(Height, Move_1, Move_1, Sigma_1, Sigma_1, Rot_1, Terrain);

        LibPackage libPackage_02 = mkReflexivePack(Terrain,
                "+", TerrainBinOp, null,
                "gauss", GaussType, null,
                "perlin", PerlinType, null,
                "m", Move_1, F.arr(-64, -32, -16, -8, -4, -2, -1, 0, 1, 2, 4, 8, 16, 32, 64),
                "h", Height, F.arr(1,2,4,8,16,32,64),
                "dy", PerlinDY, F.arr(-1,-0.75,-0.5,-0.25,0,0.25,0.5,0.75,1),
                "s", Scale, F.arr(1,2,4,8,16,32,64),
                "r", Rot_1, F.arr(0, 0.25*PI, 0.5*PI, 0.75*PI, PI, 1.25*PI, 1.5*PI, 1.75*PI),
                "si",Sigma_1, F.arr(2, 4, 8, 16, 32, 64)
        );

        //return new LibPackage(R2, gamma, evalLib, allParamsInfo);
        return libPackage_02;
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


        for (int tree_size = 1; tree_size < 30; tree_size++) {

            F.log("\n-- tree_size =", tree_size, "---------------------------------------------------------------\n");
            if (gen.getNum(tree_size, goal).doubleValue() > 0) {
                for (int i = 0; i<10; i++) {


                    AppTree tree = gen.genOne(tree_size, goal).randomizeParams(lp.getAllParamsInfo(), ch.getRandom());

                    F.log(addLambdas.apply(tree));

                    Object value = evalLib.eval(tree);
                    value = addJsonLambdas.apply(value);
                    F.log(value);
                }
            } else {
                F.log("No tree for size ", tree_size+".");
            }

        }



        ch.results();
    }
}
