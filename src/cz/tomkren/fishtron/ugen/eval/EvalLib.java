package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.ugen.AppTree;

import java.util.*;
import java.util.function.Function;

/** Created by tom on 14. 2. 2017. */

public class EvalLib {

    private Map<String,EvalCode> codes;

    public EvalLib(Object... args) {

        codes = new HashMap<>();

        if (args.length % 2 != 0) {
            throw new Error("There must be an even number of args.");
        }

        for (int i = 0; i<args.length; i+=2) {
            Object key = args[i];
            Object val = args[i+1];

            if (!(key instanceof String)) {
                throw new Error("Arg on index "+i+" is not a String.");
            }

            String sym = (String) key;
            EvalCode code = (val instanceof EvalCode) ? (EvalCode) val : () -> val;

            codes.put(sym, code);
        }
    }

    public Object eval(AppTree tree) {

        if (tree instanceof AppTree.Leaf) {
            return eval_leaf((AppTree.Leaf) tree);
        } else if (tree instanceof AppTree.App) {
            return eval_app((AppTree.App) tree);
        } else {
            throw new Error("Unsupported AppTree subclass: "+tree.getClass());
        }

    }

    private Object eval_leaf(AppTree.Leaf leaf) {
        String sym = leaf.getSym();
        EvalCode code = codes.get(sym);

        if (code == null) {
            throw new Error("Undefined symbol "+sym+".");
        }

        return code.getVal();
    }

    private Object eval_app(AppTree.App app) {

        AppTree funTree = app.getFunTree();
        AppTree argTree = app.getArgTree();

        Object funObject = eval(funTree);
        Object argObject = eval(argTree);

        Function<Object,Object> fun = ((Function<Object,Object>) funObject);

        return fun.apply(argObject);
    }
}
