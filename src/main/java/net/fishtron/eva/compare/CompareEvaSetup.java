package net.fishtron.eva.compare;

import net.fishtron.apps.cellplaza.InteractiveComparator;
import net.fishtron.eva.EvaSetup;
import net.fishtron.eva.multi.AppTreeMI;

/**
 * Created by tom on 28.10.2017.
 */
public interface CompareEvaSetup extends EvaSetup {

    CompareOpts<AppTreeMI> getOpts();
    InteractiveComparator getInteractiveComparator();

}
