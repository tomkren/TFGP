package cz.tomkren.fishtron.ugen.eva;

import net.fishtron.utils.Distribution;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.operators.CopyOp;
import net.fishtron.gen.Gen;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/** Created by tom on 17. 2. 2017.*/

public class GenOpFactory {

    public static Distribution<Operator<AppTreeIndiv>> mkOperators(JSONArray opsOpts, Random rand, Gen gen, JSONObject allParamsInfo) {
        List<Operator<AppTreeIndiv>> operators;
        operators = F.map(opsOpts, opOpts -> mkOperator((JSONObject)opOpts,rand,gen,allParamsInfo));
        return new Distribution<>(operators);
    }

    private static Operator<AppTreeIndiv> mkOperator(JSONObject opOpts, Random rand, Gen gen,JSONObject allParamsInfo) {
        String opName = opOpts.getString("name");
        switch (opName) {

            case "basicTypedXover"           : return new BasicAppTreeXover(opOpts, rand);
            case "sameSizeSubtreeMutation"   : return new SSSMutation(opOpts, allParamsInfo, gen);
            case "oneParamMutation"          : return new ParamMutation(opOpts, rand);
            case "copyOp"                    : return CopyOp.mk(opOpts);

            default : throw new Error("Unsupported operator.name: "+opName);
        }
    }

}
