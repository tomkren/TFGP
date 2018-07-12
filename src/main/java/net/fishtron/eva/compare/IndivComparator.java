package net.fishtron.eva.compare;

import net.fishtron.eva.multi.MultiIndiv;
import net.fishtron.server.api.Api;

import java.util.List;

/**
 * Created by tom on 09.07.2018.
 */
public interface IndivComparator<Indiv extends MultiIndiv> extends Api {

    Indiv compareFun(List<Indiv> indivs);

}
