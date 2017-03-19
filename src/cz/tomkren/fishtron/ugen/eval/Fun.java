package cz.tomkren.fishtron.ugen.eval;

import java.util.function.Function;

/** Created by tom on 14. 2. 2017. */

public interface Fun extends Value {

    Object applyFun(Object arg);

    @Override
    default Object getValue() {
        return (Function<Object,Object>) this::applyFun;
    }

}
