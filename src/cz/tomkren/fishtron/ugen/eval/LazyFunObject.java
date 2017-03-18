package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.ugen.trees.AppTree;

/**Created by tom on 13.03.2017.*/

public interface LazyFunObject {

    Object lazyApply(AppTree tree);

}
