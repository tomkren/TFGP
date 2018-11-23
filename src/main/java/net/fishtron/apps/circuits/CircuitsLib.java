package net.fishtron.apps.circuits;

import net.fishtron.eval.EvalLib;
import net.fishtron.eval.IUtils;
import net.fishtron.eval.LibPackage;
import net.fishtron.trees.Gamma;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import net.fishtron.utils.F;
import org.json.JSONObject;

class CircuitsLib {

    static LibPackage mkLibPack(JSONObject jobConfigOpts) {

        Type B = Types.parse("B"); // bool

        Type B1 = Types.mk(B, B);
        Type B2 = Types.mk(B, B, B);

        Type B9 = Types.mk(B, B, B, B, B, B, B, B, B, B);

        String NOT = "not";
        String AND = "and";
        String OR  = "or";

        Gamma gamma = Gamma.mk(
                NOT,   B1,
                AND,   B2,
                OR,    B2
        );

        EvalLib evalLib = EvalLib.mk(
                NOT, (IUtils.Bool1) x -> !x,
                AND, (IUtils.Bool2) x -> y -> x && y,
                OR,  (IUtils.Bool2) x -> y -> x || y
        );

        JSONObject allParamsInfo = F.obj();

        return new LibPackage(B9, gamma, evalLib, allParamsInfo);
    }

}
