package net.fishtron.eval;

import java.util.function.Function;

/** Created by user on 14. 2. 2017. */

public interface Fun2 extends Fun {

    Function<Object,Object> applyFun2(Object arg);

    @Override
    default Object applyFun(Object arg) {
        return applyFun2(arg);
    }
}
