package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.cache.Cache;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.fishtron.ugen.nf.NF;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.*;

/** Created by Tomáš Křen on 31. 1. 2017. */

public class Gen {

    private Opts opts;
    private Gamma gamma;
    private Random rand;
    private Cache cache;

    Gen(Gamma gamma, Random rand) {
        this(Opts.mkDefault(), gamma, rand);
    }

    public Gen(Opts opts, Gamma gamma, Random rand) {
        this.opts = opts;
        this.gamma = gamma;
        this.rand = rand;
        this.cache = new Cache(this);
    }

    AppTree genOne(int k, Type type) {
        return genOne(k,type, 0, true)._1();
    }

    private AB<AppTree,Integer> genOne(int k, Type rawType, int n, boolean isTopLevel) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}

        // normalization
        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        // ball selection
        BigInteger num = getNum(k, t_NF);
        if (F.isZero(num)) {return AB.mk(null,n);}
        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}

        // Compute normalized result
        AB<AppTree,Integer> res = (k == 1) ? genOne_sym(t_NF, n, ball) : genOne_app(k, t_NF, n, ball);

        //denormalize
        nf.denormalizeIf(res._1());
        return res;
    }

    private AB<AppTree,Integer> genOne_sym(Type t_NF, int n, BigInteger ball) {
        for (Ts1Res res : ts1(t_NF,n)) {
            if (F.isZero(ball)) {
                // todo logy
                AppTree symLeaf = AppTree.mk(res.getSym(), res.getSigma().apply(t_NF));
                return AB.mk(symLeaf, res.getNextVarId());
            }
            ball = ball.subtract(BigInteger.ONE);
        }
        throw new Error("Ball not exhausted (k=1), should be unreachable.");
    }

    private AB<AppTree,Integer> genOne_app(int k, Type t, int n, BigInteger ball) {
        AB<TypeVar,Integer> res_alpha = newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int     n1    = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (int i = 1; i < k; i++) {
            int j = k-i;

            for (SubsRes res_F : subs(i, t_F, n1)) {
                Type t_X = res_F.getSigma().apply(alpha);

                for (SubsRes res_X : subs(j, t_X, res_F.getNextVarId())) {
                    BigInteger num_FX  = res_F.getNum().multiply(res_X.getNum());

                    if (ball.compareTo(num_FX) < 0) {
                        return genOne_app_core(i, j, t, t_F, res_F.getSigma(), t_X, res_X.getSigma(), res_X.getNextVarId());
                    }

                    ball = ball.subtract(num_FX);
                }
            }
        }
        throw new Error("Ball not exhausted (k>1), should be unreachable.");
    }

    private AB<AppTree,Integer> genOne_app_core(int i, int j, Type t, Type t_F, Sub sigma_F, Type t_X, Sub sigma_X, int n3) {
        Type t_selected_F = sigma_F.apply(t_F);
        Type t_selected_X = sigma_X.apply(t_X);

        AB<Type,Set<Integer>> skolemizeRes_F = t_selected_F.skolemize();
        AB<Type,Set<Integer>> skolemizeRes_X = t_selected_X.skolemize();

        Type t_skolemized_F = skolemizeRes_F._1();
        Type t_skolemized_X = skolemizeRes_X._1();

        AB<AppTree,Integer> treeRes_F = genOne(i, t_skolemized_F, n3, false);
        AB<AppTree,Integer> treeRes_X = genOne(j, t_skolemized_X, /*n4*/ treeRes_F._2(), false);

        AppTree tree_F = treeRes_F._1();
        AppTree tree_X = treeRes_X._1();
        int n5 = treeRes_X._2();

        if (tree_F == null || tree_X == null) {throw new Error("Null subtrees, should be unreachable.");}

        Set<Integer> skolemizedVars_F = skolemizeRes_F._2();
        Set<Integer> skolemizedVars_X = skolemizeRes_X._2();

        tree_F.deskolemize(skolemizedVars_F);
        // todo to tu bylo ale nesouhlasim s tim, tak zatim zakomentováno, ale promyslet:
        // tree_X.deskolemize(skolemizedVars_F); // Může se tam dostat skolemizovaná z t_F, todo ještě dopromyslet
        tree_X.deskolemize(skolemizedVars_X);

        tree_F.applySub(sigma_X);

        Sub sigma_FX = Sub.dot(sigma_X, sigma_F); // #bejval-restrikt-pokus .restrict(t); -- tady nemusim, jen se aplikuje na t a zahodí
        AppTree tree_FX = AppTree.mk(tree_F, tree_X, sigma_FX.apply(t));

        // todo logy a test strictní well typovanosti

        return AB.mk(tree_FX,n5);
    }

    // je v tom trochu zmatek, v LSolver getNum dělal normalizaci, tady jí ale nedělá
    public BigInteger getNum(int k, Type t_NF) {
        if (opts.isCachingUsed()) {
            return cache.getNum(k, t_NF);
        } else {
            BigInteger sum = BigInteger.ZERO;
            for (SubsRes subsRes : subs_compute(k, t_NF, 0)) {
                sum = sum.add(subsRes.getNum());
            }
            return sum;
        }
    }

    public List<SubsRes> subs(int k, Type rawType, int n) {
        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        List<SubsRes> subs = opts.isCachingUsed() ? cache.subs(k, t_NF, n) : subs_compute(k, t_NF, n) ;

        return nf.denormalizeIf(subs, n);
    }

    public List<Ts1Res> ts1(Type t_NF, int n) {
        return opts.isCachingUsed() ? cache.ts1(t_NF, n) : ts1_compute(t_NF, n);
    }

    public List<Ts1Res> ts1_compute(Type t, int n) {
        return ts1_static(gamma, t, n);
    }

    public List<SubsRes> subs_compute(int k, Type t_NF, int n) {
        if (k < 1) {
            throw new Error("k must be > 0, it is " + k);
        } else if (k == 1) {
            return subs_1(t_NF, n);
        } else {
            List<SubsRes> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                List<SubsRes> res_ij = subs_ij(i, k - i, t_NF, n);
                ret.addAll(res_ij);
            }
            return pack(ret);
        }
    }

    private List<SubsRes> subs_1(Type t_NF, int nextVarId) {
        List<Ts1Res> ts1_results = ts1(t_NF, nextVarId);
        List<SubsRes> unpackedResults = F.map(ts1_results, Ts1Res::toSubsRes);
        return pack(unpackedResults);
    }



    public static List<Ts1Res> ts1_static(Gamma gamma, Type t, int nextVarId) {
        List<Ts1Res> ret = new ArrayList<>();

        for (AB<String,Type> p : gamma.getSymbols()) {
            String s = p._1();
            Type t_s = p._2();

            Fresh freshRes = new Fresh(t_s, t, nextVarId);
            Sub mu = Sub.mgu(t, freshRes.getFreshType());

            if (!mu.isFail()) {
                Sub sigma = mu.restrict(t);
                ret.add(new Ts1Res(s, sigma, freshRes.getNextVarId()));
            }
        }
        return ret;
    }

    private List<SubsRes> subs_ij(int i, int j, Type t, int n) {
        List<SubsRes> ret = new ArrayList<>();

        AB<TypeVar,Integer> res_alpha = newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int     n1    = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (SubsRes res_F : subs(i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);

            for (SubsRes res_X : subs(j, t_X, res_F.getNextVarId())) {
                BigInteger num_FX = res_F.getNum().multiply(res_X.getNum());
                Sub      sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                ret.add(new SubsRes(num_FX, sigma_FX, res_X.getNextVarId()));
            }
        }

        //zde neni potřeba packovat, packovat stačí subs_compute
        return ret; //pack(ret);
    }


    public static List<SubsRes> pack(List<SubsRes> subs) {
        Map<String,SubsRes> resultMap = new TreeMap<>();

        for (SubsRes res : subs) {

            String sigmaFingerprint = res.getSigma().toString();
            SubsRes val = resultMap.get(sigmaFingerprint);

            if (val == null) {
                resultMap.put(sigmaFingerprint, res);
            } else {
                BigInteger oldNum = val.getNum();
                val.setNum(oldNum.add(res.getNum()));

                int oldNextVarId = val.getNextVarId();

                // TODO dřív tu bylo todle, to sem ale zmenil na assert co to zabije když fejlne, ALE FAKT RADši PROMYSLET !!!

                val.setNextVarId( Math.max(oldNextVarId,val.getNextVarId()) ); // better safe than sorry, TODO už vim že nastává

                /*if (oldNextVarId != res.getNextVarId()) {

                    Log.it(sigmaFingerprint);
                    Log.it(oldNextVarId +" != "+ res.getNextVarId());

                    throw new Error("Assert failed in pack(): oldNextVarId != res.getNextVarId()");
                }*/

            }
        }
        return new ArrayList<>(resultMap.values());
    }


    public static AB<TypeVar,Integer> newVar(Type t, int n) {
        int n1 = t.getNextVarId(n);
        return AB.mk(new TypeVar(n1), n1+1);
    }

    private NF normalizeIf(Type t) {
        return new NF(opts.isNormalizationPerformed(), t);
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


    // -- OPTIONS -----------------------------------

    public static class Opts {
        private final boolean isCachingUsed;
        private final boolean isNormalizationPerformed;

        Opts(boolean isCachingUsed, boolean isNormalizationPerformed) {
            this.isCachingUsed = isCachingUsed;
            this.isNormalizationPerformed = isNormalizationPerformed;
        }

        boolean isCachingUsed() {return isCachingUsed;}
        public boolean isNormalizationPerformed() {return isNormalizationPerformed;}

        public static Opts mkDefault() {
            return new Opts(true,true);
        }
    }

}
