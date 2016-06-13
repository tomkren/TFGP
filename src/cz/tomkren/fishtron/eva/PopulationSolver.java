package cz.tomkren.fishtron.eva;

import cz.tomkren.utils.F;

import java.util.*;

public interface PopulationSolver<Indiv extends FitIndiv> {

    Random getRandom();
    int getNumRuns();
    int getNumGens();
    int getPopSize();
    List<Indiv> generatePop();
    EvaledPop<Indiv> evalPop(List<Indiv> pop, int gen);
    boolean saveBest();
    Distribution<Operator<Indiv>> getOperators();
    Selection<Indiv> getSelection();
    Logger<Indiv> getLogger();

    boolean isUniqueCheckPerformed();
    int getMaxNumUniqueCheckFails();


    default void startRun(int run) {

        Random rand = getRandom();

        int numGens = getNumGens();
        int popSize = getPopSize();
        Distribution<Operator<Indiv>> operators = getOperators();
        Selection<Indiv> selection = getSelection();
        Logger<Indiv> logger = getLogger();

        int gen = 0;
        List<Indiv> pop = generatePop();
        EvaledPop<Indiv> evaledPop = evalPop(pop,gen);
        logger.logPop(run, gen, evaledPop);

        while (gen < numGens-1 && !evaledPop.isTerminating()) {
            pop = new ArrayList<>(popSize);
            Set<Indiv> popSet = new HashSet<>();
            int numUniqueCheckFails = 0;

            // TODO generalize to elitism
            if (saveBest()) {
                Indiv best = evaledPop.getBestIndividual();
                pop.add(best);
                popSet.add(best);
            }

            // fill the new pop
            while (pop.size() < popSize) {

                Operator<Indiv> operator = operators.get(rand);

                int numParents = operator.getNumInputs();
                List<Indiv> parents = new ArrayList<>(numParents);

                Distribution<Indiv> popDistrib = evaledPop.getIndividuals();
                for (int i=0; i<numParents; i++) {
                    parents.add(selection.select(popDistrib)); //(popDistrib.get(rand));
                }

                List<Indiv> operatorChildren = operator.operate(parents);
                int maxNumChildren = popSize - pop.size();
                List<Indiv> children = F.take(maxNumChildren, operatorChildren);



                if (isUniqueCheckPerformed() && numUniqueCheckFails < getMaxNumUniqueCheckFails()) {

                    for (Indiv child : children) {
                        if (!popSet.contains(child)) {
                            popSet.add(child);
                            pop.add(child);
                        } else {
                            numUniqueCheckFails++;
                        }
                    }

                } else {
                    pop.addAll(children);
                }

            }

            gen ++;
            evaledPop = evalPop(pop, gen);
            logger.logPop(run, gen, evaledPop);
        }

        logger.logRun(run);
    }

    default void startRun() {startRun(1);}

    default void startRuns() {
        int numRuns = getNumRuns();
        for (int run=1; run<=numRuns; run++) {
            startRun(run);
        }
    }


}
