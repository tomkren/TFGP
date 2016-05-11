package cz.tomkren.fishtron.eva;

import cz.tomkren.utils.Weighted;

import java.util.List;

public interface Operator<Indiv> extends Weighted {
    int getNumInputs();
    List<Indiv> operate(List<Indiv> parents);
}
