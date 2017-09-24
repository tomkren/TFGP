package net.fishtron.eval;

import java.util.function.Function;

/** Created by tom on 19.03.2017.*/

public interface F3 extends Value {

    Object apply3(Object x1, Object x2, Object x3);

    @Override
    default Object getValue() {
        return (Function<Object,Function<Object,Function<Object,Object>>>) x1 -> x2 -> x3 -> apply3(x1,x2,x3);
    }

}
