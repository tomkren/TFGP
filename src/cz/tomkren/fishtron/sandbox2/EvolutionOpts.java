package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.*;
import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.utils.Distribution;

import java.util.Random;

/** Created by tom on 1. 6. 2016.*/

public interface EvolutionOpts<Indiv extends FitIndiv> {

    // Simple Parameters
    int getNumEvaluations();
    int getNumIndividualsToGenerate();
    int getMinPopulationSizeToOperate();
    int getMaxPopulationSize();
    boolean isUniquenessCheckPerform();
    boolean saveBest();
    int getTimeLimit(); // in seconds

    // Generator
    IndivGenerator<Indiv> getGenerator();

    // Evaluation
    EvalManager<Indiv> getEvalManager();

    // Selection, operators etc.
    Selection<Indiv> getParentSelection();
    Distribution<Operator<Indiv>> getOperators();
    Random getRandom();

}
