package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.utils.Checker;

import java.util.List;
import java.util.Random;

/** Created by tom on 07.03.2017. */

public interface MultiEvaOpts<Indiv extends MultiIndiv> {

    // Simple Parameters
    int getNumEvaluations();
    int getNumIndividualsToGenerate();
    int getMinPopulationSizeToOperate();
    int getMaxPopulationSize();

    //boolean saveBests();

    int getTimeLimit(); // in seconds
    long getSleepTime(); // in milliseconds (was hardcoded 2000)

    // Generator
    IndivGenerator<Indiv> getGenerator();
    default boolean generateMaxOfCapabilitiesAndNeeds() {return true;}

    // Evaluation
    //List<Boolean> getIsMaximizationList();
    //List<String> getFitnessLabels();
    FitnessSignature getFitnessSignature();
    MultiEvalManager<Indiv> getEvalManager();

    // Selection, operators etc.
    MultiSelection<Indiv> getParentSelection();
    Distribution<Operator<Indiv>> getOperators();
    Random getRandom();

    Checker getChecker();

}
