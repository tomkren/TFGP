package cz.tomkren.fishtron.ugen.multi;


import cz.tomkren.utils.*;
import org.json.JSONObject;

import java.util.*;

/** Created by tom on 07.03.2017. */

public class MultiPopulation<Indiv extends MultiIndiv> {

    private final List<Boolean> isMaxis; // TODO předělat aby pracovalo s FitnessSignature víc.

    private Set<Indiv> yetToBeEvaluated;
    private Set<Indiv> individuals;
    private Set<Indiv> removedIndividuals;

    private int numUniqueCheckFails;
    private List<Indiv> terminators;
    private Indiv worstIndividual;


    MultiPopulation(FitnessSignature fitnessSignature) {
        if (fitnessSignature.getNumFitnesses() == 0) {throw new Error("0-fitness error : isMaxis is empty.");}

        this.isMaxis = fitnessSignature.getIsMaximizationList();

        individuals = new HashSet<>();
        removedIndividuals = new HashSet<>();
        yetToBeEvaluated = new HashSet<>();

        numUniqueCheckFails = 0;
        terminators = new ArrayList<>();
        worstIndividual = null;
    }

    private String getPopulationInfo() {
        return "population-size= "+individuals.size() +
                ", num-removed= "+removedIndividuals.size() +
                ", num-waiting-for-evaluation= "+ yetToBeEvaluated.size() +
                ", num-unique-check-fails= "+ numUniqueCheckFails;
    }

    List<Indiv> keepOnlyNew_and_checkout(List<Indiv> indivsToFilter) {
        return F.filter(indivsToFilter, this::testIsNew);
    }

    private boolean testIsNew(Indiv indiv) {
        //Indiv indiv = indivData._1();
        if (individuals.contains(indiv) || removedIndividuals.contains(indiv) || yetToBeEvaluated.contains(indiv)) {
            numUniqueCheckFails ++;
            Log.it("  -> UniqueCheck failed, numUniqueCheckFails = "+numUniqueCheckFails);
            return false;
        }

        Log.it("  -> New individual found!");
        markYetToBeEvaluated(indiv);
        return true;
    }

    private void markYetToBeEvaluated(Indiv indivSelectedForEvaluation) {
        yetToBeEvaluated.add(indivSelectedForEvaluation);
    }


    Indiv select(MultiSelection<Indiv> selection) {
        return selection.select(individuals, isMaxis);
    }

    // !!! TODO určitě předělat na addIndividuals, pač neefektivní vzledem k tomu že se furt přepočítávaj ty fronty !!! !!!
    void addIndividual(Indiv indiv, StringBuilder info) {

        boolean wasThere = yetToBeEvaluated.remove(indiv);

        // todo radi misto tech dvou cheku dat warningv rači.

        if (!wasThere) {
            throw new Error("INDIV 'WAS NOT THERE'!");
        }
        if (individuals.contains(indiv) || removedIndividuals.contains(indiv)) {

            throw new Error("UniqueCheckFail in addIndividual should be unreachable!");

            /*numUniqueCheckFails ++;
            Log.it("(check fail)");
            return false;*/
        }

        if (indiv.isTerminator()) {
            terminators.add(indiv);
        }

        individuals.add(indiv);


        if (isMaxis.size() > 1) {

            Stopwatch sw = new Stopwatch();

            /*AB<Indiv,Integer> assignRes_old = MultiUtils.assignFrontsAndDistances(individuals, isMaxis);
            Log.it("(numFronts: "+ assignRes_old._2() +" & fronts-assigning took: "+ sw.restart()+")");*/

            AB<Indiv,Integer> assignRes = MultiUtils.assignFrontsAndDistances_martin(individuals, isMaxis);



            worstIndividual = assignRes._1();

            String msg = "After AddIndividual: numFronts="+ assignRes._2() +" fronts-assigning-took="+ sw.restart()+"\n";
            msg += "  Worst-individual-fitness = "+ worstIndividual.getFitness() +"\n";
            msg += "  Population-Info: "+getPopulationInfo()+"\n";
            info.append(msg);

            assert worstIndividual != null;

            /*if (!Objects.equals(assignRes._2(), assignRes_old._2())) {
                throw new Error("front numbers do not match");
            }*/

            //Log.it("(numFronts: "+ assignRes._2() +" & fronts-assigning took: "+ sw.restart()+")");

        } else {
            worstIndividual = findWorstIndividual_singleFitness();
        }

        //return true;
    }


    void removeWorstIndividual() {
        boolean success = individuals.remove(worstIndividual);
        if (!success) {
            Log.it("MISSING WORST INDIVIDUAL: "+worstIndividual);
            throw new Error("Population dos not contain the worst individual.");
        }
        removedIndividuals.add(worstIndividual);
    }


    // TODO ověřit že max fakt dává nejhoršího :) !!!!
    private Indiv findWorstIndividual_singleFitness() {
        Comparator<Indiv> singleComparator = (i1, i2) -> MultiIndiv.singleCompare(i1,i2,isMaxis.get(0));
        return individuals.stream().max(singleComparator).orElse(null);
    }




    public int size() {
        return individuals.size();
    }

    public int getNumUniqueCheckFails() {
        return numUniqueCheckFails;
    }

    public List<Indiv> getTerminators() {
        return terminators;
    }

    public Set<Indiv> getRemovedIndividuals() {
        return removedIndividuals;
    }

}
