package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.utils.Weighted;

import java.util.List;

/**Created by tom on 06.03.2017.*/

public interface MultiFitIndiv extends Weighted {

    List<FitVal> getFitVals();
    void setFront(int frontNumber);
    void setSsc(double ssc);


    default FitVal getFitVal(int i) {
        return getFitVals().get(i);
    }

    default double getValue(int i) {
        return getFitVals().get(i).getVal();
    }
}
