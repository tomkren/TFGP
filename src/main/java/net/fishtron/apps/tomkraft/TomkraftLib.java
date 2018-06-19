package net.fishtron.apps.tomkraft;

import net.fishtron.eval.LibPackage;
import net.fishtron.types.Type;
import net.fishtron.types.TypeSym;
import net.fishtron.types.Types;
import net.fishtron.utils.TODO;
import org.json.JSONObject;


public class TomkraftLib {

    static LibPackage mkLibPack(JSONObject jobConfigOpts) {

        Type R = Types.parse("R"); // float
        Type B = Types.parse("B"); // bool

        Type R2 = Types.mk(R, R, R);
        Type R1 = Types.mk(R, R);

        Type Compare_R = Types.mk(R,R,B);
        Type If_R = Types.mk(B,R,R,R);

        Type IfakBody_R = Types.mk(B,R);
        Type Ifak_R = Types.mk(B,IfakBody_R,IfakBody_R,R);



        /*

         From tomcraft code:
          Q: Shoudl we use just ReflexiveJsons, or implement it here also?
          A: Quicker way is reflexive, "also here" is can be done later.
        */



        /*
          ELib eLib = ELib.Mk(
            "+",      R2 (Func<object, object>)(x => (Func<object, object>)(y => (float)x + (float)y)),
            "-",      R2 (Func<object, object>)(x => (Func<object, object>)(y => (float)x - (float)y)),
            "*",      R2 (Func<object, object>)(x => (Func<object, object>)(y => (float)x * (float)y)),
            "/",      R2 (Func<object, object>)(x => (Func<object, object>)(y => (float)x / (float)y)),
            "sin",    R1 (Func<object, object>)(x => Mathf.Sin((float)x)),
            "cos",    R1 (Func<object, object>)(x => Mathf.Cos((float)x)),
            "perlin", R2 (Func<object, object>)(x => (Func<object, object>)(y => Mathf.PerlinNoise((float)x, (float)y))),

            // Func<bool, Func<bool,float>, Func<bool, float>, float> ifak = ((p, a, b) => p ? a(p) : b(p));
            "<=",   Compare_R (Func<object, object>)(x => (Func<object, object>)(y => (float)x <= (float)y)),
            "ifak", Ifak_R  (Func<object, object>)(p => (Func<object, object>)(a => (Func<object, object>)(b => (bool)p ? ((Func<object, object>)a)(p) : ((Func<object, object>)b)(p)))),
            "s_if", If_R  (Func<object, object>)(p => (Func<object, object>)(a => (Func<object, object>)(b => (bool)p ? a : b)))
        );

        */




        throw new TODO("mkLibPack not yet implemented!");
    }

}
