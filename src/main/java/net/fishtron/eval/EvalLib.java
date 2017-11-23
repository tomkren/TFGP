package net.fishtron.eval;

import net.fishtron.trees.App;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Leaf;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONArray;

import java.util.*;
import java.util.function.Function;

/** Created by tom on 14. 2. 2017. */

public class EvalLib {

    private Map<String,EvalCode> codes;

    public EvalLib(List<AB<String,Object>> defs) {
        codes = new HashMap<>();
        for (AB<String,Object> def : defs) {
            String sym = def._1();
            Object val = def._2();
            EvalCode code = (val instanceof EvalCode) ? (EvalCode) val : (l,e,n) -> val;
            codes.put(sym, code);
        }
    }

    private EvalLib(EvalLib lib1, EvalLib lib2) {
        codes = new HashMap<>(lib1.size() + lib2.size());
        codes.putAll(lib1.codes);
        codes.putAll(lib2.codes);
    }

    private EvalLib(List<EvalLib> libs, boolean checkUniqueness /*todo*/) {
        int size = F.sumInt(F.map(libs, EvalLib::size));
        codes = new HashMap<>(size);
        for (EvalLib lib : libs) {
            codes.putAll(lib.codes);
        }
    }

    public static EvalLib union(EvalLib lib1, EvalLib lib2) {
        return new EvalLib(lib1, lib2);
    }

    public static EvalLib union(List<EvalLib> libs) {
        return new EvalLib(libs, true);
    }

    public int size() {
        return codes.size();
    }

    public boolean contains(String sym) {
        return codes.containsKey(sym);
    }

    public static EvalLib mk(JSONArray symbols, Function<String,Object> mkCodeFun) {
        List<AB<String,Object>> defs;

        defs = F.map(symbols, o -> {
            if (!(o instanceof String)) {throw new Error("symbols must be strings!");}
            String sym = (String) o;
            return AB.mk(sym, mkCodeFun.apply(sym));
        });

        return new EvalLib(defs);
    }

    public static EvalLib mk(Object... args) {
        return mk(Arrays.asList(args));
    }

    private static EvalLib mk(List<Object> args) {

        if (args.size() % 2 != 0) {
            throw new Error("There must be an even number of args.");
        }

        List<AB<String,Object>> defs = new ArrayList<>(args.size() / 2);

        for (int i = 0; i < args.size(); i+=2) {
            Object key = args.get(i);
            Object val = args.get(i+1);

            if (!(key instanceof String)) {throw new Error("Arg on index "+i+" is not a String.");}

            String sym = (String) key;
            defs.add(AB.mk(sym, val));
        }

        return new EvalLib(defs);
    }

    public Object eval(AppTree tree) {
        return eval_(tree, 0);
    }

    private Object eval_(AppTree tree, int numArgs) {

        if (tree instanceof Leaf) {
            return eval_leaf((Leaf) tree, numArgs);
        } else if (tree instanceof App) {
            return eval_app((App) tree, numArgs);
        } else {
            throw new Error("Unsupported AppTree implementation: "+tree.getClass());
        }

    }

    private Object eval_leaf(Leaf leaf, int numArgs) {
        String sym = leaf.getSym();
        EvalCode code = codes.get(sym);

        if (code == null) {
            throw new Error("Undefined symbol "+sym+".");
        }

        return code.evalCode(leaf, tree -> eval_(tree, 0), numArgs);
    }

    @SuppressWarnings("unchecked")
    private Object eval_app(App app, int numArgs) {

        AppTree funTree = app.getFunTree();
        AppTree argTree = app.getArgTree();

        Object funObject = eval_(funTree, numArgs+1);

        if (funObject instanceof LazyFunObject) {

            LazyFunObject lazyFunObject = (LazyFunObject) funObject;
            return lazyFunObject.lazyApply(argTree);

        } else {

            Object argObject = eval_(argTree, 0);
            Function<Object,Object> fun = (Function<Object,Object>) funObject;
            return fun.apply(argObject);

        }
    }

}
