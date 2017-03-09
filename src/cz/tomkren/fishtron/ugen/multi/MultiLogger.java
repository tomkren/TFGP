package cz.tomkren.fishtron.ugen.multi;

/**Created by tom on 07.03.2017.*/

public interface MultiLogger<Indiv extends MultiIndiv> {

    void iterativeLog(int run, int evalId, MultiPopulation<Indiv> pop, MultiEvalResult<Indiv> evalResult);

}
