package net.fishtron.trees.params;

import net.fishtron.utils.Distribution;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/** Created by user on 16. 2. 2017.*/



public class Params {

    private final JSONObject paramInfos;
    private final HashMap<String,ParamValue> paramValues;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Params params = (Params) o;
        return paramValues.equals(params.paramValues);
    }

    @Override
    public int hashCode() {
        return paramValues.hashCode();
    }

    public Params(JSONObject paramInfos, Random rand) {
        this.paramInfos = paramInfos;
        paramValues = new HashMap<>();
        for (Object key : paramInfos.keySet()) {
            String paramName = (String) key;
            Object paramInfo = paramInfos.get(paramName);
            ParamValue paramValue = ParamValue.selectParamValue(paramInfo, rand);
            paramValues.put(paramName, paramValue);
        }
    }

    private Params(JSONObject paramInfos, HashMap<String, ParamValue> paramValues) {
        this.paramInfos = paramInfos;
        this.paramValues = paramValues;
    }

    public JSONObject toJson() {
        return F.jsonMap(paramValues, (paramName, paramValue) ->
            paramValue.toJson(paramInfos.get(paramName))
        );
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public Params randomlyShiftOneParam(Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {
        if (paramValues.isEmpty()) {return this;}

        String paramNameToShift = F.randomElement(paramValues.keySet(), rand);
        Object paramInfo = paramInfos.get(paramNameToShift);

        HashMap<String,ParamValue> newParamValues = new HashMap<>(paramValues);
        newParamValues.compute(paramNameToShift, (paramName,paramValue)-> paramValue.randomlyShift(paramInfo, shiftsWithProbabilities, rand));

        return new Params(paramInfos, newParamValues);
    }

    static Distribution<AB<Integer,Double>> mkShiftDist(List<AB<Integer,Double>> shiftsWithProbabilities, int currentIndex, int numValues) {
        Distribution<AB<Integer,Double>> ret = new Distribution<>();
        for (AB<Integer,Double> shiftWithProbability: shiftsWithProbabilities) {
            int resultIndex = currentIndex + shiftWithProbability._1();
            if (resultIndex >= 0 && resultIndex < numValues) {
                ret.add(shiftWithProbability);
            }
        }
        return ret;
    }

    public static List<AB<Integer,Double>> parseShifts(JSONArray jsonArray) {
        List<AB<Integer,Double>> ret = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray pair = jsonArray.getJSONArray(i);
            ret.add(new AB<>(pair.getInt(0), pair.getDouble(1)));
        }
        return ret;
    }


    public static void main(String[] args) {

        Map<String,Integer> map1 = new HashMap<>();
        map1.put("how to", 1024);
        map1.put("mr", 1024);

        Map<String,Integer> map2 = new HashMap<>(map1);

        map2.compute("how to", (k,v)-> v*2);

        Log.it(map1.get("how to"));
        Log.it(map2.get("how to"));


    }

}
