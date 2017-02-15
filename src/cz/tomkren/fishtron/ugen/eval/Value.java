package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.types.Type;

/** Created by user on 15. 2. 2017. */

public interface Value extends EvalCode {

    Object getValue();

    @Override
    default Object evalCode(Type t) {
        return getValue();
    }
}
