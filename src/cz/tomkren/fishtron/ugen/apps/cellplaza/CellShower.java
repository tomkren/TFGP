package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.multi.AppTreeMI;
import cz.tomkren.fishtron.ugen.multi.IndivShower;

/**Created by tom on 20.03.2017.*/

public class CellShower implements IndivShower<AppTreeMI> {

    @Override
    public String indivToStdout(AppTreeMI appTreeMI) {
        return appTreeMI.getTree().toString();
    }

    @Override
    public Object indivToJson(AppTreeMI appTreeMI) {
        return indivToStdout(appTreeMI);
    }
}
