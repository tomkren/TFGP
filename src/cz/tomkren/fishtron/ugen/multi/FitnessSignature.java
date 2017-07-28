package cz.tomkren.fishtron.ugen.multi;

import com.google.common.collect.Sets;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by tom on 28.07.2017.
 */
public class FitnessSignature {

    private final int numFitnesses;
    private final List<Boolean> isMaxis;
    private final List<String> fitnessLabels;

    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final Set<String> possibleDirections = Sets.newHashSet(MAX, MIN);

    public FitnessSignature(JSONArray json) {
        numFitnesses = json.length();
        isMaxis = new ArrayList<>(numFitnesses);
        fitnessLabels = new ArrayList<>(numFitnesses);

        for (int i = 0; i<numFitnesses; i++) {

            JSONArray fitnessInfo = json.getJSONArray(i);
            String directionStr = fitnessInfo.getString(0);
            String fitnessLabel = fitnessInfo.getString(1);

            if (!possibleDirections.contains(directionStr)) {
                throw new Error("'"+ directionStr +"' is not supported fitness direction value in FitnessSignature. It must be either of: "+possibleDirections);
            }

            isMaxis.add(directionStr.equals(MAX));
            fitnessLabels.add(fitnessLabel);
        }
    }

    public List<Boolean> getIsMaximizationList() { return isMaxis; }
    public List<String> getFitnessLabels() { return fitnessLabels; }

    public int getNumFitnesses() {return numFitnesses;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<numFitnesses; i++) {
            sb.append(isMaxis.get(i) ? "maximize" : "minimize").append(" ").append(fitnessLabels.get(i));
            if (i < numFitnesses - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
