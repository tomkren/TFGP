package cz.tomkren.fishtron.ugen.multi;

import java.util.Collection;

/** Created by tom on 07.03.2017.*/

public interface MultiSelection<Indiv extends MultiIndiv> {

    Indiv select(Collection<Indiv> dist);

}
