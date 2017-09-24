package cz.tomkren.fishtron.ugen.eval;

import net.fishtron.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Leaf;

import java.util.function.Function;

/** Created by user on 14. 2. 2017. */

public interface EvalCode {
    Object evalCode(Leaf leaf, Function<AppTree,Object> evalFun);
}
