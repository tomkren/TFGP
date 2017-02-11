package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.cache.Cache;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.PreSubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.fishtron.ugen.data.PreTs1Res;
import cz.tomkren.fishtron.ugen.nf.NF;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import cz.tomkren.utils.TODO;
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

    public AppTree genOne(int k, Type rawType) {
        return genOne(k, rawType, randomBall(k, rawType));
    }



    // --

    public BigInteger getBall(AppTree tree) {
        return getBall(tree, tree.getOriginalType(), 0)._1();
    }

    public AppTree genOne(int k, Type rawType, BigInteger ball) {
        if (ball == null) {return null;}
        return genOne(k, rawType, 0, ball)._1();
    }

    // --

    private AB<BigInteger,Integer> getBall(AppTree tree, Type rawType, int n) {

        NF nf = normalizeIf(rawType);
        //Type t_NF = nf.getTypeInNF();

        int k = tree.size();
        return (k == 1) ? getBall_sym(tree, nf, n) : getBall_app(tree, nf, n);
    }

    private AB<AppTree,Integer> genOne(int k, Type rawType, int n, BigInteger ball /*,boolean isTopLevel*/) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}

        // normalization
        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        // ball selection
        /*BigInteger num = getNum(k, t_NF);
        if (F.isZero(num)) {return AB.mk(null,n);}
        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}*/

        // Compute normalized result
        AB<AppTree,Integer> res = (k == 1) ? genOne_sym(t_NF, n, ball) : genOne_app(k, t_NF, n, ball);

        //denormalize
        nf.denormalizeIf(res._1());

        if (!Types.isSameType(res._1().getType(), rawType)) {
            throw new Error("Result tree hasn't the input rawType.");
        }

        return res;
    }

    // --

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

    private AB<BigInteger,Integer> getBall_sym(AppTree tree, NF nf, int n) {
        if (!(tree instanceof AppTree.Leaf)) {throw new Error("Input tree must be a Leaf.");}

        Type t_NF = nf.getTypeInNF();

        AppTree.Leaf leaf = (AppTree.Leaf) tree;
        String sym = leaf.getSym();
        BigInteger ball = BigInteger.ZERO;

        for (Ts1Res res : ts1(t_NF,n)) {
            if (res.getSym().equals(sym)) {
                return AB.mk(ball, res.getNextVarId());
            } else {
                ball = ball.add(BigInteger.ONE);
            }
        }
        throw new Error("The leaf symbol "+sym+" is not in the Gamma with compatible type.");
    }

    // --


    private  AB<BigInteger,Integer> getBall_app(AppTree tree, NF nf, int n) {
        if (!(tree instanceof AppTree.App)) {throw new Error("Input tree must be an App.");}

        Type t_NF = nf.getTypeInNF();

        int k = tree.size();
        AppTree.App FX = (AppTree.App) tree;
        AppTree F = FX.getFunTree();
        AppTree X = FX.getArgTree();

        int i_goal = F.size();
        int j_goal = X.size();

        //Type t_F_goal = nf.toNF(F.getType());
        //Type t_X_goal = nf.toNF(X.getType());

        BigInteger base = BigInteger.ZERO;

        // ---

        AB<TypeVar,Integer> res_alpha = newVar(t_NF, n);
        TypeVar alpha = res_alpha._1();
        int n1 = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t_NF);

        for (int i = 1; i < k; i++) {
            int j = k - i;

            for (SubsRes res_F : subs(i, t_F, n1)) {

                BigInteger num_F = res_F.getNum();
                Sub sigma_F = res_F.getSigma();
                int n2 = res_F.getNextVarId();

                Type t_X = sigma_F.apply(alpha);

                for (SubsRes res_X : subs(j, t_X, n2)) {

                    BigInteger num_X = res_X.getNum();
                    Sub sigma_X = res_X.getSigma();
                    int n3 = res_X.getNextVarId();

                    if (i == i_goal && j == j_goal) {

                        //Type t_F_sel = sigma_X.apply(sigma_F.apply(t_F));
                        //Type t_X_sel = sigma_X.apply(t_X);

                        Sub sigma_FX = Sub.dot(sigma_X, sigma_F);

                        Type XF_t_NF = sigma_FX.apply(t_NF);

                        //Type t_magic = nf.fromNF(XF_t_NF);
                        //Type t_roota = tree.getType();

                        Log.it(tree);
                        Log.it(" "+t_NF);
                        Log.it(" "+sigma_FX.restrict(t_NF));
                        Log.it(" "+tree.getOriginalType());
                        Log.it(" "+XF_t_NF);

                        if (Types.isSameType(XF_t_NF,tree.getOriginalType())) {

                            Type t_selected_F = sigma_F.apply(t_F);
                            AB<Type,Set<Integer>> skolemizeRes_F = t_selected_F.skolemize();
                            Type t_skolemized_F = skolemizeRes_F._1();
                            Set<Integer> skolemizedVars_F = skolemizeRes_F._2();
                            AB<BigInteger,Integer> ball_F_res = getBall(F, t_skolemized_F , n3); // genOne(i, t_skolemized_F, n3, ball_F);
                            BigInteger ball_F = ball_F_res._1();
                            int n4 = ball_F_res._2();

                            Type t_selected_X = sigma_X.apply(t_X);
                            AB<Type,Set<Integer>> skolemizeRes_X = t_selected_X.skolemize();
                            Type t_skolemized_X = skolemizeRes_X._1();
                            Set<Integer> skolemizedVars_X = skolemizeRes_X._2();
                            AB<BigInteger,Integer> ball_X_res = getBall(X, t_skolemized_X, n4); // genOne(j, t_skolemized_X, n4, ball_X);
                            BigInteger ball_X = ball_X_res._1();
                            int n5 = ball_X_res._2();

                            BigInteger ball = base.add(num_X.multiply(ball_F).add(ball_X));
                            return AB.mk(ball,n5);
                        }

                        /*if (Types.isRenamedType(t_F_sel,t_F_goal)) {
                            if (Types.isRenamedType(t_X_sel,t_X_goal)) {
                                AB<BigInteger,Integer> ball_F_res = getBall(F, n3);
                                BigInteger ball_F = ball_F_res._1();
                                int n4 = ball_F_res._2();
                                AB<BigInteger,Integer> ball_X_res = getBall(X, n4);
                                BigInteger ball_X = ball_X_res._1();
                                int n5 = ball_X_res._2();
                                BigInteger ball = base.add(num_X.multiply(ball_F).add(ball_X));
                                return AB.mk(ball,n5);
                            }
                        }*/
                    }

                    BigInteger num_FX  = num_F.multiply(num_X);
                    base = base.add(num_FX);
                }
            }
        }

        throw new Error("Tree not 'exhausted' (k>1), should be unreachable. Tree:"+tree);
    }

    private AB<AppTree,Integer> genOne_app(int k, Type t_NF, int n, BigInteger ball) {
        AB<TypeVar,Integer> res_alpha = newVar(t_NF, n);
        TypeVar alpha = res_alpha._1();
        int n1 = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t_NF);

        for (int i = 1; i < k; i++) {
            int j = k-i;

            for (SubsRes res_F : subs(i, t_F, n1)) {
                Type t_X = res_F.getSigma().apply(alpha);

                for (SubsRes res_X : subs(j, t_X, res_F.getNextVarId())) {
                    BigInteger num_FX  = res_F.getNum().multiply(res_X.getNum());

                    if (ball.compareTo(num_FX) < 0) {
                        return genOne_app_core(i, j, t_NF, t_F, res_F.getSigma(), t_X, res_X.getSigma(), res_X.getNextVarId(), ball, res_X.getNum());
                    } else {
                        ball = ball.subtract(num_FX);
                    }

                }
            }
        }
        throw new Error("Ball not exhausted (k>1), should be unreachable.");
    }

    private AB<AppTree,Integer> genOne_app_core(int i, int j, Type t_NF, Type t_F, Sub sigma_F, Type t_X, Sub sigma_X, int n3, BigInteger ball, BigInteger num_X) {
        Type t_selected_F = sigma_F.apply(t_F);
        Type t_selected_X = sigma_X.apply(t_X);

        AB<Type,Set<Integer>> skolemizeRes_F = t_selected_F.skolemize();
        AB<Type,Set<Integer>> skolemizeRes_X = t_selected_X.skolemize();

        Type t_skolemized_F = skolemizeRes_F._1();
        Type t_skolemized_X = skolemizeRes_X._1();

        BigInteger[] subBalls = ball.divideAndRemainder(num_X);
        BigInteger ball_F = subBalls[0];
        BigInteger ball_X = subBalls[1];

        AB<AppTree,Integer> treeRes_F = genOne(i, t_skolemized_F, n3, ball_F/*, false*/);
        AB<AppTree,Integer> treeRes_X = genOne(j, t_skolemized_X, /*n4*/ treeRes_F._2(), ball_X/*, false*/);

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
        AppTree tree_FX = AppTree.mk(tree_F, tree_X, sigma_FX.apply(t_NF));

        // todo logy a test strictní well typovanosti

        return AB.mk(tree_FX,n5);
    }


    private BigInteger randomBall(int k, Type rawType) {
        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        BigInteger num = getNum(k, t_NF);
        if (F.isZero(num)) {return null;}
        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}
        return ball;
    }

    // TODO nebezpečný aby verce, co předpokládá typ v NF byla public
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

    private List<Ts1Res> ts1(Type t_NF, int n) {
        return opts.isCachingUsed() ? cache.ts1(t_NF, n) : ts1_compute(t_NF, n);
    }


    public List<Ts1Res> ts1_compute(Type t, int n) {
        List<PreTs1Res> ts1results_unmoved = ts1_static(gamma, t, n);
        return Mover.movePreTs1Results(t, n, ts1results_unmoved);
    }

    public List<SubsRes> subs_compute(int k, Type t_NF, int n) {
        List<PreSubsRes> ret = new ArrayList<>();
        if (k < 1) {
            throw new Error("k must be > 0, it is " + k);
        } else if (k == 1) {
            ret = F.map(ts1(t_NF, n), Ts1Res::toPreSubsRes);
        } else {
            for (int i = 1; i < k; i++) {
                List<PreSubsRes> res_ij = subs_ij(i, k - i, t_NF, n);
                ret.addAll(res_ij);
            }
        }
        return pack(t_NF, n, ret);
    }


    public static List<PreTs1Res> ts1_static(Gamma gamma, Type t, int nextVarId) {
        List<PreTs1Res> ret = new ArrayList<>();

        for (AB<String,Type> p : gamma.getSymbols()) {
            String s = p._1();
            Type t_s = p._2();

            Fresh freshRes = new Fresh(t_s, t, nextVarId);
            Sub mu = Sub.mgu(t, freshRes.getFreshType());

            if (!mu.isFail()) {
                Sub sigma = mu.restrict(t);
                ret.add(new PreTs1Res(s, sigma));
            }
        }
        return ret;
    }

    private List<PreSubsRes> subs_ij(int i, int j, Type t, int n) {
        List<PreSubsRes> ret = new ArrayList<>();

        AB<TypeVar,Integer> res_alpha = newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int     n1    = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (SubsRes res_F : subs(i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);

            for (SubsRes res_X : subs(j, t_X, res_F.getNextVarId())) {
                BigInteger num_FX = res_F.getNum().multiply(res_X.getNum());
                Sub sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                ret.add(new PreSubsRes(num_FX, sigma_FX));
            }
        }

        //zde neni potřeba packovat, packovat stačí subs_compute
        return ret;
    }


    public static List<SubsRes> pack(Type t, int n, List<PreSubsRes> preSubsResults) {
        Map<String,SubsRes> resultMap = new TreeMap<>();

        List<SubsRes> subsResults = Mover.movePreSubsResults(t, n, preSubsResults);

        for (SubsRes res : subsResults) {

            String sigmaFingerprint = res.getSigma().toString();
            SubsRes val = resultMap.get(sigmaFingerprint);

            if (val == null) {
                resultMap.put(sigmaFingerprint, res);
            } else {
                BigInteger oldNum = val.getNum();
                val.setNum(oldNum.add(res.getNum()));

                // ASSERT o NVI:
                int oldNextVarId = val.getNextVarId();
                if (oldNextVarId != res.getNextVarId()) {
                    //Log.it(sigmaFingerprint);
                    //Log.it(oldNextVarId +" != "+ res.getNextVarId());
                    throw new Error("Assert failed in pack(): oldNextVarId != res.getNextVarId()");
                }
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


    // -- Stats -------------------------------------------------------

    public Cache getCache() {
        return cache;
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
