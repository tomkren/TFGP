package cz.tomkren.fishtron.ugen.params;

import cz.tomkren.fishtron.eva.Distribution;
import net.fishtron.utils.AB;
import org.json.JSONArray;

import java.util.List;
import java.util.Random;

/**Created by tom on 19.03.2017.*/

class SingleEnumValue implements ParamValue {

    private final int selectedEnumValueIndex;

    SingleEnumValue(JSONArray paramInfo, Random rand) {
        this(rand.nextInt(paramInfo.length()));
    }

    private SingleEnumValue(int selectedEnumValueIndex) {
        this.selectedEnumValueIndex = selectedEnumValueIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleEnumValue that = (SingleEnumValue) o;
        return selectedEnumValueIndex == that.selectedEnumValueIndex;
    }

    @Override
    public int hashCode() {
        return selectedEnumValueIndex;
    }

    @Override
    public Object toJson(Object paramInfo) {
        if (!(paramInfo instanceof JSONArray)) {throw new Error("SingleEnumValue must be given JSONArray as paramInfo");}

        return ((JSONArray)paramInfo).get(selectedEnumValueIndex);
    }

    @Override
    public ParamValue randomlyShift(Object paramInfo, List<AB<Integer, Double>> shiftsWithProbabilities, Random rand) {
        if (!(paramInfo instanceof JSONArray)) {throw new Error("SingleEnumValue must be given JSONArray as paramInfo");}

        int numValues = ((JSONArray)paramInfo).length();
        Distribution<AB<Integer,Double>> shiftDist = Params.mkShiftDist(shiftsWithProbabilities, selectedEnumValueIndex, numValues);
        int shift = shiftDist.isEmpty() ? 0 : shiftDist.get(rand)._1();

        return new SingleEnumValue(selectedEnumValueIndex + shift);
    }
}
