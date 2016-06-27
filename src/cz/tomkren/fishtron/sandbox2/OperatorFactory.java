package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.operators.BasicTypedXover;
import cz.tomkren.fishtron.operators.CopyOp;
import cz.tomkren.fishtron.operators.OneParamMutation;
import cz.tomkren.fishtron.operators.SameSizeSubtreeMutation;
import cz.tomkren.fishtron.reusegen.QuerySolver;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/** Created by tom on 27. 6. 2016.*/

public class OperatorFactory {

    public static Distribution<Operator<PolyTree>> mkOperators(JSONArray opsOpts, Random rand, QuerySolver qs) {
        return new Distribution<>(F.map(opsOpts, opOpts -> mkOperator((JSONObject)opOpts,rand,qs)));
    }

    public static Operator<PolyTree> mkOperator(JSONObject opOpts, Random rand, QuerySolver qs) {
        String opName = opOpts.getString("name");
        switch (opName) {
            case "basicTypedXover" : return new BasicTypedXover(rand, opOpts);
            case "sameSizeSubtreeMutation" : return new SameSizeSubtreeMutation(qs, opOpts);
            case "oneParamMutation" : return new OneParamMutation(rand, opOpts);
            case "copyOp" : return CopyOp.mk(opOpts);
            default: throw new Error("Unsupported operator.name: "+opName);
        }
    }

}
