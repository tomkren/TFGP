package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Params;

/** Created by user on 14. 2. 2017. */

public interface EvalCode {
    Object evalCode(Params params, Type t);
}
