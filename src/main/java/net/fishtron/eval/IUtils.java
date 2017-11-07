package net.fishtron.eval;

import java.util.function.Function;

/**Created by tom on 13.03.2017.*/

public class IUtils {

    public interface S extends Value {
        Function<Function<Object,Object>,Function<Object,Object>> apply(Function<Object,Function<Object,Object>> f);
        @Override
        default Object getValue() {
            return (Function<Function<Object,Function<Object,Object>>,Function<Function<Object,Object>,Function<Object,Object>>>) this::apply;
        }
    }

    public interface K extends Value {
        Function<Function<Object,Object>,Function<Object,Object>> apply(Object x);
        @Override
        default Object getValue() {
            return (Function<Function<Object,Function<Object,Object>>,Function<Function<Object,Object>,Function<Object,Object>>>) this::apply;
        }
    }

    public interface Bool1 extends Value {
        boolean apply(boolean x);
        @Override default Object getValue() {return (Function<Boolean,Boolean>) this::apply;}
    }

    public interface Bool2 extends Value {
        Function<Boolean,Boolean> apply(boolean x);
        @Override default Object getValue() {return (Function<Boolean,Function<Boolean,Boolean>>) this::apply;}
    }

    public interface Eq extends Value {
        Function<Object,Function<Object,Boolean>> apply(Object instanceEvidence);
        @Override default Object getValue() {return (Function<Object,Function<Object,Function<Object,Boolean>>>) this::apply;}
    }

    public interface IntOrd extends Value {
        Function<Integer,Function<Integer,Boolean>> apply(Object instanceEvidence);
        @Override default Object getValue() {return (Function<Object,Function<Integer,Function<Integer,Boolean>>>) this::apply;}
    }


}
