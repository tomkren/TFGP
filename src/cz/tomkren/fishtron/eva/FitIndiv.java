package cz.tomkren.fishtron.eva;

import cz.tomkren.utils.Weighted;

public interface FitIndiv extends Weighted {

    FitVal getFitVal();
    void setFitVal(FitVal fitVal);
    Object computeValue();

}
