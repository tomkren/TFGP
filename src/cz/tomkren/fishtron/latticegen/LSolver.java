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
    private Random rand;
    private Map<String,TypeData> typeDataMap;
    private List<Sub> subsList;
    private Map<String,Integer> sub2id;

    private LSolver(List<AB<String, Type>> gamma, Random rand) {
        this.gamma = gamma;
        this.rand = rand;
        typeDataMap = new HashMap<>();
        subsList = new ArrayList<>();
        sub2id = new HashMap<>();
    }



    // -- generate all -------------------------------------

    // TODO | Tato metoda by mohla být statická, nijak nevyužívá předpočítanou strukturu.
    // TODO | Q: Dá se pří téhle operaci nějak využít že máme předpočítaný substituce?
    // TODO | A: Substituce můžem použít, abychom nezkoušeli zbytečný uličky;
    // TODO |    a navíc si můžem v podobnym duchu předpočítat i termy.
    // TODO |    Zatimbych to ale přeskočil a radši udělal generateOne.
    private List<AB<String,Sub>> ts_k(int k, Type t) {
        AB<Type,Sub> nf = normalize(t);
        List<AB<String,Sub>> ts = core_k(k,nf._1(), ts_1(gamma), this::ts_ij, xs->xs);
        return denormalize(ts, nf);
    }

    private List<AB<String,Sub>> ts_ij(int i, int j, Type t) {
        AB<Type,Sub> nf = normalize(t);
        List<AB<String,Sub>> ts = core_ij(i,j,nf._1(), this::ts_k, LSolver::mkAppString, xs->xs);
        return denormalize(ts, nf);
    }


    // -- generate one -------------------------------------

    private AB<String,Sub> gen_k(int k, Type type) {
        AB<Type,Sub> nf = normalize(type);
        Type t = nf._1();

        SizeTypeData sizeTypeData = getSizeTypeData(k, nf);

        BigInteger num  = sizeTypeData.computeNum();
        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}

        for (int i = 1; i < k; i++) {
            int j = k-i;

            Type alpha = newVar(t);
            Type t_F = Types.mkFunType(alpha, t);

            for (AB<BigInteger,Sub> p_F : subs_k(i, t_F)) {
                BigInteger n_F = p_F._1();
                Sub        s_F = p_F._2();

                Type t_X = s_F.apply(alpha);

                for (AB<BigInteger,Sub> p_X : subs_k(j, t_X)) {
                    BigInteger n_X = p_X._1();
                    Sub        s_X = p_X._2();

                    BigInteger n_FX = n_F.multiply(n_X);

                    if (ball.compareTo(n_FX) < 0) {

                        AB<String,Sub> F_res = gen_k(i, t_F);
                        AB<String,Sub> X_res = gen_k(j, t_X);

                        String FX = mkAppString(F_res._1(),X_res._1());
                        Sub  s_FX = Sub.dot(s_X, s_F).restrict(t);

                        AB<String,Sub> FX_res = AB.mk(FX, s_FX);
                        return denormalize(FX_res, nf);
                    }

                    ball = ball.subtract(n_FX);
                }
            }
        }

        throw new Error("Ball not exhausted, should be unreachable.");
    }


    // -- CORE ---------------------------------------------

    private List<AB<BigInteger,Sub>> subs_k(int k, Type t) {
        AB<Type,Sub> nf = normalize(t);
        SizeTypeData sizeTypeData = getSizeTypeData(k, nf);

        /*if (!sizeTypeData.isComputed()) {
            List<AB<BigInteger,Sub>> subs = core_k(k, nf._1(), subs_1(gamma), this::subs_ij, LSolver::packSubs);
            sizeTypeData.setSubsData(encodeSubs(subs));
        }*/

        List<AB<BigInteger,Integer>> subsData = sizeTypeData.getSubsData();
        List<AB<BigInteger,Sub>> decodedSubs  = decodeSubs(subsData);
        return denormalize(decodedSubs, nf);
    }

    private SizeTypeData getSizeTypeData(int k, AB<Type,Sub> nfData) {
        Type t = nfData._1();
        TypeData typeData = typeDataMap.computeIfAbsent(t.toString(), key->new TypeData());
        SizeTypeData sizeTypeData = typeData.getSizeTypeData(k);

        if (!sizeTypeData.isComputed()) {
            List<AB<BigInteger,Sub>> subs = core_k(k, t, subs_1(gamma), this::subs_ij, LSolver::packSubs);
            sizeTypeData.setSubsData(encodeSubs(subs));
        }

        return sizeTypeData;
    }

    private List<AB<BigInteger,Sub>> subs_ij(int i, int j, Type t) {
        AB<Type,Sub> nf = normalize(t);
        List<AB<BigInteger,Sub>> subs = core_ij(i, j, nf._1(), this::subs_k, BigInteger::multiply, LSolver::packSubs);
        return denormalize(subs, nf);
    }


    // -- Utils for CORE ---------------------------------------------

    private AB<BigInteger,Integer> encodeSub(AB<BigInteger,Sub> subData) {
        Sub sub = subData._2();
        String sub_str = sub.toString();
        Integer sub_id = sub2id.get(sub_str);

        if (sub_id == null) {
            sub_id = subsList.size();
            subsList.add(sub);
            sub2id.put(sub_str, sub_id);
        }

        BigInteger num = subData._1();
        return AB.mk(num,sub_id);
    }

    private List<AB<BigInteger,Integer>> encodeSubs(List<AB<BigInteger,Sub>> subs) {
        return F.map(subs, this::encodeSub);
    }

    private List<AB<BigInteger,Sub>> decodeSubs(List<AB<BigInteger,Integer>> encodedSubs) {
        return F.map(encodedSubs, p -> AB.mk( p._1(), subsList.get(p._2()) ));
    }

    private static <A> AB<A,Sub> denormalize(AB<A,Sub> x, AB<Type,Sub> nfData) {
        Function<AB<A,Sub>,AB<A,Sub>> f = mkDenormalizator(nfData);
        return f.apply(x);
    }

    private static <A> List<AB<A,Sub>> denormalize(List<AB<A,Sub>> xs, AB<Type,Sub> nfData) {
        Function<AB<A,Sub>,AB<A,Sub>> f = mkDenormalizator(nfData);
        return F.map(xs, f);
    }

    private static <A> Function<AB<A,Sub>,AB<A,Sub>> mkDenormalizator(AB<Type,Sub> nfData) {
        Sub  t2nf = nfData._2();
        Sub  nf2t = t2nf.inverse();
        return p -> {
            A a = p._1();
            Sub sub_nf = p._2();
            Sub s1 = Sub.dot(sub_nf,t2nf);
            Sub sub = Sub.dot(nf2t, s1);
            return AB.mk(a,sub);
        };
    }






        // -- STATIC FUNS : core of the method -----------------------------------------------------

    private static BiFunction<Integer,Type,List<AB<BigInteger,Sub>>> subs_k(List<AB<String,Type>> gamma) {
        return (k,t) -> core_k(k,t, subs_1(gamma), subs_ij(gamma), LSolver::packSubs);
    }

    private static BiFunction<Integer,Type,List<AB<String,Sub>>> ts_k(List<AB<String,Type>> gamma) {
        return (k,t) -> core_k(k,t, ts_1(gamma), ts_ij(gamma), ts->ts);
    }

    private static TriFun<Integer,Integer,Type,List<AB<BigInteger,Sub>>> subs_ij(List<AB<String,Type>> gamma) {
        return (i,j,t) -> core_ij(i,j,t, subs_k(gamma), BigInteger::multiply, LSolver::packSubs);
    }

    private static TriFun<Integer,Integer,Type,List<AB<String,Sub>>> ts_ij(List<AB<String,Type>> gamma) {
        return (i,j,t) -> core_ij(i,j,t, ts_k(gamma), LSolver::mkAppString, ts->ts);
    }

    private static <A> List<AB<A,Sub>> core_k(int k, Type t,
            Function<Type,List<AB<A,Sub>>> fun_1,
            TriFun<Integer, Integer, Type, List<AB<A,Sub>>> fun_ij,
            Function<List<AB<A,Sub>>,List<AB<A,Sub>>> pack_fun) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return fun_1.apply(t);
        } else {
            List<AB<A,Sub>> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                ret.addAll(fun_ij.apply(i, k - i, t));
            }
            return pack_fun.apply(ret);
        }
    }

    private static <A> List<AB<A,Sub>> core_ij(int i, int j, Type t,
               BiFunction<Integer,Type,List<AB<A,Sub>>> fun_k,
               BiFunction<A,A,A> operation,
               Function<List<AB<A,Sub>>,List<AB<A,Sub>>> pack_fun) {

        List<AB<A,Sub>> ret = new ArrayList<>();

        Type alpha = newVar(t);
        Type t_F = Types.mkFunType(alpha, t);

        for (AB<A,Sub> p_F : fun_k.apply(i, t_F)) {
            A   a_F = p_F._1();
            Sub s_F = p_F._2();

            Type t_X = s_F.apply(alpha);

            for (AB<A,Sub> p_X : fun_k.apply(j, t_X)) {
                A   a_X = p_X._1();
                Sub s_X = p_X._2();

                A   a_FX = operation.apply(a_F,a_X);
                Sub s_FX = Sub.dot(s_X, s_F).restrict(t);

                ret.add(AB.mk(a_FX, s_FX));
            }
        }
        return pack_fun.apply(ret);
    }



    private static Function<Type,List<AB<BigInteger,Sub>>> subs_1(List<AB<String,Type>> gamma) {
        return t -> packSubs(F.map(ts_1(gamma, t), p -> AB.mk(BigInteger.ONE,p._2()) ));
    }

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


    // -- CORE utils ----------------------------------------------------------------

    private static String mkAppString(String F, String X) {
        return "("+F+" "+X+")";
    }

    private static List<AB<BigInteger,Sub>> packSubs(List<AB<BigInteger,Sub>> subs) {
        Map<String,AB<BigInteger,Sub>> subsMap = new TreeMap<>();

        for (AB<BigInteger,Sub> p : subs) {
            BigInteger num = p._1();
            Sub sub = p._2();

            String key = sub.toString();
            AB<BigInteger,Sub> val = subsMap.get(key);

            if (val == null) {
                subsMap.put(key, p);
            } else {
                BigInteger oldNum = val._1();
                val.set_1(oldNum.add(num));
            }
        }
        return new ArrayList<>(subsMap.values());
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

    private static JSONArray sizeTypeDataToJson(List<AB<BigInteger,Integer>> subs) {
        return F.jsonMap(subs, p -> F.arr(p._1().toString(),p._2()));
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

        //tests_lambdaDags(ch); // TODO vyřešit otázky nastolené tímto testem !

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

        List<AB<BigInteger,Sub>> subs = subs_k(gamma).apply(k, t_nf);
        Log.it("-- subs_"+k+"(gamma, t_nf) ----------");
        Log.listLn(subs);

        Log.it("Creating LSolver ... initial state:");
        LSolver solver = new LSolver(gamma, ch.getRandom());
        Log.it(solver);

        List<AB<BigInteger,Sub>> subs2 = solver.subs_k(k, t_nf);
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
