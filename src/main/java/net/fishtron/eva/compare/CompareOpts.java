package net.fishtron.eva.compare;

import net.fishtron.server.api.Api;
import net.fishtron.utils.Distribution;
import net.fishtron.eva.IndivGenerator;
import net.fishtron.eva.Operator;
import net.fishtron.eva.multi.MultiIndiv;
import net.fishtron.utils.Checker;

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

    Api getApi();

}
