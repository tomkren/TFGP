package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.utils.F;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**Created by tom on 12.03.2017.*/

public interface Rule {

    int nextState(Cell cell);

    int NUM_NEIGHBOURS = 8;

    static int neighbourCases(int numStates) {return (numStates - 1) * NUM_NEIGHBOURS + 1;}
    static int numBits(int numStates) {return numStates * neighbourCases(numStates);}


    static Rule fromBits(JSONArray bits, int numStates) {
        return fromBits(F.map(bits, x->(int)x), numStates);
    }

    static Rule fromBits(List<Integer> bits, int numStates) {

        if (bits.size() != numBits(numStates)) {throw new Error("bits have wrong size: "+bits.size());}

        int nCases = neighbourCases(numStates);

        List<List<Integer>> ruleTable = new ArrayList<>(numStates);

        int i = 0;
        for (int s = 0; s < numStates; s++) {
            List<Integer> tableRow = new ArrayList<>(nCases);
            for (int nSum = 0; nSum < nCases; nSum++) {
                tableRow.add(bits.get(i));
                i++;
            }
            ruleTable.add(tableRow);
        }

        return mk(ruleTable);
    }


    static Rule mk(List<List<Integer>> ruleTable) {
        return cell -> ruleTable.get(cell.getState()).get(cell.getSumNeighbourState());
    }

    static int GOL(Cell cell) {
        int nSum = cell.getSumNeighbourState();
        if (cell.getState() == 0) {return nSum == 3 ? 1 : 0;}
        else {return (nSum == 2 || nSum == 3) ? 1 : 0;}
    }


}
