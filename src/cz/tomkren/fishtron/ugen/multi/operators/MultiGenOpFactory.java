package cz.tomkren.fishtron.ugen.multi.operators;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.operators.CopyOp;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**Created by tom on 09.03.2017.*/

public class MultiGenOpFactory {

    public static Distribution<Operator<AppTreeMI>> mkOperators(JSONArray opsOpts, Random rand, Gen gen, JSONObject allParamsInfo) {
        List<Operator<AppTreeMI>> operators;
        operators = F.map(opsOpts, opOpts -> mkOperator((JSONObject)opOpts,rand,gen,allParamsInfo));
        return new Distribution<>(operators);
    }

    private static Operator<AppTreeMI> mkOperator(JSONObject opOpts, Random rand, Gen gen,JSONObject allParamsInfo) {
        String opName = opOpts.getString("name");
        switch (opName) {

            case "basicTypedXover"           : return new XoverAppTreeMI(opOpts, rand);
            case "sameSizeSubtreeMutation"   : return new SssMutation(opOpts, allParamsInfo, gen);
            case "oneParamMutation"          : return new MutationParam(opOpts, rand);
            case "copyOp"                    : return CopyOp.mk(opOpts);

            default : throw new Error("Unsupported operator.name: "+opName);
        }
    }

}
