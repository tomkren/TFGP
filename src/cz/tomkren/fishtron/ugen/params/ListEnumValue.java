package cz.tomkren.fishtron.ugen.params;

import cz.tomkren.fishtron.eva.Distribution;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Created by tom on 19.03.2017.*/

public class ListEnumValue implements ParamValue {

    static final String TYPE_VALUE = "list";
    private static final String LENGTH_KEY = "length";
    private static final String VALUES_KEY = "values";


    private final List<Integer> selectedIndices;

    ListEnumValue(JSONObject paramInfo, Random rand) {

        if (!paramInfo.has(LENGTH_KEY) || !paramInfo.has(VALUES_KEY)) {
            throw new Error("ListEnumValue paramInfo must have '"+LENGTH_KEY+"' and '"+VALUES_KEY+"' keys.");
        }

        int length = paramInfo.getInt(LENGTH_KEY);
        JSONArray values = paramInfo.getJSONArray(VALUES_KEY);
        int numValues = values.length();

        selectedIndices = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            selectedIndices.add(rand.nextInt(numValues));
        }
    }

    private ListEnumValue(List<Integer> selectedIndices) {
        this.selectedIndices = selectedIndices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListEnumValue that = (ListEnumValue) o;
        return selectedIndices.equals(that.selectedIndices);
    }

    @Override
    public int hashCode() {
        return selectedIndices.hashCode();
    }

    @Override
    public Object toJson(Object paramInfo) {
        if (!(paramInfo instanceof JSONObject)) {throw new Error("ListEnumValue must be given JSONObject as paramInfo");}
        JSONArray values = ((JSONObject) paramInfo).getJSONArray(VALUES_KEY);
        return F.jsonMap(selectedIndices, values::get);
    }

    @Override
    public ParamValue randomlyShift(Object paramInfo, List<AB<Integer, Double>> shiftsWithProbabilities, Random rand) {
        if (!(paramInfo instanceof JSONObject)) {throw new Error("ListEnumValue must be given JSONObject as paramInfo");}

        JSONObject paramInfo_JO = (JSONObject) paramInfo;
        int length    = paramInfo_JO.getInt(LENGTH_KEY);
        int numValues = paramInfo_JO.getJSONArray(VALUES_KEY).length();

        int elemPosToShift = rand.nextInt(length);
        int selectedEnumValueIndex = selectedIndices.get(elemPosToShift);

        Distribution<AB<Integer,Double>> shiftDist = Params.mkShiftDist(shiftsWithProbabilities, selectedEnumValueIndex, numValues);
        int shift = shiftDist.isEmpty() ? 0 : shiftDist.get(rand)._1();

        int newIndex = selectedEnumValueIndex + shift;

        List<Integer> newSelectedIndices = new ArrayList<>(selectedIndices);
        newSelectedIndices.set(elemPosToShift, newIndex);
        return new ListEnumValue(newSelectedIndices);
    }
}
