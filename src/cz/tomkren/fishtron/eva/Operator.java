package cz.tomkren.fishtron.eva;

import cz.tomkren.utils.F;
import cz.tomkren.utils.Weighted;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONObject;

import java.util.List;

public interface Operator<Indiv> extends Weighted {
    int getNumInputs();
    List<Indiv> operate(List<Indiv> parents);

    default JSONObject getOperatorInfo() {
        return F.obj("toString", toString());
    }
}
