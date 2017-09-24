package cz.tomkren.fishtron.eva;

import net.fishtron.utils.F;
import cz.tomkren.utils.Weighted;
import org.json.JSONObject;

import java.util.List;

public interface Operator<Indiv> extends Weighted {
    int getNumInputs();
    List<Indiv> operate(List<Indiv> parents);

    default JSONObject getOperatorInfo() {
        return F.obj("toString", toString());
    }
}
