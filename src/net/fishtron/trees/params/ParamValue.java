package net.fishtron.trees.params;

import net.fishtron.utils.AB;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**Created by tom on 19.03.2017.*/

public interface ParamValue {

    Object toJson(Object paramInfo);
    ParamValue randomlyShift(Object paramInfo, List<AB<Integer,Double>> shiftsWithProbabilities, Random rand);

    String TYPE_KEY = "type";

    static ParamValue selectParamValue(Object paramInfo, Random rand) {

        if (paramInfo instanceof JSONArray) {
            return new SingleEnumValue((JSONArray) paramInfo, rand);
        } else if (paramInfo instanceof JSONObject) {
            JSONObject pi = (JSONObject) paramInfo;
            if (pi.has(TYPE_KEY)) {
                String paramValueType = pi.getString(TYPE_KEY);

                switch (paramValueType) {
                    case ListEnumValue.TYPE_VALUE : return new ListEnumValue(pi, rand);
                    default : throw new Error("Unsupported paramValueType: "+paramValueType);
                }

            } else {
                throw new Error("JSONObject paramInfo must have key '"+TYPE_KEY+"'");
            }

        } else {
            throw new Error("Unsupported paramInfo format: "+paramInfo.toString());
        }

    }

}
