package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.utils.F;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**Created by tom on 12.03.2017.*/

public interface Rule {

    int nextState(Cell cell);

    int neighbourCases = 9;
    int numBits = 2 * neighbourCases;

    static Rule mk(int code) {
        List<Boolean> bits = new ArrayList<>(numBits);
        for (int i = 0; i < numBits; i++) {
            bits.add(code % 2 == 1);
            code /= 2;
        }
        return fromBits(bits);
    }

    static Rule fromBits(JSONArray bits) {
        return fromBits(F.map(bits, x -> (int)x == 1));
    }

    static Rule fromBits(List<Boolean> bits) {
        if (bits.size() != numBits) {throw new Error("bits have wrong size: "+bits.size());}

        int num1 = F.filter(bits.subList(0,neighbourCases),x->x).size();
        int num2 = F.filter(bits.subList(neighbourCases,numBits),x->x).size();

        List<Integer> resurrectNums = new ArrayList<>(num1);
        List<Integer> surviveNums   = new ArrayList<>(num2);

        for (int n = 0; n < neighbourCases; n++) {
            if (bits.get(neighbourCases + n)) {surviveNums.add(n);}
            if (bits.get(n)) {resurrectNums.add(n);}
        }

        return mk(resurrectNums, surviveNums);
    }

    static Rule mk(Integer... args) {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> current = list1;
        List<Integer> list2 = new ArrayList<>();
        for (Integer arg : args) {
            if (arg == null) {
                current = list2;
            } else {
                current.add(arg);
            }
        }
        return mk(list1, list2);
    }

    static Rule mk(Collection<Integer> resurrectNums, Collection<Integer> surviveNums) {
        return cell -> {
            int numAlive = cell.getNumAliveNeighbours();
            if (cell.isAlive()) {
                return surviveNums.contains(numAlive) ? Cell.ALIVE : Cell.DEAD;
            } else {
                return resurrectNums.contains(numAlive) ? Cell.ALIVE : Cell.DEAD;
            }
        };
    }

    static int GOL(Cell cell) {
        int numAlive = cell.getNumAliveNeighbours();
        if (cell.isAlive()) {
            return (numAlive == 2 || numAlive == 3) ? Cell.ALIVE : Cell.DEAD;
        } else {
            return numAlive == 3 ? Cell.ALIVE : Cell.DEAD;
        }
    }


}
