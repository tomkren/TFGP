package net.fishtron.eva.multi;

import net.fishtron.utils.AB;
import net.fishtron.utils.Log;
import net.fishtron.utils.Stopwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class MultiUtils {

    private static <Indiv extends MultiIndiv> boolean dominates(Indiv i1, Indiv i2, List<Boolean> isMaxis) {

        int n = i1.getFitnessSize();
        if (n != i2.getFitnessSize()) {throw new Error("FitVal list sizes do not match.");}
        if (n != isMaxis.size()) {throw new Error("isMaximization size does not match fitVal list sizes.");}

        boolean isStrong = false;
        for (int i = 0; i < n; i++) {

            double fitness1 = i1.getFitness(i);
            double fitness2 = i2.getFitness(i);

            boolean isMaxi = isMaxis.get(i);
            boolean i1wins = isMaxi ? fitness1 > fitness2 : fitness1 < fitness2;
            boolean i2wins = isMaxi ? fitness1 < fitness2 : fitness1 > fitness2;

            if (i1wins) {
                isStrong = true;
            }
            if (i2wins) {
                return false;
            }
        }
        return isStrong || i1.getId() > i2.getId();
        //return isStrong;
    }


    private static <Indiv extends MultiIndiv> List<Indiv> getNonDominatedFront(List<Indiv> indivs, List<Boolean> isMaxis) {

        List<Indiv> front = new ArrayList<>();

        for (int i = 0; i < indivs.size(); i++) {
            boolean dominated = false;

            MultiIndiv indiv = indivs.get(i);

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


    private static void checkFitnessSize(List<Boolean> isMaxis) {
        if (isMaxis.size() != 2) {
            throw new Error("This implementation of the algorithm assumes 2-objective problem (it is "+isMaxis.size()+"-objective), sorry.");
        }
    }


    private static <Indiv extends MultiIndiv> void assignCrowdingDistances(List<Indiv> front, List<Boolean> isMaxis) {
        checkFitnessSize(isMaxis);

        front.sort(new ObjectiveValueComparator<>(0, isMaxis));

        int iLast = front.size() - 1;

        front.get(0).setCrowdingDistance(Double.MAX_VALUE);
        front.get(iLast).setCrowdingDistance(Double.MAX_VALUE);

        for (int i = 1; i < iLast; i++) {

            MultiIndiv prev  = front.get(i-1);
            MultiIndiv indiv = front.get(i);
            MultiIndiv next  = front.get(i+1);

            double d0 = next.getFitness(0) - prev.getFitness(0);
            double d1 = next.getFitness(1) - prev.getFitness(1);

            indiv.setCrowdingDistance(Math.abs(d0) + Math.abs(d1));
        }
    }


    private static <Indiv extends MultiIndiv> int assignCrowdingDistances_test(List<Indiv> front, List<Boolean> isMaxis) {
        checkFitnessSize(isMaxis);

        int numOk = 0;

        front.sort(new ObjectiveValueComparator<>(0, isMaxis));

        int iLast = front.size() - 1;

        if (front.get(0).getCrowdingDistance() == Double.MAX_VALUE) {
            numOk ++;
        } else {
            throw new Error("Not true: front.get(0).getCrowdingDistance() == Double.MAX_VALUE");
        }

        if (front.get(iLast).getCrowdingDistance() == Double.MAX_VALUE) {
            numOk ++;
        } else {
            throw new Error("Not true: front.get(iLast).getCrowdingDistance() == Double.MAX_VALUE");
        }

        for (int i = 1; i < iLast; i++) {

            MultiIndiv prev  = front.get(i-1);
            MultiIndiv indiv = front.get(i);
            MultiIndiv next  = front.get(i+1);

            double d0 = next.getFitness(0) - prev.getFitness(0);
            double d1 = next.getFitness(1) - prev.getFitness(1);

            if (indiv.getCrowdingDistance() == (Math.abs(d0) + Math.abs(d1))) {
                numOk ++;
            } else {
                throw new Error("Not true: indiv.getCrowdingDistance() == (Math.abs(d0) + Math.abs(d1))");
            }
        }

        return numOk;
    }


    public static <Indiv extends MultiIndiv> AB<Indiv,Integer> assignFrontsAndDistances_test(Collection<Indiv> indivs, List<Boolean> isMaxis) {

        Stopwatch sw = new Stopwatch();
        Log.it_noln("Starting front assignment test ... ");
        int numOk = 0;
        int numOkDist = 0;

        checkFitnessSize(isMaxis);

        List<Indiv> indivsToAssign = new ArrayList<>(indivs);
        int frontNumber = 1;

        List<Indiv> front = null;
        while (!indivsToAssign.isEmpty()) {
            front = getNonDominatedFront(indivsToAssign, isMaxis);
            indivsToAssign.removeAll(front);

            for (Indiv indiv : front) {
                //indiv.setFront(frontNumber);
                if (indiv.getFront() == frontNumber) {
                    //Log.it("OK front!");
                    numOk++;
                } else {
                    throw new Error("BAD FRONT!");
                }
            }
            frontNumber ++;

            numOkDist += assignCrowdingDistances_test(front, isMaxis);
        }

        Log.it(numOk+" front tests performed successfully. (numOkDist = "+numOkDist+") It took "+sw.restart());

        return AB.mk(front == null ? null : findWorstIndiv(front), frontNumber-1);
    }

    public static <Indiv extends MultiIndiv> AB<Indiv,Integer> assignFrontsAndDistances_martin(Collection<Indiv> indivs, List<Boolean> isMaxis) {
        checkFitnessSize(isMaxis);

        ArrayList<Indiv> listIndivs = new ArrayList<>(indivs);

        listIndivs.sort(new ObjectiveValueComparator2<>(isMaxis));

        ArrayList<Indiv> notAssigned = listIndivs;

        ArrayList<Indiv> front = new ArrayList<>();
        ArrayList<Indiv> stillNotAssigned = new ArrayList<>();

        int frontNumber = 1;
        while (notAssigned.size() > 0) {

            front.clear();

            front.add(notAssigned.get(0));
            Indiv lastNonDom = front.get(0);
            lastNonDom.setFront(frontNumber);

            for (int i = 1; i < notAssigned.size(); i++) {
                Indiv check = notAssigned.get(i);
                if (dominates(lastNonDom, check, isMaxis)) {
                    stillNotAssigned.add(check);
                } else {
                    check.setFront(frontNumber);
                    front.add(check);
                    lastNonDom = check;

                }
            }

            frontNumber++;
            assignCrowdingDistances(front, isMaxis); // dříve bylo zakomentováno, kvůli domnělé optimalizaci

            notAssigned = stillNotAssigned;
            stillNotAssigned = new ArrayList<>();
        }

        //assignCrowdingDistances(front, isMaxis); // we need to assign crowding distance in the last front only? well, not really

        return AB.mk(findWorstIndiv(front), frontNumber-1);
    }

    private static <Indiv extends MultiIndiv> Indiv findWorstIndiv(List<Indiv> front) {

        assert !front.isEmpty();

        Indiv worst = front.get(0);
        double worstDistance = Double.MAX_VALUE;
        for (Indiv indiv : front) {
            double distance = indiv.getCrowdingDistance();
            if (distance < worstDistance) {
                worst = indiv;
                worstDistance = distance;
            }
        }
        return worst;
    }

    public static <Indiv extends MultiIndiv> double calculateHypervolume(List<Indiv> indivs, List<Double> reference, List<Boolean> isMaxis) {
        checkFitnessSize(isMaxis);

        List<Indiv> front = getNonDominatedFront(indivs, isMaxis);

        if (front == null) {return 0.0;}

        front.sort(new ObjectiveValueComparator<>(0,isMaxis));

        double volume = 0.0;
        int size = front.size();
        double x0,x1;

        for (int i = 0; i < size - 1; i++) {

            MultiIndiv indiv = front.get(i);
            MultiIndiv next  = front.get(i+1);

            x0 = next.getFitness(0) - indiv.getFitness(0);
            x1 = reference.get(1) - indiv.getFitness(1);

            volume += Math.abs(x0) * Math.abs(x1);
        }

        MultiIndiv last = front.get(size - 1);

        x0 = reference.get(0) - last.getFitness(0);
        x1 = reference.get(1) - last.getFitness(1);

        volume += Math.abs(x0) * Math.abs(x1);

        return volume;
    }



    static class ObjectiveValueComparator<Indiv extends MultiIndiv> implements Comparator<Indiv> {

        private final int index;
        private final boolean isMaxi;

        ObjectiveValueComparator(int index, List<Boolean> isMaxis) {
            this.index = index;
            this.isMaxi = isMaxis.get(index);
        }

        @Override
        public int compare(Indiv o1, Indiv o2) {

            double val1 = o1.getFitness(index);
            double val2 = o2.getFitness(index);

            return (isMaxi ? -1 : 1) * Double.compare(val1, val2);
        }
    }


    static class ObjectiveValueComparator2<Indiv extends MultiIndiv> implements Comparator<Indiv> {

        private final List<Boolean> isMaxis;

        ObjectiveValueComparator2(List<Boolean> isMaxis) {
            this.isMaxis = isMaxis;
        }

        @Override
        public int compare(Indiv o1, Indiv o2) {

            for (int i = 0 ; i < isMaxis.size(); i++) {

                double val1 = o1.getFitness(i);
                double val2 = o2.getFitness(i);

                int compareRes = Double.compare(val1, val2);

                if (compareRes != 0) {
                    return (isMaxis.get(i) ? -1 : 1) * compareRes;
                }
            }

            return - Double.compare(o1.getId(), o2.getId());

            //return 0;
        }
    }



}
