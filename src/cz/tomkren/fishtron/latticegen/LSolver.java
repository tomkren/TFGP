package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.types.*;
import cz.tomkren.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.*;
import java.util.*;
import java.util.function.*;


/** Created by tom on 18. 7. 2016.*/

class LSolver {

    private List<AB<String, Type>> gamma;
    private Random rand;
    private Opts opts;

    private Cache cache;

    /*private Map<String, TypeData> typeDataMap;
    private List<Sub> subsList;
    private Map<String, Integer> sub2id;*/

    LSolver(Opts opts, List<AB<String, Type>> gamma, Random rand) {
        this.opts = opts;
        this.gamma = gamma;
        this.rand = rand;
        this.cache = new Cache(this);

        /*typeDataMap = new HashMap<>();
        subsList = new ArrayList<>();
        sub2id = new HashMap<>();*/
    }


    // -- "open problem" methods ----------------

    // TODO decide
    private static int initNextVarDecision(Type t) {
        return t.getNextVarId(); // vs. return 0;
    }



    // -- generate one -------------------------------------

    AppTree genOne(int k, Type type) {
        int initNextVarId = initNextVarDecision(type);
        return genOne(k,type, true, initNextVarId)._1();
    }

    private AB<AppTree,Integer> genOne(int k, Type type, boolean isTopLevel, int nextVarId) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}


        // ball selection
        BigInteger num = getNum(k, type, nextVarId);
        if (F.isZero(num)) {return AB.mk(null,nextVarId);}
        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}

        // normalization
        boolean isNormalizationPerformed = opts.isNormalizationPerformed();

        NF nf      = normalizeIf(type);
        Type t     = nf.getTypeInNF();
        Sub fromNF = nf.getFromNF();

        // debug log
        JSONObject log = log_prelude(k, type, t, ball, num);

        // --- CASE-A (treeSize = 1) ---------------------------------
        if (k == 1) {

            for (AB<String,Type> p : gamma) {
                String s = p._1();
                Type t_s = p._2();

                AB<Type,Integer> t_s_p = fresh(t_s,t,nextVarId); // todo ještě promyslet kterou přesně variantu //t_s.freshenVars(nextVarId); //fresh(t_s, t);
                Type t_s_fresh     = t_s_p._1();
                int  t_s_nextVarId = t_s_p._2();

                Sub mu = Sub.mgu(t, t_s_fresh);

                if (!mu.isFail()) {

                    if (F.isZero(ball)) {

                        AppTree s_tree = AppTree.mk(s, mu.apply(t));

                        if (isNormalizationPerformed) {
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

            Type alpha = newVar(t);
            Type t_F = Types.mkFunType(alpha, t);

            List<ABC<BigInteger,Sub,Integer>> subs_F = subs_k(i, t_F,nextVarId);

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
                        X_tree.deskolemize(t_X_skolemized_p._2()); // TODO nemůže se tam dostat skolemizovaná z t_F ??? ??? ???

                        F_tree.applySub(s_X);

                        Sub s_FX = Sub.dot(s_X, s_F); // #bejval-restrikt-pokus .restrict(t); -- tady nemusim, jen se aplikuje na t a zahodí

                        AppTree FX_tree = AppTree.mk(F_tree, X_tree, s_FX.apply(t));

                        if (isNormalizationPerformed) {
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

    // -- num utils -----------------------------

    BigInteger getNum(int k, Type type, int nextVarId) {
        Type t = normalizeIf(type).getTypeInNF(); //opts.isNormalizationPerformed() ? normalize_with_toNF(type)._1() : type;
        return opts.isCachingUsed() ? getNum_caching(k, t, nextVarId) : getNum_noCaching(k, t, nextVarId);
    }

    private BigInteger getNum_caching(int k, Type t, int nextVarId) {
        SizeTypeData sizeTypeData = cache.getSizeTypeData(k, t, nextVarId);
        return sizeTypeData.computeNum();
    }

    private BigInteger getNum_noCaching(int k, Type t, int nextVarId) {

        List<ABC<BigInteger,Sub,Integer>> subs = subs_k_noCaching(k, t, nextVarId);

        BigInteger sum = BigInteger.ZERO;
        for (ABC<BigInteger,Sub,Integer> sub : subs) {
            sum = sum.add(sub._1());
        }
        return sum;
    }

    // -- CORE ---------------------------------------------

    List<ABC<BigInteger,Sub,Integer>> subs_k(int k, Type type, int nextVarId) {
        NF nf = normalizeIf(type);
        Type t = nf.getTypeInNF();
        List<ABC<BigInteger,Sub,Integer>> subs =
                opts.isCachingUsed() ?
                cache.subs_k_caching(k,t,nextVarId):
                subs_k_noCaching(k,t,nextVarId);
        return nf.denormalize(subs);
    }

    private List<ABC<BigInteger,Sub,Integer>> subs_ij(int i, int j, Type type, int nextVarId) {
        NF nf = normalizeIf(type);
        Type t = nf.getTypeInNF();
        List<ABC<BigInteger,Sub,Integer>> subs =
                core_ij(i, j, t, this::subs_k, BigInteger::multiply, LSolver::packSubs, nextVarId);
        return nf.denormalize(subs);
    }

    List<ABC<BigInteger,Sub,Integer>> subs_k_noCaching(int k, Type t, int nextVarId) {
        return core_k(k, t, subs_1(gamma), this::subs_ij, LSolver::packSubs, nextVarId);
    }


    // -- generate all -------------------------------------

    List<ABC<String,Sub,Integer>> ts_k(int k, Type type, int nextVarId) {
        NF nf = normalizeIf(type);
        List<ABC<String,Sub,Integer>> ts = core_k(k,nf.getTypeInNF(), ts_1(gamma), this::ts_ij, xs->xs,nextVarId);
        return nf.denormalize(ts);
    }

    private List<ABC<String,Sub,Integer>> ts_ij(int i, int j, Type t, int nextVarId) {
        NF nf = normalizeIf(t);
        List<ABC<String,Sub,Integer>> ts = core_ij(i,j,nf.getTypeInNF(), this::ts_k, LSolver::mkAppString, xs->xs, nextVarId);
        return nf.denormalize(ts);
    }


    // -- STATIC FUNS : core of the method -----------------------------------------------------

    static TriFun<Integer,Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_k(List<AB<String,Type>> gamma) {
        return (k,t,nextVarId) -> core_k(k, t, subs_1(gamma), subs_ij(gamma), LSolver::packSubs, nextVarId);
    }

    private static TetraFun<Integer,Integer,Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_ij(List<AB<String,Type>> gamma) {
        return (i,j,t,nextVarId) -> core_ij(i, j, t, subs_k(gamma), BigInteger::multiply, LSolver::packSubs, nextVarId);
    }

    static TriFun<Integer,Type,Integer,List<ABC<String,Sub,Integer>>> ts_k(List<AB<String,Type>> gamma) {
        return (k,t,nextVarId) -> core_k(k, t, ts_1(gamma), ts_ij(gamma), ts->ts, nextVarId);
    }

    private static TetraFun<Integer,Integer,Type,Integer,List<ABC<String,Sub,Integer>>> ts_ij(List<AB<String,Type>> gamma) {
        return (i,j,t,nextVarId) -> core_ij(i, j, t, ts_k(gamma), LSolver::mkAppString, ts->ts, nextVarId);
    }

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

        Type alpha = newVar(t);
        Type t_F = Types.mkFunType(alpha, t);

        for (ABC<A,Sub,Integer> p_F : fun_k.apply(i, t_F, nextVarId)) {
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

    private static BiFunction<Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_1(List<AB<String,Type>> gamma) {
        return (t,nextVarId) -> packSubs(F.map(ts_1(gamma,t,nextVarId), p -> ABC.mk(BigInteger.ONE,p._2(),p._3()) ));
    }

    private static BiFunction<Type,Integer,List<ABC<String,Sub,Integer>>> ts_1(List<AB<String,Type>> gamma) {
        return (t,nextVarId) -> ts_1(gamma,t,nextVarId);
    }

    private static List<ABC<String,Sub,Integer>> ts_1(List<AB<String,Type>> gamma, Type t, int nextVarId) {
        List<ABC<String,Sub,Integer>> ret = new ArrayList<>();

        for (AB<String,Type> p : gamma) {
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

    private static String mkAppString(String F, String X) {
        return "("+F+" "+X+")";
    }

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

    private static TypeVar newVar(Type t) {
        return new TypeVar(t.getNextVarId());
    }

    static AB<Type,Integer> fresh(Type typeToFresh, Type typeToAvoid, int nextVarId) {
        int startVarId = Math.max(typeToAvoid.getNextVarId(),nextVarId);
        return typeToFresh.freshenVars(startVarId);
    }

    private NF normalizeIf(Type t) {
        return new NF(opts.isNormalizationPerformed(), t);
    }


    // -- toString and Serialization to json ----------------------------------------------------------------

    private JSONObject toJson() {
        return F.obj(
                "gamma", gammaToJson(gamma),
                "cache", cache.toJson()
                //"types", typesToJson(cache.getTypeDataMap()),
                //"subs",  subsToJson(cache.getSubsList())
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

    private static JSONArray gammaToJson(List<AB<String,Type>> gamma) {
        return F.jsonMap(gamma, p -> F.arr(p._1(),p._2().toString()));
    }

    private static JSONObject subsToJson_debugVersion(List<Sub> subsList) {
        JSONObject ret = new JSONObject();
        for (int i = 0; i < subsList.size(); i++) {
            ret.put(Integer.toString(i), subsList.get(i).toJson());
        }
        return ret;
    }

    // -- general utils, move out asi -----------------------------------

    static List<AB<String,Type>> mkGamma(String... strs) {
        if (strs.length % 2 != 0) {throw new Error("There must be an even number of gamma strings.");}
        List<AB<String,Type>> ret = new ArrayList<>(strs.length/2);
        for (int i = 0; i < strs.length; i+=2) {
            ret.add(new AB<>(strs[i],Types.parse(strs[i+1])));
        }
        return ret;
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
            return new Opts(false,true);
        }
    }

}
