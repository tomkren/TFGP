package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Params;

/** Created by user on 15. 2. 2017. */

public interface Value extends EvalCode {

    Object getValue();

    @Override
    default Object evalCode(Params params, Type t) {
        return getValue();
    }
}
