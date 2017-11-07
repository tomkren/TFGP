package net.todo.apps.workflows;

import net.fishtron.apps.gpml.MyList;
import net.fishtron.eval.Value;
import cz.tomkren.fishtron.workflows.TypedDag;

import java.util.function.Function;

/** TypedDag helper interfaces for EvalLib construction
 * Created by tom on 15. 2. 2017. */

class TD {

    public interface Op extends Value {
        Object apply(TypedDag dag);
        @Override
        default Object getValue() {
            Function<Object,Object> fun = (d ->
                apply((TypedDag)d)
            );
            return fun;
        }
    }

    public interface Op2 extends Value {
        Object apply(TypedDag dag1, TypedDag dag2);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Object>> fun = (d1 -> (d2 ->
                apply((TypedDag)d1,(TypedDag)d2)
            ));
            return fun;
        }
    }

    public interface Op3 extends Value {
        Object apply(TypedDag dag1, TypedDag dag2, TypedDag dag3);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Function<Object,Object>>> fun = (d1 -> (d2 -> (d3 ->
                apply((TypedDag)d1,(TypedDag)d2,(TypedDag)d3)
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
        Object apply(TypedDag dag, MyList myList);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Object>> fun = (d -> (xs ->
                    apply((TypedDag)d,(MyList)xs)
            ));
            return fun;
        }
    }

    public interface DLD extends Value {
        Object apply(TypedDag dag1, MyList myList, TypedDag dag2);
        @Override
        default Object getValue() {
            Function<Object,Function<Object,Function<Object,Object>>> fun = (d1 -> (xs -> (d2 ->
                    apply((TypedDag)d1,(MyList) xs,(TypedDag)d2)
            )));
            return fun;
        }
    }

}
