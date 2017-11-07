package net.fishtron.eva.multi;

import net.fishtron.eva.EvaSetup;

/**
 * Created by tom on 09.10.2017.
 */
public interface MultiEvaSetup extends EvaSetup {

    MultiEvaOpts<AppTreeMI> getEvaOpts();

}
