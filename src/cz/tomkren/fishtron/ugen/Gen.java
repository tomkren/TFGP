package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import cz.tomkren.utils.TODO;

import java.math.BigInteger;
import java.util.*;

/** Created by Tomáš Křen on 31. 1. 2017. */

public class Gen {

    private Opts opts;
    private Gamma gamma;
    private Random rand;

    Gen(Opts opts, Gamma gamma, Random rand) {
        this.opts = opts;
        this.gamma = gamma;
        this.rand = rand;
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
        BigInteger num = getNum_noNF(k, t_NF); // TODO je v tom trochu zmatek, v LSOlveru getNum delal normalizaci, tady zatim předpokladame že ji nedělá
        if (F.isZero(num)) {return AB.mk(null,n);}
        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}


        // Compute normalized result
        AB<AppTree,Integer> res = (k == 1) ? genOne_1(t_NF, n, ball) : genOne_k(k, t_NF, n, ball);

        //denormalize
        denormalizeIf(res, nf);
        return res;
    }

    private AB<AppTree,Integer> genOne_1(Type t, int n, BigInteger ball) {
        for (Ts1Res res : ts_1(t,n)) {
            if (F.isZero(ball)) {
                // todo logy
                AppTree symLeaf = AppTree.mk(res.getSym(), res.getSigma().apply(t));
                return AB.mk(symLeaf, res.getNextVarId());
            }
            ball = ball.subtract(BigInteger.ONE);
        }
        throw new Error("Ball not exhausted (k=1), should be unreachable.");
    }

    private AB<AppTree,Integer> genOne_k(int k, Type t, int n, BigInteger ball) {
        AB<TypeVar,Integer> res_alpha = newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int     n1    = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (int i = 1; i < k; i++) {
            int j = k-i;

            for (SubsRes res_F : subs_k(i, t_F, n1)) {
                Type t_X = res_F.getSigma().apply(alpha);

                for (SubsRes res_X : subs_k(j, t_X, res_F.getNextVarId())) {
                    BigInteger num_FX  = res_F.getNum().multiply(res_X.getNum());

                    if (ball.compareTo(num_FX) < 0) {
                        return genOne_k_core(i, j, t, t_F, res_F.getSigma(), t_X, res_X.getSigma(), res_X.getNextVarId());
                    }

                    ball = ball.subtract(num_FX);
                }
            }
        }
        throw new Error("Ball not exhausted (k>1), should be unreachable.");
    }

    private AB<AppTree,Integer> genOne_k_core(int i, int j, Type t, Type t_F, Sub sigma_F, Type t_X, Sub sigma_X, int n3) {
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

    private BigInteger getNum_noNF(int k, Type type) {
        throw new TODO();
    }

    private List<SubsRes> subs_k(int k, Type rawType, int n) {
        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        throw new TODO();
    }

    private List<Ts1Res> ts_1(Type t, int n) {
        throw new TODO();
    }



    private static List<Ts1Res> ts_1(Gamma gamma, Type t, int nextVarId) {
        List<Ts1Res> ret = new ArrayList<>();

        for (AB<String,Type> p : gamma.getSymbols()) {
            String s = p._1();
            Type t_s = p._2();

            FreshRes freshRes = fresh(t_s, t, nextVarId);
            Sub mu = Sub.mgu(t, freshRes.getFreshType());

            if (!mu.isFail()) {
                Sub sigma = mu.restrict(t);
                ret.add(new Ts1Res(s, sigma, freshRes.getNextVarId()));
            }
        }
        return ret;
    }

    private static List<SubsRes> subs_1(Gamma gamma, Type t, int nextVarId) {
        List<Ts1Res> ts1_results = ts_1(gamma, t, nextVarId); // todo asi lepší předávat ts1_result pač ho chcem počítat jen jednou
        List<SubsRes> unpackedResults = F.map(ts1_results, Ts1Res::toSubsRes);
        return pack(unpackedResults);
    }

    private static List<SubsRes> subs_ij(Gamma gamma, int i, int j, Type t, int n) {
        List<SubsRes> ret = new ArrayList<>();

        AB<TypeVar,Integer> res_alpha = newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int     n1    = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (SubsRes res_F : subs_k(gamma, i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);

            for (SubsRes res_X : subs_k(gamma, j, t_X, res_F.getNextVarId())) {
                BigInteger num_FX = res_F.getNum().multiply(res_X.getNum());
                Sub      sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                ret.add(new SubsRes(num_FX, sigma_FX, res_X.getNextVarId()));
            }
        }

        //todo zde neni potřeba packovat, packovat stačí subs_k, rozmyslet co je efektivnější
        return ret; //pack(ret);
    }

    private static List<SubsRes> subs_k(Gamma gamma, int k, Type t, int nextVarId) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return subs_1(gamma, t, nextVarId);
        } else {
            List<SubsRes> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                ret.addAll(subs_ij(gamma, i, k - i, t, nextVarId));
            }
            return pack(ret);
        }
    }


    private static List<SubsRes> pack(List<SubsRes> subs) {
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
                // val.set_3( Math.max(oldNextVarId,nextVarId) ); // better safe than sorry, TODO promyslet, zda může někdy nastat nerovnost
                if (oldNextVarId != res.getNextVarId()) {
                    throw new Error("Assert failed in pack(): oldNextVarId != res.getNextVarId()");
                }

            }
        }
        return new ArrayList<>(resultMap.values());
    }


    private static AB<TypeVar,Integer> newVar(Type t, int n) {
        int n1 = t.getNextVarId(n);
        return AB.mk(new TypeVar(n1), n1+1);
    }

    static FreshRes fresh(Type typeToFresh, Type typeToAvoid, int n) {
        int n1 = typeToAvoid.getNextVarId(n);
        int n2 = typeToFresh.getNextVarId(n1);
        AB<Type,Integer> res = typeToFresh.freshenVars(n2);
        return new FreshRes(res._1(), res._2());
    }

    private NF normalizeIf(Type t) {
        return new NF(opts.isNormalizationPerformed(), t);
    }

    private void denormalizeIf(AB<AppTree,Integer> tree_res, NF nf) {
        if (opts.isNormalizationPerformed()) {
            AppTree tree = tree_res._1();
            Sub fromNF = nf.getFromNF();
            tree.applySub(fromNF);
        }
    }


    static class FreshRes {
        private final Type freshType;
        private final int nextVarId;

        FreshRes(Type freshType, int nextVarId) {
            this.freshType = freshType;
            this.nextVarId = nextVarId;
        }

        Type getFreshType() {return freshType;}
        int getNextVarId() {return nextVarId;}
    }

    static class Ts1Res {
        private final String s;
        private final Sub sigma;
        private final int nextVarId;

        Ts1Res(String s, Sub sigma, int nextVarId) {
            this.s = s;
            this.sigma = sigma;
            this.nextVarId = nextVarId;
        }

        String getSym() {return s;}
        Sub getSigma() {return sigma;}
        int getNextVarId() {return nextVarId;}

        SubsRes toSubsRes() {
            return new SubsRes(BigInteger.ONE, sigma, nextVarId);
        }
    }

    static class SubsRes {
        private BigInteger num;
        private final Sub sigma;
        private final int nextVarId;

        SubsRes(BigInteger num, Sub sigma, int nextVarId) {
            this.num = num;
            this.sigma = sigma;
            this.nextVarId = nextVarId;
        }

        public void setNum(BigInteger num) {this.num = num;}

        BigInteger getNum() {return num;}
        Sub getSigma() {return sigma;}
        int getNextVarId() {return nextVarId;}
    }

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
