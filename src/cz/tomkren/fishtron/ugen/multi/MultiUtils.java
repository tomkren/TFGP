package cz.tomkren.fishtron.ugen.multi;

import com.sun.org.apache.xpath.internal.operations.Mult;
import cz.tomkren.fishtron.eva.FitVal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Created by sekol on 06.03.2017. */

public class MultiUtils {

    public static boolean dominates(MultiFitIndiv i1, MultiFitIndiv i2, List<Boolean> isMaxis) {

        int n = i1.getFitVals().size();
        if (n != i2.getFitVals().size()) {throw new Error("FitVal list sizes do not match.");}
        if (n != isMaxis.size()) {throw new Error("isMaximization size does not match fitVal list sizes.");}

        boolean isStrong = false;
        for (int i = 0; i < n; i++) {

            double val1 = i1.getValue(i);
            double val2 = i2.getValue(i);

            boolean isMaxi = isMaxis.get(i);
            boolean i1wins = isMaxi ? val1 > val2 : val1 < val2;
            boolean i2wins = isMaxi ? val1 < val2 : val1 > val2;

            if (i1wins) {
                isStrong = true;
            }
            if (i2wins) {
                return false;
            }
        }
        return isStrong;
    }


    public static List<MultiFitIndiv> getNonDominatedFront(List<MultiFitIndiv> indivs, List<Boolean> isMaxis) {
        if (indivs.isEmpty()) {return null;}

        List<MultiFitIndiv> front = new ArrayList<>();

        for (int i = 0; i < indivs.size(); i++) {
            boolean dominated = false;

            MultiFitIndiv indiv = indivs.get(i);

            for (int j = 0; j < indivs.size(); j++) {
                if (i != j) {
                    if (dominates(indivs.get(j), indiv, isMaxis)) {
                        dominated = true;
                    }
                }
            }

            if (!dominated) {
                front.add(indivs.get(i));
            }
        }

        return front;
    }


    public static void assignFrontNumbers(List<MultiFitIndiv> indivs, List<Boolean> isMaxis) {

        List<MultiFitIndiv> indivsToAssign = new ArrayList<>(indivs);

        List<MultiFitIndiv> front = getNonDominatedFront(indivsToAssign, isMaxis);
        int frontNumber = 1;
        while (front != null) {
            indivsToAssign.removeAll(front); // TODO ověřit si že funguje pak - equals na tom indivu je třeba !!!
            for (MultiFitIndiv indiv: front) {
                indiv.setFront(frontNumber);
            }
            front = getNonDominatedFront(indivsToAssign, isMaxis);
            frontNumber ++;
        }

    }


    public static void assignCrowdingDistance(List<MultiFitIndiv> front, List<Boolean> isMaxis) {

        if (isMaxis.size() != 2) {
            throw new Error("This implementation of the algorithm assumes bi-objective problem.");
        }

        // TODO ujistit se že nevadí že se fronta přesortí (martin to tam castoval a kopíroval...)


        front.sort(new ObjectiveValueComparator(0, isMaxis));

        front.get(0).setSsc(Double.MAX_VALUE);
        front.get(front.size()-1).setSsc(Double.MAX_VALUE);

        for (int i = 1; i < front.size() - 1; i++) {
            double ssc;
            ssc  = Math.abs(front.get(i+1).getValue(0) - front.get(i-1).getValue(0));
            ssc += Math.abs(front.get(i-1).getValue(1) - front.get(i+1).getValue(1)); // TODO zkonzultovat zda abs stačí kuli isMaxis
            front.get(i).setSsc(ssc);
        }

    }


    public static void assignFrontAndSsc(List<MultiFitIndiv> indivs, List<Boolean> isMaxis) {

        if (isMaxis.size() != 2) {
            throw new Error("This implementation of the algorithm assumes bi-objective problem.");
        }

        List<MultiFitIndiv> indivsToAssign = new ArrayList<>(indivs);

        List<MultiFitIndiv> front = getNonDominatedFront(indivsToAssign, isMaxis);
        int frontNumber = 1;
        while (front != null) {
            indivsToAssign.removeAll(front); //todo dtto
            for (MultiFitIndiv i: front) {
                i.setFront(frontNumber);
            }
            assignCrowdingDistance(front, isMaxis);
            front = getNonDominatedFront(indivsToAssign, isMaxis);
            frontNumber++;
        }
    }

    public static double calculateHypervolume(List<MultiFitIndiv> indivs, List<Double> reference, List<Boolean> isMaxis) {

        if (isMaxis.size() != 2) {
            throw new Error("This implementation of the algorithm assumes bi-objective problem.");
        }

        List<MultiFitIndiv> front = getNonDominatedFront(indivs, isMaxis);

        if (front == null) {return 0.0;}

        front.sort(new ObjectiveValueComparator(0,isMaxis));

        double volume = 0.0;
        int size = front.size();
        for (int i = 0; i < size - 1; i++) {

            MultiFitIndiv indiv = front.get(i);
            MultiFitIndiv next  = front.get(i+1);

            double q1 = reference.get(1) - indiv.getValue(1);
            double q2 = next.getValue(0) - indiv.getValue(0);

            volume += Math.abs(q1) * Math.abs(q2); // TODO zkonzultovat zda abs stačí kuli isMaxis
        }

        MultiFitIndiv last = front.get(size - 1);

        double p1 = reference.get(1) - last.getValue(1);
        double p2 = reference.get(0) - last.getValue(0);

        volume += Math.abs(p1) * Math.abs(p2); // TODO zkonzultovat zda abs stačí kuli isMaxis

        return volume;
    }



    static class ObjectiveValueComparator implements Comparator<MultiFitIndiv> {

        private final int index;
        private final boolean isMaxi;

        ObjectiveValueComparator(int index, List<Boolean> isMaxis) {
            this.index = index;
            this.isMaxi = isMaxis.get(index);
        }

        @Override
        public int compare(MultiFitIndiv o1, MultiFitIndiv o2) {

            double val1 = o1.getFitVals().get(index).getVal();
            double val2 = o2.getFitVals().get(index).getVal();

            return (isMaxi ? -1 : 1) * Double.compare(val1, val2);
        }
    }




}
