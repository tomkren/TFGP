package net.fishtron.eval;

import net.fishtron.trees.AppTree;
import net.fishtron.trees.Leaf;
import net.fishtron.utils.Log;

import java.util.function.Function;

/**Created by tom on 13.03.2017.*/

public class If implements EvalCode {


    @Override
    public Object evalCode(Leaf leaf, Function<AppTree,Object> eval) {

        return (Function<Boolean,LazyFunObject>) p ->
            p ? (LazyFunObject) (a_tree -> (LazyFunObject) (b_tree -> eval.apply(a_tree)))
              : (LazyFunObject) (a_tree -> (LazyFunObject) (b_tree -> eval.apply(b_tree)));
    }



    // some testing ...
    public static void main(String[] args) {
        If if1 = new If();

        Object if1val = if1.evalCode(null, x-> {
            if(((Leaf)x).getSym().equals("ERROR")) {
                throw new Error("ERROR!!!1!");
            } else {
                return x;
            }
        });

        Function<Object,Object> if1fun = (Function<Object,Object>) if1val;

        Object if1true = if1fun.apply(true);
        Object if1false = if1fun.apply(false);

        AppTree a_tree = AppTree.mk("f",null);
        AppTree b_tree = AppTree.mk("g",null);
        AppTree error  = AppTree.mk("ERROR",null);


        if (if1true instanceof LazyFunObject && if1false instanceof LazyFunObject) {Log.it("Zdá se že to funguje (1).");} else {Log.it("Sakra! (1)");return;}

        LazyFunObject if1true_lazy  = (LazyFunObject) if1true;
        LazyFunObject if1false_lazy = (LazyFunObject) if1false;

        Object if1trueA  = if1true_lazy .lazyApply(a_tree);
        Object if1falseA = if1false_lazy.lazyApply(error);

        if (if1trueA instanceof LazyFunObject && if1falseA instanceof LazyFunObject) {Log.it("Zdá se že to funguje (2).");} else {Log.it("Sakra! (2)");return;}

        LazyFunObject if1trueA_lazy  = (LazyFunObject) if1trueA;
        LazyFunObject if1falseA_lazy = (LazyFunObject) if1falseA;

        Object res_f = if1trueA_lazy .lazyApply(error);
        Object res_g = if1falseA_lazy.lazyApply(b_tree);

        Log.it(res_f +" =?= "+ a_tree);
        Log.it(res_g +" =?= "+ b_tree);

    }
}
