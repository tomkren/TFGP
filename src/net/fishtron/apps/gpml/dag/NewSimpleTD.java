package net.fishtron.apps.gpml.dag;

import net.fishtron.apps.gpml.MyList;
import net.fishtron.eval.Value;

import java.util.function.Function;

/** TypedDag helper interfaces for EvalLib construction
 * Created by tom on 15. 2. 2017. */

class NewSimpleTD {

    public interface Op extends Value {
        Object apply(NewSimpleTypedDag dag);
        @Override
        default Object getValue() {
            Function<Object,Object> fun = (d ->
                apply((NewSimpleTypedDag)d)
            );
            return fun;
        }
    }

    public interface Op2 extends Value {
        Object apply(NewSimpleTypedDag dag1, NewSimpleTypedDag dag2);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Object>> fun = (d1 -> (d2 ->
                apply((NewSimpleTypedDag)d1,(NewSimpleTypedDag)d2)
            ));
            return fun;
        }
    }

    public interface Op3 extends Value {
        Object apply(NewSimpleTypedDag dag1, NewSimpleTypedDag dag2, NewSimpleTypedDag dag3);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Function<Object,Object>>> fun = (d1 -> (d2 -> (d3 ->
                apply((NewSimpleTypedDag)d1,(NewSimpleTypedDag)d2,(NewSimpleTypedDag)d3)
            )));
            return fun;
        }
    }

    public interface OL extends Value {
        Object apply(Object obj, MyList myList);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Object>> fun = (o -> (xs ->
                    apply(o,(MyList)xs)
            ));
            return fun;
        }
    }

    public interface DL extends Value {
        Object apply(NewSimpleTypedDag dag, MyList myList);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Object>> fun = (d -> (xs ->
                    apply((NewSimpleTypedDag)d,(MyList)xs)
            ));
            return fun;
        }
    }

    public interface DLD extends Value {
        Object apply(NewSimpleTypedDag dag1, MyList myList, NewSimpleTypedDag dag2);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Function<Object,Object>>> fun = (d1 -> (xs -> (d2 ->
                    apply((NewSimpleTypedDag)d1,(MyList) xs,(NewSimpleTypedDag)d2)
            )));
            return fun;
        }
    }

}
