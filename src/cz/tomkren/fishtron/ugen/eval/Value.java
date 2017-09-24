package cz.tomkren.fishtron.ugen.eval;

import net.fishtron.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Leaf;

import java.util.function.Function;

/** Created by user on 15. 2. 2017. */

public interface Value extends EvalCode {

    Object getValue();

    @Override
    default Object evalCode(Leaf leaf, Function<AppTree,Object> eval) {
        return getValue();
    }
}
