package cz.tomkren.fishtron.ugen.eval;

import java.util.function.Function;

/** Created by user on 14. 2. 2017. */

public interface FunCode2 extends FunCode {

    Function<Object,Object> applyFun2(Object arg);

    @Override
    default Object applyFun(Object arg) {
        return applyFun2(arg);
    }
}
