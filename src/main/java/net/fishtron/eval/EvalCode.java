package net.fishtron.eval;

import net.fishtron.trees.AppTree;
import net.fishtron.trees.Leaf;
import net.fishtron.utils.F;

import java.util.function.Function;

/** Created by user on 14. 2. 2017. */

public interface EvalCode {
    Object evalCode(Leaf leaf, Function<AppTree,Object> evalFun, int numArgs);


    class ReflexiveJsonLeaf implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun, int numArgs) {
            return mkValue(numArgs, leaf.getSym());
        }
        private static Object mkValue(int numArgs, Object acc) {
            if (numArgs == 0) {
                return acc;
            } else {
                return (Function<Object,Object>) arg -> mkValue(numArgs-1, F.arr(acc, arg));
            }
        }
    }

    String DEFAULT_PARAM_NAME = "_";

    class ReflexiveJsonParam implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun, int numArgs) {
            return leaf.getParams().toJson().get(DEFAULT_PARAM_NAME);
        }
    }

}
