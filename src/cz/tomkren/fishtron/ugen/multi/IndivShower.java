package cz.tomkren.fishtron.ugen.multi;

import java.util.function.Function;

/**Created by tom on 20.03.2017.*/

public interface IndivShower<Indiv> {

    String indivToStdout(Indiv indiv);

    default Object indivToJson(Indiv indiv) {
        return indivToStdout(indiv);
    }

}
