package cz.tomkren.fishtron.ugen.params;

import cz.tomkren.utils.AB;
import org.json.JSONArray;

import java.util.List;
import java.util.Random;

/**Created by tom on 19.03.2017.*/

public interface ParamValue {

    Object toJson(Object paramInfo);
    ParamValue randomlyShift(Object paramInfo, List<AB<Integer,Double>> shiftsWithProbabilities, Random rand);


    static ParamValue selectParamValue(Object paramInfo, Random rand) {

        if (paramInfo instanceof JSONArray) {
            return new SingleEnumValue((JSONArray) paramInfo, rand);
        } else {
            throw new Error("Unsupported paramInfo format: "+paramInfo.toString());
        }

    }

}
