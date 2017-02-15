package cz.tomkren.fishtron.eva;

import cz.tomkren.utils.Weighted;

public interface FitIndiv extends Weighted {

    FitVal getFitVal();
    void setFitVal(FitVal fitVal);
    Object computeValue();

    @Override
    default double getWeight() {
        FitVal fitVal = getFitVal();
        if (fitVal == null) {throw new Error("fitVal must be not-null!");}
        return fitVal.getVal();
    }
}
