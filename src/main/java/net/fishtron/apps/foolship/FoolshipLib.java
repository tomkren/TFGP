package net.fishtron.apps.foolship;

import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Gamma;
import net.fishtron.trees.Leaf;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import net.fishtron.utils.ABC;
import net.fishtron.utils.F;
import net.fishtron.utils.TODO;
import org.json.JSONArray;
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


    private static class SimpleDNA implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            return leaf.getParams().toJson();
        }
    }

}
