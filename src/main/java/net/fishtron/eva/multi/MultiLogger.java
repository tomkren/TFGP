package net.fishtron.eva.multi;

/**Created by tom on 07.03.2017.*/

public interface MultiLogger<Indiv extends MultiIndiv> {

    void log(int run, int evalId, MultiEvalResult<Indiv> evalResult);

}
