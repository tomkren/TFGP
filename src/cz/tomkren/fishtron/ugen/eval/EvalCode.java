package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.ugen.trees.AppTree;
import java.util.function.Function;

/** Created by user on 14. 2. 2017. */

public interface EvalCode {
    Object evalCode(AppTree.Leaf leaf, Function<AppTree,Object> evalFun);
}
