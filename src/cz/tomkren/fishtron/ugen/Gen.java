package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.cache.Cache;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.PreSubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.fishtron.ugen.data.TsRes;
import cz.tomkren.fishtron.ugen.data.PreTs1Res;
import cz.tomkren.fishtron.ugen.nf.NF;
import cz.tomkren.fishtron.ugen.tests.StaticGen;
import cz.tomkren.fishtron.ugen.trees.App;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Leaf;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import org.json.JSONObject;


import java.math.BigInteger;
import java.util.*;

/** Created by Tomáš Křen on 31. 1. 2017. */

public class Gen {

    private Opts opts;
    private Gamma gamma;
    private Checker ch;
    private Cache cache;

    public Gen(Gamma gamma, Checker ch) {
        this(Opts.mkDefault(), gamma, ch);
    }

    public Gen(Opts opts, Gamma gamma, Checker ch) {
        this.opts = opts;
        this.gamma = gamma;
        this.ch = ch;
        this.cache = new Cache(this);
    }

    public Checker getChecker() {return ch;}

    // -- GEN ONE -----------------------------------------------

    // "public random api"
    public AppTree genOne(int k, Type rawType) {
        return genOne(randomBall(k, rawType), k, rawType);
    }

    // "private random api"
    private AB<AppTree,Integer> genOne(int k, Type rawType, int n, int lvl) {
        BigInteger ball = randomBall(k, rawType);
        if (ball == null) {return AB.mk(null,n);}
        return genOne(ball, k, rawType, n, lvl);
    }

    // --

    // "public deterministic api"
    public AppTree genOne(BigInteger ball, int k, Type rawType) {
        if (ball == null) {return null;}
        return genOne(ball, k, rawType, 0, 0)._1();
    }

    // "private deterministic api"
    private AB<AppTree,Integer> genOne(BigInteger ball, int k, Type rawType, int n, int lvl) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}

        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        log_root(lvl, k, rawType, t_NF, ball);

        AB<AppTree,Integer> res = (k == 1) ? genOne_sym(ball, t_NF, n, lvl) : genOne_app(ball, k, t_NF, n, lvl);

        nf.denormalizeIf(res._1());

        // todo pozor spadne z nějakyho duvodu, když dám uplne nahoře typ "n", viz eval tester (imho nic vážnýho ale vyřešit)
        // TODO Taky už při dummy běhu evoluce jednou spadlo, ale zjistil jsem že to byly jen renamingy, tak jsem to zvolnil
        // TODO proto pořádně promyslet až bude čas jestli je to ok aby to přemenovalo ten typ...
        // bylo to swe seedem -8674298408011838851, ale těžko říct esli to do ty doby nerozvrtam že už to nereplikuje
        if (!Types.isRenamedType(res._1().getType(), rawType)) {

            ch.it("!!! VS:");
            ch.it(res._1().getType());
            ch.it(rawType);

            throw new Error("Result tree hasn't the input rawType.");
        }

        return res;
    }

    // "sym case"
    private AB<AppTree,Integer> genOne_sym(BigInteger ball, Type t_NF, int n, int lvl) {
        for (Ts1Res res : ts1(t_NF,n)) {
            if (F.isZero(ball)) {
                AppTree symLeaf = AppTree.mk(res.getSym(), res.getSigma().apply(t_NF));
                log_leaf(lvl, res, ball);
                return AB.mk(symLeaf, res.getNextVarId());
            }
            ball = ball.subtract(BigInteger.ONE);
        }
        throw new Error("Ball not exhausted (k=1), should be unreachable.");
    }

    // "app case"
    private AB<AppTree,Integer> genOne_app(BigInteger ball, int k, Type t_NF, int n, int lvl) {

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
                        return genOne_app_core(ball, i, j, t_NF, t_F, t_X, res_F, res_X, lvl);
                    }

                    ball = ball.subtract(num_FX);
                }
            }
        }
        throw new Error("Ball not exhausted (k>1), should be unreachable.");
    }

    // "core"
    private AB<AppTree,Integer> genOne_app_core(BigInteger ball, int i, int j, Type t_NF, Type t_F, Type t_X, SubsRes res_F, SubsRes res_X, int lvl) {

        BigInteger num_X = res_X.getNum();
        int n3 = res_X.getNextVarId();
        Sub sigma_F = res_F.getSigma();
        Sub sigma_X = res_X.getSigma();

        AB<Type,Set<Integer>> skol_F = selectAndSkolemize(sigma_F, t_F);
        AB<Type,Set<Integer>> skol_X = selectAndSkolemize(sigma_X, t_X);
        Type t_skol_F = skol_F._1();
        Type t_skol_X = skol_X._1();

        //todo vyřešit
        //assert_skolemizationNumTest(i, t_skol_F, res_F, t_F, "F");
        //assert_skolemizationNumTest(j, t_skol_X, res_X, t_X, "X");

        AB<AppTree,Integer> genRes_F, genRes_X;

        if (opts.computeSubBalls()) {
            BigInteger[] subBalls = ball.divideAndRemainder(num_X);
            BigInteger ball_F = subBalls[0];
            BigInteger ball_X = subBalls[1];

            genRes_F = genOne(ball_F, i, t_skol_F, n3, lvl+1);
            genRes_X = genOne(ball_X, j, t_skol_X, genRes_F._2() /*n4*/, lvl+1);
        } else {
            genRes_F = genOne(i, t_skol_F, n3, lvl+1);
            genRes_X = genOne(j, t_skol_X, genRes_F._2() /*n4*/, lvl+1);
        }

        AppTree tree_F = genRes_F._1();
        AppTree tree_X = genRes_X._1();
        int n5 = genRes_X._2();

        if (tree_F == null || tree_X == null) {throw new Error("Null subtrees, should be unreachable.");}

        tree_F.deskolemize(skol_F._2()); // ještě tu bylo, ale nesouhlasím s tím, tak zatim zakomentováno, todo ale promyslet: tree_X.deskolemize(skolemizedVars_F); // Může se tam dostat skolemizovaná z t_F, todo ještě dopromyslet
        tree_X.deskolemize(skol_X._2());

        tree_F.applySub(sigma_X);

        Sub sigma_FX = Sub.dot(sigma_X, sigma_F); // .restrict(t); nemusím, jen se aplikuje na t_NF a zahodí
        AppTree tree_FX = AppTree.mk(tree_F, tree_X, sigma_FX.apply(t_NF));

        return AB.mk(tree_FX,n5); // todo logy a test strictní well typovanosti
    }

    private static AB<Type,Set<Integer>> selectAndSkolemize(Sub sigma_A, Type t_A) {
        Type t_selected_A = sigma_A.apply(t_A);
        return t_selected_A.skolemize();
    }

    private void assert_skolemizationNumTest(int i, Type t_skol_A, SubsRes res_A, Type t_A, String caseName) {
        BigInteger num_A = res_A.getNum();
        BigInteger num_test_A = getNum(i, t_skol_A);
        if (!num_A.equals(num_test_A)) {
            String errMsg = "Skolemization num assert failed ("+caseName+"): "+num_test_A+" != "+num_A;

            Sub sigma_A = res_A.getSigma();
            Type t_selected_A = sigma_A.apply(t_A);

            ch.it("\n-- !!! ----------------------------------------");
            ch.it(errMsg);
            ch.it("t_"+caseName+" = "+ t_A);
            ch.it("sigma_"+caseName+" = "+ sigma_A);
            ch.it("t_selected_"+caseName+" = "+ t_selected_A +" = sigma_"+caseName+"(t_"+caseName+")");
            ch.it("t_skol_"+caseName+"     = "+ t_skol_A);
            ch.it();

            ch.it("res_"+caseName+" = "+ res_A);
            ch.it("res_"+caseName+".getNum() = "+ num_A);
            ch.it("getNum("+i+", t_skol_"+caseName+") = "+num_test_A);
            ch.it();

            List<TsRes> all_t_A_trees = StaticGen.ts(gamma, i, t_A, 0);
            ch.it("|all_t_"+caseName+"_trees| = "+ all_t_A_trees.size());
            List<TsRes> filteredTrees = F.filter(all_t_A_trees, res -> res.getSigma().equals(sigma_A));
            ch.it("|filtered_t_"+caseName+"_trees| = "+ filteredTrees.size());

            List<AppTree> trees_skol = F.map(StaticGen.ts(gamma, i, t_skol_A, 0), TsRes::getTree);
            ch.it("|trees_skol| = "+trees_skol.size());

            Map<String,AppTree> filteredTreesMap = new TreeMap<>();
            for (TsRes res : filteredTrees) {
                filteredTreesMap.put(res.getTree().toRawString(), res.getTree());
            }

            List<AppTree> problemTrees = F.filter(trees_skol,
                    t -> !filteredTreesMap.containsKey(t.toRawString()));

            ch.it("|problem_trees| = "+problemTrees.size());

            Map<String,TsRes> allTreesMap = new TreeMap<>();
            for (TsRes res : all_t_A_trees) {
                allTreesMap.put(res.getTree().toRawString(), res);
            }

            ch.it("Problem trees:");
            for (AppTree problemTree : problemTrees) {
                ch.it_noln("\t"+problemTree.toRawString());

                TsRes problemRes = allTreesMap.get(problemTree.toRawString());

                if (problemRes != null) {
                    ch.it_noln("\t ... but it is in all_t_"+caseName+"_trees :)");
                    ch.it(" ... with sigma = "+problemRes.getSigma());
                } else {
                    ch.it("\t ... and it is not even in all_t_"+caseName+"_trees :(");
                }
            }

            ch.it();
            try {Thread.sleep(500L);} catch (InterruptedException e) {e.printStackTrace();}

            throw new Error(errMsg);
        }
    }

    // -- LOGGING ----------------------------------------------------------------

    private void log_root(int lvl, int k, Type rawType, Type t_NF, BigInteger ball) {
        if (!opts.isLogging()) {return;}
        log(lvl, "ball", ball);
        log(lvl, "k", k);
        log(lvl, "rawType", rawType);
        log(lvl, "t_NF", t_NF);
    }

    private void log_leaf(int lvl, Ts1Res res, BigInteger ball) {
        if (!opts.isLogging()) {return;}
        log(lvl, "sym  ", res.getSym());
        log(lvl, "sigma", res.getSigma());
        log(lvl, "nvi  ", res.getNextVarId());
        log(lvl, "ball ", ball);
    }

    private void log(int lvl, String key, Object val) {
        String ods = F.fillStr(lvl, "  ");
        ch.it(ods+key+": "+val);
    }


    // ------------------------------------------------------------------------------

    private BigInteger randomBall(int k, Type rawType) {
        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        BigInteger num = getNum_NF(k, t_NF);
        if (F.isZero(num)) {return null;}
        BigInteger ball = F.nextBigInteger(num, ch.getRandom());
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}
        return ball;
    }

    public BigInteger getNum(int k, Type rawType) {
        NF nf = normalizeIf(rawType);
        return getNum_NF(k, nf.getTypeInNF());
    }

    private BigInteger getNum_NF(int k, Type t_NF) {
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



    // -- GET BALL -----------------------------------------------------------------------------------

    // "public api"
    public BigInteger getBall(AppTree tree) {
        return getBall(tree, tree.getOriginalType(), 0, 0)._1();
    }

    // "private api"
    private AB<BigInteger,Integer> getBall(AppTree tree, Type rawType, int n, int lvl) {
        int k = tree.size();

        NF nf = normalizeIf(rawType);
        Type t_NF = nf.getTypeInNF();

        log_root(lvl, k, rawType, t_NF, null);

        return (k == 1) ? getBall_sym(tree, t_NF, n, lvl) : getBall_app(tree, t_NF, n, lvl);
    }

    // "sym case"
    private AB<BigInteger,Integer> getBall_sym(AppTree tree, Type t_NF, int n, int lvl) {
        if (!(tree instanceof Leaf)) {throw new Error("Input tree must be a Leaf.");}

        Leaf leaf = (Leaf) tree;
        String sym = leaf.getSym();
        BigInteger ball = BigInteger.ZERO;

        for (Ts1Res res : ts1(t_NF,n)) {
            if (res.getSym().equals(sym)) {
                log_leaf(lvl, res, ball);
                return AB.mk(ball, res.getNextVarId());
            } else {
                ball = ball.add(BigInteger.ONE);
            }
        }
        throw new Error("The leaf symbol "+sym+" is not in the Gamma with compatible type.");
    }

    // "app case"
    private  AB<BigInteger,Integer> getBall_app(AppTree tree, Type t_NF, int n, int lvl) {
        if (!(tree instanceof App)) {throw new Error("Input tree must be an App.");}
        int k = tree.size();
        App FX = (App) tree;
        AppTree F = FX.getFunTree();
        AppTree X = FX.getArgTree();
        int i_goal = FX.getFunTree().size();
        int j_goal = FX.getArgTree().size();

        BigInteger base = BigInteger.ZERO;

        // ---

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

                    if (i == i_goal && j == j_goal) {

                        Sub sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma());

                        if (Types.isSameType( sigma_FX.apply(t_NF) , tree.getOriginalType()) ) {
                            return getBall_app_core(base, F, X, t_NF, t_F, t_X, res_F, res_X, lvl);
                        }
                    }

                    base = base.add(num_FX);
                }
            }
        }

        throw new Error("Tree not 'exhausted' (k>1), should be unreachable. Tree:"+tree);
    }

    // "core"
    private AB<BigInteger,Integer> getBall_app_core(BigInteger base, AppTree F, AppTree X, Type t_NF, Type t_F, Type t_X, SubsRes res_F, SubsRes res_X, int lvl) {

        BigInteger num_X = res_X.getNum();
        int n3 = res_X.getNextVarId();
        Sub sigma_F = res_F.getSigma();
        Sub sigma_X = res_X.getSigma();

        AB<Type,Set<Integer>> skol_F = selectAndSkolemize(sigma_F, t_F);
        AB<Type,Set<Integer>> skol_X = selectAndSkolemize(sigma_X, t_X);

        AB<BigInteger,Integer> ball_F_res = getBall(F, skol_F._1() , n3, lvl+1);
        BigInteger ball_F = ball_F_res._1();
        int n4 = ball_F_res._2();

        AB<BigInteger,Integer> ball_X_res = getBall(X, skol_X._1(), n4, lvl+1);
        BigInteger ball_X = ball_X_res._1();
        int n5 = ball_X_res._2();

        BigInteger ball = base.add(num_X.multiply(ball_F)).add(ball_X);
        return AB.mk(ball,n5);
    }


    // -- getters --------------------------------------------------------------------------------

    public Random getRand() {
        return ch.getRandom();
    }

    // -- STATS ----------------------------------------------------------------------------------

    public Cache getCache() {
        return cache;
    }

    // -- toString and Serialization to json -----------------------------------------------------

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

        public enum BallMode {GenerateSubBalls, ComputeSubBalls}

        private final boolean isCachingUsed;
        private final boolean isNormalizationPerformed;
        private final BallMode ballMode;
        private final boolean isLogging;


        Opts(boolean isCachingUsed, boolean isNormalizationPerformed, BallMode ballMode, boolean isLogging) {
            this.isCachingUsed = isCachingUsed;
            this.isNormalizationPerformed = isNormalizationPerformed;
            this.ballMode = ballMode;
            this.isLogging = isLogging;
        }

        boolean isCachingUsed() {return isCachingUsed;}
        public boolean isNormalizationPerformed() {return isNormalizationPerformed;}
        boolean isLogging() {return isLogging;}
        //boolean generateSubBalls() {return ballMode == BallMode.GenerateSubBalls;}
        boolean computeSubBalls() {return ballMode == BallMode.ComputeSubBalls;}


        public static Opts mkDefault(boolean isLogging) {
            return new Opts(true,true, BallMode.GenerateSubBalls, isLogging);
        }

        public static Opts mkDefault() {
            return mkDefault(false);
        }
    }

}
