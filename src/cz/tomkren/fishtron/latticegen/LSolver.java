package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.types.*;
import cz.tomkren.utils.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/** Created by tom on 18. 7. 2016.*/

public class LSolver {

    private static List<AB<Sub,BigInteger>> subs_k(List<AB<String,Type>> gamma, int k, Type t) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return subs_1(gamma, t);
        } else {
            List<AB<Sub,BigInteger>> subs = new ArrayList<>();
            for (int i = 1; i < k; k++) {
                subs.addAll(subs_ij(gamma, i, k-i, t));
            }
            return packSubs(subs);
        }
    }

    private static List<AB<Sub,BigInteger>> subs_ij(List<AB<String,Type>> gamma, int i, int j, Type t) {
        List<AB<Sub,BigInteger>> subs = new ArrayList<>();

        Type alpha = newVar(t);
        Type t_F = Types.mkFunType(alpha, t);

        for (AB<Sub,BigInteger> p_F : subs_k(gamma, i, t_F)) {
            Sub        s_F = p_F._1();
            BigInteger n_F = p_F._2();

            Type t_X = s_F.apply(alpha);

            for (AB<Sub,BigInteger> p_X : subs_k(gamma, j, t_X)) {
                Sub        s_X = p_X._1();
                BigInteger n_X = p_X._2();

                Sub        s_FX = Sub.dot(s_X, s_F).restrict(t);
                BigInteger n_FX = n_X.multiply(n_F);

                subs.add(AB.mk(s_FX, n_FX));
            }
        }
        return packSubs(subs);
    }


    private static List<AB<Sub,BigInteger>> subs_1(List<AB<String,Type>> gamma, Type t) {
        return packSubs(F.map(ts_1(gamma, t), p -> AB.mk(p._2(), BigInteger.ONE)));
    }

    private static List<AB<Sub,BigInteger>> packSubs(List<AB<Sub,BigInteger>> subs) {
        Map<String,AB<Sub,BigInteger>> subsMap = new TreeMap<>();

        for (AB<Sub, BigInteger> p : subs) {
            Sub sub = p._1();
            BigInteger num = p._2();

            String key = sub.toString();
            AB<Sub,BigInteger> val = subsMap.get(key);

            if (val == null) {
                subsMap.put(key, p);
            } else {
                BigInteger oldNum = val._2();
                val.set_2(oldNum.add(num));
            }
        }
        return new ArrayList<>(subsMap.values());
    }


    private static List<AB<String,Sub>> ts_k(List<AB<String,Type>> gamma, int k, Type t) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return ts_1(gamma, t);
        } else {
            List<AB<String,Sub>> ts = new ArrayList<>();
            for (int i = 1; i < k; k++) {
                ts.addAll(ts_ij(gamma, i, k-i, t));
            }
            return ts;
        }
    }

    private static List<AB<String,Sub>> ts_ij(List<AB<String,Type>> gamma, int i, int j, Type t) {
        List<AB<String,Sub>> ts = new ArrayList<>();

        Type alpha = newVar(t);
        Type t_F = Types.mkFunType(alpha, t);

        for (AB<String,Sub> p_F : ts_k(gamma, i, t_F)) {
            String F = p_F._1();
            Sub  s_F = p_F._2();

            Type t_X = s_F.apply(alpha);

            for (AB<String,Sub> p_X : ts_k(gamma, j, t_X)) {
                String X = p_X._1();
                Sub  s_X = p_X._2();

                String FX = "("+F+" "+X+")";
                Sub  s_FX = Sub.dot(s_X, s_F).restrict(t);

                ts.add(AB.mk(FX, s_FX));
            }
        }
        return ts;
    }

    private static List<AB<String,Sub>> ts_1(List<AB<String,Type>> gamma, Type t) {
        List<AB<String,Sub>> ret = new ArrayList<>();

        for (AB<String,Type> p : gamma) {
            String s = p._1();
            Type t_s = p._2();

            Type t_s_fresh = fresh(t_s, t);
            Sub mu = Sub.mgu(t, t_s_fresh);

            if (!mu.isFail()) {
                ret.add(new AB<>(s, mu.restrict(t)));
            }
        }
        return ret;
    }

    private static TypeVar newVar(Type t) {
        return new TypeVar(t.getNextVarId());
    }

    private static Type fresh(Type typeToFresh, Type typeToAvoid) {
        int startVarId = typeToAvoid.getNextVarId();
        Sub old2fresh = new Sub();
        AB<Type,Integer> p = typeToFresh.freshenVars(startVarId, old2fresh);
        return p._1();
    }

    private static AB<Type,Sub> normalize(Type t) {
        Sub t2nf = new Sub();
        Type nf = t.freshenVars(0, t2nf)._1();
        Sub nf2t = t2nf.inverse();
        if (nf2t.isFail()) {throw new Error("Unable to construct inverse: "+nf2t.getFailMsg());}
        return new AB<>(nf,nf2t);
    }



    // -- TESTING -----------------------------------

    public static void main(String[] args) {
        Checker ch = new Checker();

        testNormalizations(ch);
        tests_ts1_subs1(ch);

        ch.results();
    }


    private static List<AB<String,Type>> mkGamma(String... strs) {
        if (strs.length % 2 != 0) {throw new Error("There must be an even number of gamma strings.");}
        List<AB<String,Type>> ret = new ArrayList<>(strs.length/2);
        for (int i = 0; i < strs.length; i+=2) {
            ret.add(new AB<>(strs[i],Types.parse(strs[i+1])));
        }
        return ret;
    }


    private static void tests_ts1_subs1(Checker ch) {

        Log.it("\n== ts_1 & subs_1 tests ===================================================\n");


        List<AB<String,Type>> gamma1 = mkGamma(
                "s", "(a -> (b -> c)) -> ((a -> b) -> (a -> c))",
                "s2","(x5 -> (x0 -> x1)) -> ((x5 -> x0) -> (x5 -> x1))",
                "s3","(y5 -> (x0 -> x1)) -> ((y5 -> x0) -> (y5 -> x1))",
                "k", "a -> (b -> a)",
                "k2","x1 -> (x0 -> x1)",
                "+", "Int -> (Int -> Int)",
                "42", "Int",
                "magicVal", "alpha"
        );


        testTs1(ch, "Int -> Int", gamma1);
        testTs1(ch, "x1 -> x0",   gamma1);


    }
    private static void testTs1(Checker ch, String tStr, List<AB<String,Type>> gamma) {
        testTs1(ch, Types.parse(tStr), gamma);
    }

    private static void testTs1(Checker ch, Type t, List<AB<String,Type>> gamma) {

        AB<Type,Sub> nf = normalize(t);
        Type t_nf = nf._1();
        Sub nf2t  = nf._2();

        Log.it();
        Log.it("-- LIB gamma -------------");
        Log.listLn(gamma);

        Log.it("-- GOAL TYPE t -----");
        Log.it("t: "+t);
        Log.it("t_nf: "+t_nf);
        Log.it("nf2t: "+nf2t+"\n");

        List<AB<String, Sub>> ts1_t = ts_1(gamma, t_nf);
        Log.it("-- ts_1(gamma, t_nf) ------------");
        Log.listLn(ts1_t);

        List<AB<Sub, BigInteger>> subs1_t = subs_1(gamma, t_nf);
        Log.it("-- subs_1(gamma, t_nf) ----------");
        Log.listLn(subs1_t);

        Log.it("-------------------------------------------------------");
    }


    private static void testNormalizations(Checker ch) {

        Log.it("\n== normalization tests ===================================================\n");

        Type t1 = Types.parse("(x111 -> (x11 -> x1)) -> ((x111 -> x11) -> (x111 -> x1))");
        Type t2 = Types.parse("(x0 -> (x11 -> x1)) -> ((x0 -> x11) -> (x0 -> x1))");
        Type t3 = Types.parse("(x2 -> (x1 -> x0)) -> ((x2 -> x1) -> (x2 -> x0))");
        Type t4 = Types.parse("(x2 -> (x0 -> x1)) -> ((x2 -> x0) -> (x2 -> x1))");

        ch.it(t1);
        ch.it(((TypeTerm)t1).fold(Object::toString, Object::toString) +"\n");

        checkNormalisation(ch, t1);
        checkNormalisation(ch, t2);
        checkNormalisation(ch, t3);
        checkNormalisation(ch, t4);

    }

    private static void checkNormalisation(Checker ch, Type t) {
        AB<Type,Sub> p = normalize(t);
        Type nf  = p._1();
        Sub nf2t = p._2();

        ch.it(nf2t.apply(nf),t.toString());
        ch.it(p);
        ch.it("");
    }
}
