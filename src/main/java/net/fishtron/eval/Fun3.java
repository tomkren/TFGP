package net.fishtron.eval;

import java.util.function.Function;

/** Created by tom on 15. 2. 2017. */

public interface Fun3 extends Fun {

    Function<Object,Function<Object,Object>> applyFun3(Object arg);

    @Override
    default Object applyFun(Object arg) {
        return applyFun3(arg);
    }
}
