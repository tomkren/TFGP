package cz.tomkren.fishtron.ugen.compare;

import cz.tomkren.fishtron.eva.Distribution;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.utils.Checker;

import java.util.Random;

/**Created by tom on 22.03.2017.*/

public interface CompareOpts<Indiv extends MultiIndiv> {

    // Simple Parameters
    int getNumEvaluations();
    int getNumIndividualsToGenerate();
    //int getMinPopulationSizeToOperate();
    int getMaxPopulationSize();

    int getTimeLimit(); // in seconds
    long getSleepTime(); // in milliseconds (was hardcoded 2000)

    // Generator
    IndivGenerator<Indiv> getGenerator();

    boolean compareIndividuals(Indiv i1, Indiv i2);

    // Selection, operators etc.
    CompareSelection<Indiv> getParentSelection();
    Distribution<Operator<Indiv>> getOperators();
    Random getRandom();

    Checker getChecker();

}
