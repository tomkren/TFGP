package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/** Created by user on 16. 2. 2017.*/



public class Params {

    private final JSONObject paramsInfo;
    private final HashMap<String,Integer> selectedParamIndices;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Params params = (Params) o;
        return selectedParamIndices.equals(params.selectedParamIndices);
    }

    @Override
    public int hashCode() {
        return selectedParamIndices.hashCode();
    }

    Params(JSONObject paramsInfo, Random rand) {
        this.paramsInfo = paramsInfo;
        selectedParamIndices = new HashMap<>();
        for (Object key : paramsInfo.keySet()) {
            String paramName = (String) key;
            JSONArray possibleValues = paramsInfo.getJSONArray(paramName);
            selectedParamIndices.put(paramName, rand.nextInt(possibleValues.length()));
        }
    }

    private Params(JSONObject paramsInfo, HashMap<String, Integer> selectedParamIndices) {
        this.paramsInfo = paramsInfo;
        this.selectedParamIndices = selectedParamIndices;
    }

    public JSONObject toJson() {
        return F.jsonMap(selectedParamIndices, (paramName,selectedIndex) ->
                paramsInfo.getJSONArray(paramName).get(selectedIndex)
        );
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    Params randomlyShiftOneParam(Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {
        if (selectedParamIndices.isEmpty()) {return this;}

        String paramName = F.list(selectedParamIndices.entrySet()).randomElement(rand).getKey();
        HashMap<String,Integer> newIndices = new HashMap<>(selectedParamIndices);

        newIndices.compute(paramName, (k,index)-> {
            int numValues =  paramsInfo.getJSONArray(paramName).length();
            Distribution<AB<Integer,Double>> shiftDist = mkShiftDist(shiftsWithProbabilities, index, numValues);
            return index + (shiftDist.isEmpty() ? 0 : shiftDist.get(rand)._1());
        });

        return new Params(paramsInfo, newIndices);
    }

    private static Distribution<AB<Integer,Double>> mkShiftDist(List<AB<Integer,Double>> shiftsWithProbabilities, int currentIndex, int numValues) {
        Distribution<AB<Integer,Double>> ret = new Distribution<>();
        for (AB<Integer,Double> shiftWithProbability: shiftsWithProbabilities) {
            int resultIndex = currentIndex + shiftWithProbability._1();
            if (resultIndex >= 0 && resultIndex < numValues) {
                ret.add(shiftWithProbability);
            }
        }
        return ret;
    }


    public static void main(String[] args) {

        Map<String,Integer> map1 = new HashMap<>();
        map1.put("hovno", 1024);
        map1.put("mrdečka", 1024);

        Map<String,Integer> map2 = new HashMap<>(map1);

        map2.compute("hovno", (k,v)-> v*2);

        Log.it(map1.get("hovno"));
        Log.it(map2.get("hovno"));


    }

}
