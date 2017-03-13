package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.ugen.AppTree;

import java.util.function.Function;

/** Created by user on 15. 2. 2017. */

public interface Value extends EvalCode {

    Object getValue();

    @Override
    default Object evalCode(AppTree.Leaf leaf, Function<AppTree,Object> eval) {
        return getValue();
    }
}
