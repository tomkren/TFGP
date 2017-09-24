package net.fishtron.eval;

import net.fishtron.trees.AppTree;

/**Created by tom on 13.03.2017.*/

public interface LazyFunObject {

    Object lazyApply(AppTree tree);

}
