package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.types.*;
import cz.tomkren.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.*;
import java.util.*;
import java.util.function.*;


/** Created by tom on 18. 7. 2016.*/

public class LSolver {

    private List<AB<String,Type>> gamma;

    private Map<String,TypeData> typeDataMap;

    private List<Sub> subsList;
    private Map<String,Integer> sub2id;


    private LSolver(List<AB<String, Type>> gamma) {
        this.gamma = gamma;
        typeDataMap = new HashMap<>();

        subsList = new ArrayList<>();
        sub2id = new HashMap<>();
    }


    // -- generate all -------------------------------------

    private List<AB<String,Sub>> ts_k(int k, Type t) {
        throw new TODO();
    }

    private static List<AB<String,Sub>> ts_ij(int i, int j, Type t) {




        throw new TODO();
    }



    // -- CORE ---------------------------------------------

    private List<AB<Sub,BigInteger>> subs_k(int k, Type t) {
        AB<Type,Sub> nf = normalize(t);
        SizeTypeData sizeTypeData = getSizeTypeData(k, nf);

        if (!sizeTypeData.isComputed()) {
            List<AB<Sub,BigInteger>> subs = subs_k(k, nf._1(), subs_1(gamma), this::subs_ij);
            sizeTypeData.setSubsData(encodeSubs(subs));
        }

        List<AB<Integer, BigInteger>> subsData = sizeTypeData.getSubsData();
        List<AB<Sub, BigInteger>> decodedSubs  = decodeSubs(subsData);
        return denormalizeSubs(decodedSubs, nf);
    }

    private List<AB<Sub,BigInteger>> subs_ij(int i, int j, Type t) {
        AB<Type,Sub> nfData = normalize(t);
        List<AB<Sub,BigInteger>> subs = subs_ij(i, j, nfData._1(), this::subs_k);
        return denormalizeSubs(subs, nfData);
    }


    // -- Utils for CORE ---------------------------------------------

    private SizeTypeData getSizeTypeData(int k, AB<Type,Sub> nfData) {
        String nf_str = nfData._1().toString();
        TypeData typeData = typeDataMap.computeIfAbsent(nf_str, key->new TypeData());
        return typeData.getSizeTypeData(k);
    }

    private AB<Integer,BigInteger> encodeSub(AB<Sub, BigInteger> subData) {
        Sub sub = subData._1();
        String sub_str = sub.toString();
        Integer sub_id = sub2id.get(sub_str);

        if (sub_id == null) {
            sub_id = subsList.size();
            subsList.add(sub);
            sub2id.put(sub_str, sub_id);
        }

        BigInteger num = subData._2();
        return AB.mk(sub_id,num);
    }

    private List<AB<Integer,BigInteger>> encodeSubs(List<AB<Sub, BigInteger>> subs) {
        return F.map(subs, this::encodeSub);
    }

    private List<AB<Sub,BigInteger>> decodeSubs(List<AB<Integer, BigInteger>> encodedSubs) {
        return F.map(encodedSubs, p -> AB.mk(subsList.get(p._1()), p._2()));
    }

    private List<AB<Sub,BigInteger>> denormalizeSubs(List<AB<Sub, BigInteger>> subs, AB<Type,Sub> nfData) {
        Sub  t2nf = nfData._2();
        Sub  nf2t = t2nf.inverse();
        return F.map(subs, p -> {
            Sub sub_nf = p._1();
            BigInteger num = p._2();
            Sub s1 = Sub.dot(sub_nf,t2nf);
            Sub sub = Sub.dot(nf2t, s1);
            return AB.mk(sub,num);
        });
    }



    // -- STATIC FUNS : core of the method -----------------------------------------------------

    private static BiFunction<Integer,Type,List<AB<Sub,BigInteger>>> subs_k(List<AB<String,Type>> gamma) {
        return (k,t) -> subs_k(k,t,subs_1(gamma), subs_ij(gamma));
    }

    private static TriFun<Integer,Integer,Type,List<AB<Sub,BigInteger>>> subs_ij(List<AB<String,Type>> gamma) {
        return (i,j,t) -> subs_ij(i,j,t,subs_k(gamma));
    }

    private static BiFunction<Integer,Type,List<AB<String,Sub>>> ts_k(List<AB<String,Type>> gamma) {
        return (k,t) -> ts_k(k,t,ts_1(gamma), ts_ij(gamma));
    }

    private static TriFun<Integer,Integer,Type,List<AB<String,Sub>>> ts_ij(List<AB<String,Type>> gamma) {
        return (i,j,t) -> ts_ij(i, j, t, ts_k(gamma));
    }

    private static List<AB<Sub,BigInteger>> subs_k(int k, Type t,
            Function<Type,List<AB<Sub,BigInteger>>> subs_1_fun,
            TriFun<Integer, Integer, Type, List<AB<Sub,BigInteger>>> subs_ij_fun) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return subs_1_fun.apply(t);
        } else {
            List<AB<Sub,BigInteger>> subs = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                subs.addAll(subs_ij_fun.apply(i, k - i, t));
            }
            return packSubs(subs);
        }
    }

    private static List<AB<String,Sub>> ts_k(int k, Type t,
            Function<Type,List<AB<String,Sub>>> ts_1_fun,
            TriFun<Integer,Integer,Type,List<AB<String,Sub>>> ts_ij_fun) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return ts_1_fun.apply(t);
        } else {
            List<AB<String,Sub>> ts = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                ts.addAll(ts_ij_fun.apply(i, k-i, t));
            }
            return ts;
        }
    }

    private static List<AB<Sub,BigInteger>> subs_ij(int i, int j, Type t,
            BiFunction<Integer,Type,List<AB<Sub,BigInteger>>> subs_k_fun) {

        List<AB<Sub,BigInteger>> subs = new ArrayList<>();

        Type alpha = newVar(t);
        Type t_F = Types.mkFunType(alpha, t);

        List<AB<Sub,BigInteger>> p_Fs = subs_k_fun.apply(i, t_F);
        for (AB<Sub,BigInteger> p_F : p_Fs) {
            Sub        s_F = p_F._1();
            BigInteger n_F = p_F._2();

            Type t_X = s_F.apply(alpha);

            List<AB<Sub,BigInteger>> p_Xs = subs_k_fun.apply(j, t_X);
            for (AB<Sub,BigInteger> p_X : p_Xs) {
                Sub        s_X = p_X._1();
                BigInteger n_X = p_X._2();

                Sub        s_FX = Sub.dot(s_X, s_F).restrict(t);
                BigInteger n_FX = n_X.multiply(n_F);

                subs.add(AB.mk(s_FX, n_FX));
            }
        }
        return packSubs(subs);
    }

    private static List<AB<String,Sub>> ts_ij(int i, int j, Type t,
            BiFunction<Integer,Type,List<AB<String,Sub>>> ts_k_fun) {

        List<AB<String,Sub>> ts = new ArrayList<>();

        Type alpha = newVar(t);
        Type t_F = Types.mkFunType(alpha, t);

        for (AB<String,Sub> p_F : ts_k_fun.apply(i, t_F)) {
            String F = p_F._1();
            Sub  s_F = p_F._2();

            Type t_X = s_F.apply(alpha);

            for (AB<String,Sub> p_X : ts_k_fun.apply(j, t_X)) {
                String X = p_X._1();
                Sub  s_X = p_X._2();

                String FX = "("+F+" "+X+")";
                Sub  s_FX = Sub.dot(s_X, s_F).restrict(t);

                ts.add(AB.mk(FX, s_FX));
            }
        }
        return ts;
    }


    private static Function<Type,List<AB<Sub,BigInteger>>> subs_1(List<AB<String,Type>> gamma) {
        return t -> packSubs(F.map(ts_1(gamma, t), p -> AB.mk(p._2(), BigInteger.ONE)));
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


    /*private static List<AB<String,Sub>> ts_k(List<AB<String,Type>> gamma, int k, Type t) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return ts_1(gamma, t);
        } else {
            List<AB<String,Sub>> ts = new ArrayList<>();
            for (int i = 1; i < k; i++) {
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
    }*/

    private static Function<Type,List<AB<String,Sub>>> ts_1(List<AB<String,Type>> gamma) {
        return t -> ts_1(gamma, t);
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

        Sub rho = t2nf.toRenaming(t);
        if (rho.isFail()) {throw new Error("Unable to construct renaming: "+rho.getFailMsg());}

        return new AB<>(nf,rho);
    }


    // -- toString and Serialization to json ----------------------------------------------------------------

    private JSONObject toJson() {
        return F.obj(
                "gamma", gammaToJson(gamma),
                "types", typesToJson(typeDataMap),
                "subs",  subsToJson(subsList)
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

    private static JSONObject typesToJson(Map<String,TypeData> typeDataMap) {
        return F.jsonMap(typeDataMap, LSolver::typeDataToJson);
    }

    private static JSONObject typeDataToJson(TypeData td) {
        return F.jsonMap(td.getSizeDataMap(), x -> sizeTypeDataToJson(x.getSubsData()) );
    }

    private static JSONArray sizeTypeDataToJson(List<AB<Integer, BigInteger>> subs) {
        return F.jsonMap(subs, p -> F.arr(p._1(),p._2().toString()));
    }


    private static JSONArray subsToJson(List<Sub> subsList) {
        return F.jsonMap(subsList, Sub::toJson);
    }

    private static JSONObject subsToJson_debugVersion(List<Sub> subsList) {
        JSONObject ret = new JSONObject();
        for (int i = 0; i < subsList.size(); i++) {
            ret.put(Integer.toString(i), subsList.get(i).toJson());
        }
        return ret;
    }

    // -- TESTING -----------------------------------

    public static void main(String[] args) {
        Checker ch = new Checker();


        testNormalizations(ch);
        tests_subs_1(ch);
        tests_subs_k(ch);

        tests_lambdaDags(ch);

        ch.results();
    }

    private static void tests_lambdaDags(Checker ch) {
        Log.it("\n== LAMBDA DAGS ===========================================================\n");

        List<AB<String,Type>> gamma = mkGamma(
            "s",     "(a -> (b -> c)) -> ((a -> b) -> (a -> c))",
            "k",     "a -> (b -> a)",
            "seri",  "(Dag a b) -> ((Dag b c) -> (Dag a c))",
            "para",  "(Dag a b) -> ((Dag c d) -> (Dag (P a c) (P b d))",
            "mkDag", "(a -> b) -> (Dag a b)",
            "deDag", "(Dag a b) -> (a -> b)",
            "mkP",   "a -> (b -> (P a b))",
            "fst",   "(P a b) -> a",
            "snd",   "(P a b) -> b"
        );

        Type t = Types.parse("(P a (P b c)) -> (P c (P b a))");

        test_ts_k(ch, 7, t, gamma);


    }

    private static void tests_subs_k(Checker ch) {
        Log.it("\n== ts_k & subs_k tests ===================================================\n");

        List<AB<String,Type>> gamma1 = mkGamma(
                "f", "X -> X",
                "seri", "(a -> b) -> ((b -> c) -> (a -> c))"
        );

        test_ts_k(ch, 1, "X -> X", gamma1);
        test_ts_k(ch, 2, "X -> X", gamma1);
        test_ts_k(ch, 3, "X -> X", gamma1);
    }

    private static void tests_subs_1(Checker ch) {
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

        test_ts_k(ch, 1, "Int -> Int", gamma1);
        test_ts_k(ch, 1, "x1 -> x0",   gamma1);
    }

    private static void test_ts_k(Checker ch, int k, String tStr, List<AB<String,Type>> gamma) {
        test_ts_k(ch, k, Types.parse(tStr), gamma);
    }

    private static void test_ts_k(Checker ch, int k, Type t, List<AB<String,Type>> gamma) {

        AB<Type,Sub> p_nf = normalize(t);
        Type t_nf = p_nf._1();
        Sub t2nf  = p_nf._2();

        Log.it();
        Log.it("-- LIB gamma -------------");
        Log.listLn(gamma);

        Log.it("-- GOAL TYPE t -----");
        Log.it("t: "+t);
        Log.it("t_nf: "+t_nf);
        Log.it("t2nf: "+t2nf);

        ch.it(t2nf.apply(t), t_nf.toString());
        Log.it();

        List<AB<String, Sub>> ts = ts_k(gamma).apply(k, t_nf);
        Log.it("-- ts_"+k+"(gamma, t_nf) ------------");
        Log.listLn(ts);

        List<AB<Sub, BigInteger>> subs = subs_k(gamma).apply(k, t_nf);
        Log.it("-- subs_"+k+"(gamma, t_nf) ----------");
        Log.listLn(subs);

        Log.it("Creating LSolver ... initial state:");
        LSolver solver = new LSolver(gamma);
        Log.it(solver);

        List<AB<Sub, BigInteger>> subs2 = solver.subs_k(k, t_nf);
        Log.it("-- LSolver.subs_"+k+"(gamma, t_nf) ----------");
        Log.listLn(subs);

        Log.it("... LSolver after subs_k call:");
        Log.it(solver);


        ch.list(subs2,subs);

        Log.it("-------------------------------------------------------");
    }

    private static List<AB<String,Type>> mkGamma(String... strs) {
        if (strs.length % 2 != 0) {throw new Error("There must be an even number of gamma strings.");}
        List<AB<String,Type>> ret = new ArrayList<>(strs.length/2);
        for (int i = 0; i < strs.length; i+=2) {
            ret.add(new AB<>(strs[i],Types.parse(strs[i+1])));
        }
        return ret;
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
        checkNormalisation(ch, "(x1 -> (x4 -> (x4 -> (x5 -> (x66 -> (x0 -> (x0 -> (x3 -> (x77 -> (x4 -> (x66 -> (x5 -> (x77 -> (x88 -> (x1 -> x2)))))))))))))))");
        checkNormalisation(ch, "(x10 -> (x0 -> (x4 -> (x55 -> (x4 -> (x55 -> (x0 -> (x33 -> (x8 -> (x7 -> (x6 -> (x5 -> (x7 -> (x8 -> (x6 -> x2)))))))))))))))");
    }

    private static void checkNormalisation(Checker ch, String tStr) {
        checkNormalisation(ch, Types.parse(tStr));
    }

    private static void checkNormalisation(Checker ch, Type t) {
        AB<Type,Sub> p = normalize(t);
        Type nf  = p._1();
        Sub t2nf = p._2();
        Sub nf2t = t2nf.inverse();

        ch.it(t);
        ch.it(nf);
        ch.it(t2nf);
        Log.it("----------------------------------");
        ch.it(t2nf.apply(t),nf.toString());
        ch.it(nf2t.apply(t2nf.apply(t)),t.toString());

        Log.it();
    }
}
