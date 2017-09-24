package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.latticegen.cache.Cache;
import net.fishtron.types.*;
import net.fishtron.utils.*;
import org.json.JSONObject;

import java.math.*;
import java.util.*;
import java.util.function.*;


/** Created by tom on 18. 7. 2016.*/

public class LSolver {

    private Opts opts;
    private Gamma gamma;
    private Random rand;
    private Cache cache;

    LSolver(Opts opts, Gamma gamma, Random rand) {
        this.opts = opts;
        this.gamma = gamma;
        this.rand = rand;
        this.cache = new Cache(this);
    }

    // -- generate one -------------------------------------

    AppTree genOne(int k, Type type) {
        return genOne(k,type, true, 0)._1();
    }

    private AB<AppTree,Integer> genOne(int k, Type type, boolean isTopLevel, int nextVarId) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}

        // ball selection
        BigInteger num = getNum(k, type, nextVarId);
        /* (31.1.17) beru zpět že by se mělo počítat z normalizovaného typu, protože getNum si to normalizuje
           tzn neplatí že: getNum by se mělo počítat z normalizovaného typu, jinak normalizujeme zbytečně*/
        if (F.isZero(num)) {return AB.mk(null,nextVarId);}
        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}

        NF nf      = normalizeIf(type);
        Type t     = nf.getTypeInNF();
        Sub fromNF = nf.getFromNF();

        // debug log
        JSONObject log = log_prelude(k, type, t, ball, num);

        // --- CASE-A (treeSize = 1) ---------------------------------
        if (k == 1) {

            for (AB<String,Type> p : gamma.getSymbols()) {
                String s = p._1();
                Type t_s = p._2();

                AB<Type,Integer> t_s_p = fresh(t_s,t,nextVarId); // todo ještě promyslet kterou přesně variantu //t_s.freshenVars(nextVarId); //fresh(t_s, t);
                Type t_s_fresh     = t_s_p._1();
                int  t_s_nextVarId = t_s_p._2();

                Sub mu = Sub.mgu(t, t_s_fresh);

                if (!mu.isFail()) {

                    if (F.isZero(ball)) {

                        AppTree s_tree = AppTree.mk(s, mu.apply(t));

                        if (opts.isNormalizationPerformed()) {
                            s_tree.applySub(fromNF);
                        }

                        log_leaf(log, s_tree, s, t_s, t_s_fresh, mu, t_s_nextVarId, fromNF);
                        return AB.mk(s_tree,t_s_nextVarId);
                    }

                    ball = ball.subtract(BigInteger.ONE);
                }
            }

            throw new Error("Ball not exhausted (k=1), should be unreachable.");
        }

        // -- CASE-B (treeSize > 1) ------------------------------------------------
        for (int i = 1; i < k; i++) {
            int j = k-i;

            AB<TypeVar,Integer> p_alpha = newVar(t, nextVarId);
            TypeVar alpha = p_alpha._1();
            int nextVarId_alpha = p_alpha._2();

            Type t_F = Types.mkFunType(alpha, t);

            List<ABC<BigInteger,Sub,Integer>> subs_F = subs_k(i, t_F, nextVarId_alpha);

            for (ABC<BigInteger,Sub,Integer> p_F : subs_F) {
                BigInteger  n_F = p_F._1();
                Sub         s_F = p_F._2();
                int nextVarId_F = p_F._3();

                Type t_X = s_F.apply(alpha);

                List<ABC<BigInteger,Sub,Integer>> subs_X = subs_k(j, t_X, nextVarId_F);

                for (ABC<BigInteger,Sub,Integer> p_X : subs_X) {
                    BigInteger  n_X = p_X._1();
                    Sub         s_X = p_X._2();
                    int nextVarId_X = p_X._3();

                    BigInteger n_FX = n_F.multiply(n_X);

                    if (ball.compareTo(n_FX) < 0) {

                        Type t_F_selected = s_F.apply(t_F);
                        Type t_X_selected = s_X.apply(t_X);

                        AB<Type,Set<Integer>> t_F_skolemized_p = t_F_selected.skolemize();
                        AB<Type,Set<Integer>> t_X_skolemized_p = t_X_selected.skolemize();

                        Type t_F_skolemized = t_F_skolemized_p._1();
                        Type t_X_skolemized = t_X_skolemized_p._1();

                        AB<AppTree,Integer> F_tree_p = genOne(i, t_F_skolemized, false, nextVarId_X);
                        AB<AppTree,Integer> X_tree_p = genOne(j, t_X_skolemized, false, F_tree_p._2());

                        AppTree F_tree = F_tree_p._1();
                        AppTree X_tree = X_tree_p._1();

                        if (F_tree == null || X_tree == null) {throw new Error("Null subtrees, should be unreachable.");}

                        F_tree.deskolemize(t_F_skolemized_p._2());
                        X_tree.deskolemize(t_F_skolemized_p._2()); // Může se tam dostat skolemizovaná z t_F, todo ještě dopromyslet
                        X_tree.deskolemize(t_X_skolemized_p._2());

                        F_tree.applySub(s_X);

                        Sub s_FX = Sub.dot(s_X, s_F); // #bejval-restrikt-pokus .restrict(t); -- tady nemusim, jen se aplikuje na t a zahodí

                        AppTree FX_tree = AppTree.mk(F_tree, X_tree, s_FX.apply(t));

                        if (opts.isNormalizationPerformed()) {
                            FX_tree.applySub(fromNF);
                        }

                        log_app(log,FX_tree,i,j,alpha,t_F,s_F,t_F_selected,t_F_skolemized,t_X,s_X,t_X_selected,t_X_skolemized,fromNF);

                        if (isTopLevel && !FX_tree.isStrictlyWellTyped(gamma)) {
                            JSONObject trace = FX_tree.getTypeTrace();
                            Log.it("!!! TREE IS NOT SWT: "+trace);
                            AppTree.writeErrorTreeToFile(trace);
                            throw new Error("TREE IS NOT SWT!");
                        }

                        return AB.mk(FX_tree,X_tree_p._2());
                    }

                    ball = ball.subtract(n_FX);
                }
            }
        }

        throw new Error("Ball not exhausted (k>1), should be unreachable.");
    }


    private JSONObject log_prelude(int k, Type inputType, Type normalizedType, BigInteger ball, BigInteger num) {
        JSONObject log = new JSONObject();
        log.put("k",k);
        log.put("input type",inputType.toJson());
        log.put("ball",ball+"/"+num);

        if (opts.isNormalizationPerformed()) {
            log.put("normalized type", normalizedType.toJson());
        }

        return log;
    }

    private void log_leaf(
            JSONObject log, AppTree s_tree,
            String s, Type t_s, Type t_s_fresh, Sub mu, int t_s_nextVarId,
            Sub fromNF
    ) {
        log.put("s",s);
        log.put("t_s",t_s.toJson());
        log.put("t_s_fresh",t_s_fresh.toJson());
        log.put("mu",mu.toJson());
        log.put("t_s_nextVarId",t_s_nextVarId);

        if (opts.isNormalizationPerformed()) {
            log.put("fromNF",fromNF.toJson());
        }

        s_tree.updateDebugInfo(info -> info.put("log",log));
    }

    private void log_app(
            JSONObject log, AppTree FX_tree,
            int i, int j, Type alpha,
            Type t_F, Sub s_F, Type t_F_selected, Type t_F_skolemized,
            Type t_X, Sub s_X, Type t_X_selected, Type t_X_skolemized,
            Sub fromNF
    ) {
        log.put("i,j",           i+","+j);
        log.put("alpha",         alpha.toJson());
        log.put("t_F",           t_F.toJson());
        log.put("s_F",           s_F.toJson());
        log.put("t_F_selected",  t_F_selected.toJson());
        log.put("t_F_skolemized",t_F_skolemized.toJson());
        log.put("t_X",           t_X.toJson());
        log.put("s_X",           s_X.toJson());
        log.put("t_X_selected",  t_X_selected.toJson());
        log.put("t_X_skolemized",t_X_skolemized.toJson());

        if (opts.isNormalizationPerformed()) {
            log.put("fromNF", fromNF.toJson());
        }

        FX_tree.updateDebugInfo(info -> info.put("log",log));
    }

    // -- NUM -----------------------------

    BigInteger getNum(int k, Type type, int nextVarId) {
        Type t = normalizeIf(type).getTypeInNF();
        return opts.isCachingUsed() ? getNum_caching(k, t, nextVarId) : getNum_noCaching(k, t, nextVarId);
    }

    private BigInteger getNum_caching(int k, Type t, int nextVarId) {
        return cache.computeNum(k, t, nextVarId);
    }

    private BigInteger getNum_noCaching(int k, Type t, int nextVarId) {

        List<ABC<BigInteger,Sub,Integer>> subs = subs_k_compute(k, t, nextVarId);

        BigInteger sum = BigInteger.ZERO;
        for (ABC<BigInteger,Sub,Integer> sub : subs) {
            sum = sum.add(sub._1());
        }
        return sum;
    }

    // -- SUBS ---------------------------------------------

    List<ABC<BigInteger,Sub,Integer>> subs_k(int k, Type type, int nextVarId) {
        NF nf = normalizeIf(type);
        Type t = nf.getTypeInNF();
        List<ABC<BigInteger,Sub,Integer>> subs =
                opts.isCachingUsed() ?
                cache.subs_k_caching(k,t,nextVarId):
                subs_k_compute(k,t,nextVarId);
        return nf.denormalize(subs);
    }

    public List<ABC<BigInteger,Sub,Integer>> subs_k_compute(int k, Type t, int nextVarId) {
        return core_k(k, t, subs_1(gamma), this::subs_ij, LSolver::packSubs, nextVarId);
    }

    private List<ABC<BigInteger,Sub,Integer>> subs_ij(int i, int j, Type type, int nextVarId) {
        NF nf = normalizeIf(type);
        Type t = nf.getTypeInNF();
        List<ABC<BigInteger,Sub,Integer>> subs =
                core_ij(i, j, t, this::subs_k, BigInteger::multiply, LSolver::packSubs, nextVarId);
        return nf.denormalize(subs);
    }


    // -- generate all -------------------------------------

    List<ABC<String,Sub,Integer>> ts_k(int k, Type type, int nextVarId) {
        NF nf = normalizeIf(type);
        Type t = nf.getTypeInNF();
        List<ABC<String,Sub,Integer>> ts = core_k(k,t, ts_1(gamma), this::ts_ij, xs->xs,nextVarId);
        return nf.denormalize(ts);
    }

    private List<ABC<String,Sub,Integer>> ts_ij(int i, int j, Type type, int nextVarId) {
        NF nf = normalizeIf(type);
        Type t = nf.getTypeInNF();
        List<ABC<String,Sub,Integer>> ts = core_ij(i,j,t, this::ts_k, LSolver::mkAppString, xs->xs, nextVarId);
        return nf.denormalize(ts);
    }


    // -- STATIC FUNS -----------------------------------------------------

    static TriFun<Integer,Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_k(Gamma gamma) {
        return (k,t,nextVarId) -> core_k(k, t, subs_1(gamma), subs_ij(gamma), LSolver::packSubs, nextVarId);
    }

    private static TetraFun<Integer,Integer,Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_ij(Gamma gamma) {
        return (i,j,t,nextVarId) -> core_ij(i, j, t, subs_k(gamma), BigInteger::multiply, LSolver::packSubs, nextVarId);
    }

    static TriFun<Integer,Type,Integer,List<ABC<String,Sub,Integer>>> ts_k(Gamma gamma) {
        return (k,t,nextVarId) -> core_k(k, t, ts_1(gamma), ts_ij(gamma), ts->ts, nextVarId);
    }

    private static TetraFun<Integer,Integer,Type,Integer,List<ABC<String,Sub,Integer>>> ts_ij(Gamma gamma) {
        return (i,j,t,nextVarId) -> core_ij(i, j, t, ts_k(gamma), LSolver::mkAppString, ts->ts, nextVarId);
    }

    // -- CORE ---------------------------------------------------------------------------

    private static <A> List<ABC<A,Sub,Integer>> core_k(
            int k, Type t,
            BiFunction<Type,Integer,List<ABC<A,Sub,Integer>>> fun_1,
            TetraFun<Integer, Integer, Type, Integer, List<ABC<A,Sub,Integer>>> fun_ij,
            Function<List<ABC<A,Sub,Integer>>,List<ABC<A,Sub,Integer>>> pack_fun,
            int nextVarId
    ) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return fun_1.apply(t,nextVarId);
        } else {
            List<ABC<A,Sub,Integer>> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                ret.addAll(fun_ij.apply(i, k - i, t, nextVarId));
            }
            return pack_fun.apply(ret);
        }
    }

    private static <A> List<ABC<A,Sub,Integer>> core_ij(
            int i, int j, Type t,
            TriFun<Integer,Type,Integer,List<ABC<A,Sub,Integer>>> fun_k,
            BiFunction<A,A,A> operation,
            Function<List<ABC<A,Sub,Integer>>,List<ABC<A,Sub,Integer>>> pack_fun,
            int nextVarId
    ) {

        List<ABC<A,Sub,Integer>> ret = new ArrayList<>();

        AB<TypeVar,Integer> p_alpha = newVar(t, nextVarId);
        TypeVar alpha = p_alpha._1();
        int nextVarId_alpha = p_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (ABC<A,Sub,Integer> p_F : fun_k.apply(i, t_F, nextVarId_alpha)) {
            A   a_F         = p_F._1();
            Sub s_F         = p_F._2();
            int nextVarId_F = p_F._3();

            Type t_X = s_F.apply(alpha);

            for (ABC<A,Sub,Integer> p_X : fun_k.apply(j, t_X, nextVarId_F)) {
                A   a_X         = p_X._1();
                Sub s_X         = p_X._2();
                int nextVarId_X = p_X._3();

                A   a_FX = operation.apply(a_F,a_X);
                Sub s_FX = Sub.dot(s_X, s_F);//TODO #restrikt-pokus .restrict(t);

                ret.add(ABC.mk(a_FX, s_FX, nextVarId_X));
            }
        }
        return pack_fun.apply(ret);
    }

    private static BiFunction<Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_1(Gamma gamma) {
        return (t,nextVarId) -> packSubs(F.map(ts_1(gamma,t,nextVarId), p -> ABC.mk(BigInteger.ONE,p._2(),p._3()) ));
    }

    private static BiFunction<Type,Integer,List<ABC<String,Sub,Integer>>> ts_1(Gamma gamma) {
        return (t,nextVarId) -> ts_1(gamma,t,nextVarId);
    }

    private static List<ABC<String,Sub,Integer>> ts_1(Gamma gamma, Type t, int nextVarId) {
        List<ABC<String,Sub,Integer>> ret = new ArrayList<>();

        for (AB<String,Type> p : gamma.getSymbols()) {
            String s = p._1();
            Type t_s = p._2();

            AB<Type,Integer> t_s_p = fresh(t_s,t,nextVarId); // todo ještě promyslet kterou přesně variantu //t_s.freshenVars(nextVarId); //fresh(t_s, t);
            Type t_s_fresh     = t_s_p._1();
            int  t_s_nextVarId = t_s_p._2();

            Sub mu = Sub.mgu(t, t_s_fresh);

            if (!mu.isFail()) {
                ret.add(ABC.mk(s, mu.restrict(t), t_s_nextVarId)); /*TODO #restrikt-pokus .restrict(t)*/
            }
        }
        return ret;
    }


    // -- CORE utils ----------------------------------------------------------------

    private static List<ABC<BigInteger,Sub,Integer>> packSubs(List<ABC<BigInteger,Sub,Integer>> subs) {
        Map<String,ABC<BigInteger,Sub,Integer>> subsMap = new TreeMap<>();

        for (ABC<BigInteger,Sub,Integer> p : subs) {
            BigInteger num = p._1();
            Sub sub = p._2();
            int nextVarId = p._3();

            String key = sub.toString();
            ABC<BigInteger,Sub,Integer> val = subsMap.get(key);

            if (val == null) {
                subsMap.put(key, p);
            } else {
                BigInteger oldNum = val._1();
                val.set_1(oldNum.add(num));

                int oldNextVarId = val._3();
                val.set_3( Math.max(oldNextVarId,nextVarId) ); // better safe than sorry, TODO promyslet, zda může někdy nastat nerovnost
            }
        }
        return new ArrayList<>(subsMap.values());
    }

    private static AB<TypeVar,Integer> newVar(Type t, int nextVarId) {
        int newVarId = t.getNextVarId(nextVarId);
        return AB.mk(new TypeVar(newVarId), newVarId+1);
    }

    static AB<Type,Integer> fresh(Type typeToFresh, Type typeToAvoid, int nextVarId) {
        int startVarId = typeToAvoid.getNextVarId(nextVarId);
        startVarId = typeToFresh.getNextVarId(startVarId); // přidáno po objevení chyby 24.1.17, todo předělat aby bylo přehledný jak na papíře
        return typeToFresh.freshenVars(startVarId);
    }

    private NF normalizeIf(Type t) {
        return new NF(opts.isNormalizationPerformed(), t);
    }

    private static String mkAppString(String F, String X) {
        return "("+F+" "+X+")";
    }

    // -- toString and Serialization to json ----------------------------------------------------------------

    private JSONObject toJson() {
        return F.obj(
                "gamma", gamma.toJson(),
                "cache", cache.toJson()
        );
    }

    @Override
    public String toString() {
        return F.prettyJson(toJson(),F.obj(
                "types", 2,
                "gamma", 1,
                "subs",  1
        ));
    }

    // -- OPTS -----------------------------------

    static class Opts {
        private final boolean isCachingUsed;
        private final boolean isNormalizationPerformed;

        Opts(boolean isCachingUsed, boolean isNormalizationPerformed) {
            this.isCachingUsed = isCachingUsed;
            this.isNormalizationPerformed = isNormalizationPerformed;
        }

        boolean isCachingUsed() {return isCachingUsed;}
        boolean isNormalizationPerformed() {return isNormalizationPerformed;}

        static Opts mkDefault() {
            return new Opts(true,true);
        }
    }

}
