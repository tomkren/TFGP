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

    public static void main(String[] args) {
        Checker ch = new Checker(7404398919224944163L);

        testNormalizations(ch);
        tests_subs_1(ch);
        tests_subs_k(ch);

        /* todo vyřešit: tests_lambdaDags(ch, 7, 10000);
        zkončilo (ale bez chyby) u 1600 s Exception java.lang.OutOfMemoryError: GC overhead limit exceeded

        */

        //tests_lambdaDags(ch, 6, 10000);

        tests_treeGenerating(ch, 6, 100);

        ch.results();
    }

    public static void main_(String[] args) {

        boolean wasOk = true;
        int seed = 210;
        while (wasOk) {
            wasOk = separateError_strictlyWellTyped(seed); // 13L: (snd (mkP snd (s k)) mkP)
            seed++;
            Log.it("----------------------------------");
        }
    }

    private static boolean separateError_strictlyWellTyped(int seed) {

        Checker ch = new Checker((long) seed);

        List<AB<String, Type>> gamma = mkGamma(
                "s", "(a -> (b -> c)) -> ((a -> b) -> (a -> c))",
                "k", "a -> (b -> a)",
                "seri", "(Dag a b) -> ((Dag b c) -> (Dag a c))",
                "para", "(Dag a b) -> ((Dag c d) -> (Dag (P a c) (P b d))",
                "mkDag", "(a -> b) -> (Dag a b)",
                "deDag", "(Dag a b) -> (a -> b)",
                "mkP", "a -> (b -> (P a b))",
                "fst", "(P a b) -> a",
                "snd", "(P a b) -> b"
        );

        Type t = Types.parse("(P A (P A A)) -> (P A (P A A))");

        int k = 6;

        LSolver s = new LSolver(gamma, ch.getRandom());

        boolean wasOk = true;

        AppTree newTree = s.genOne(k, t);
        if (newTree != null) {
            boolean isStrictlyWellTyped = newTree.isStrictlyWellTyped();

            ch.is(isStrictlyWellTyped, "Is tree strictly well typed?");

            Log.it(newTree.getTypeTrace());

            if (!isStrictlyWellTyped) {
                JSONObject typeTrace = newTree.getTypeTrace();
                Log.it("tree is not strictly well-typed: " + newTree + "\n" + typeTrace.toString());
                writeErrorTreeToFile(typeTrace);
                wasOk = false;
            }
        }

        if (!wasOk) {
            ch.results();
        }

        return wasOk;
    }

    private static void writeErrorTreeToFile(JSONObject typeTrace) {
        F.writeJsonAsJsFile("www/data/lastErrTree.js", "mkLastErrTree", typeTrace);
    }


    private List<AB<String, Type>> gamma;
    private Random rand;
    private Map<String, TypeData> typeDataMap;
    private List<Sub> subsList;
    private Map<String, Integer> sub2id;

    private LSolver(List<AB<String, Type>> gamma, Random rand) {
        this.gamma = gamma;
        this.rand = rand;
        typeDataMap = new HashMap<>();
        subsList = new ArrayList<>();
        sub2id = new HashMap<>();
    }


    // -- future public utils -----------------------------

    private BigInteger getNum(int k, Type t) {
        AB<Type, Sub> nf = normalize(t);
        SizeTypeData sizeTypeData = getSizeTypeData(k, nf, t.getNextVarId());  // TODO opravdu 0 ??????? ?????? ????? ???? ??? ?? ?
        return sizeTypeData.computeNum();
    }


    // -- generate one -------------------------------------

    private AppTree genOne(int k, Type type) {
        return genOne(k,type, true , true, type.getNextVarId())._1();
    }

    private AB<AppTree,Integer> genOne(int k, Type type, boolean isNormalizationPerformed, boolean isTopLevel, int nextVarId) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}

        JSONObject log = new JSONObject();

        log.put("k",k);
        log.put("input type",type.toJson());

        Type t;
        Sub fromNF;
        SizeTypeData sizeTypeData_forNum;


        if (isNormalizationPerformed) {
            AB<Type,Sub> nf = normalize(type);
            t = nf._1();
            fromNF = nf._2().inverse();
            sizeTypeData_forNum = getSizeTypeData(k, nf, nextVarId); // todo ??? nextVarId ????

            log.put("normalized type",t.toJson());

        } else {
            t = type;
            fromNF = null;
            sizeTypeData_forNum = getSizeTypeData(k, t, nextVarId); // todo ??? nextVarId ????
        }


        BigInteger num = sizeTypeData_forNum.computeNum();

        if (F.isZero(num)) {return AB.mk(null,nextVarId);}

        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}

        log.put("ball",ball+"/"+num);

        if (k == 1) {

            for (AB<String,Type> p : gamma) {
                String s = p._1();
                Type t_s = p._2();

                //Type t_s_fresh = fresh(t_s, t);
                AB<Type,Integer> t_s_p =  fresh(t_s,t,nextVarId); // todo ještě promyslet kterou přesně variantu //t_s.freshenVars(nextVarId); //fresh(t_s, t);
                Type t_s_fresh     = t_s_p._1();
                int  t_s_nextVarId = t_s_p._2();

                Sub mu = Sub.mgu(t, t_s_fresh);

                if (!mu.isFail()) {

                    if (F.isZero(ball)) {

                        log.put("s",s);
                        log.put("t_s",t_s.toJson());
                        log.put("t_s_fresh",t_s_fresh.toJson());
                        log.put("mu",mu.toJson());
                        log.put("t_s_nextVarId",t_s_nextVarId);

                        AppTree s_tree = AppTree.mk(s, mu.apply(t));

                        if (isNormalizationPerformed) {
                            s_tree.applySub(fromNF);
                            log.put("fromNF",fromNF.toJson());
                        }

                        s_tree.updateDebugInfo(info -> info.put("log",log));

                        return AB.mk(s_tree,t_s_nextVarId);
                    }

                    ball = ball.subtract(BigInteger.ONE);
                }
            }

            throw new Error("Ball not exhausted (k=1), should be unreachable.");
        }

        for (int i = 1; i < k; i++) {
            int j = k-i;

            Type alpha = newVar(t);
            Type t_F = Types.mkFunType(alpha, t);

            for (ABC<BigInteger,Sub,Integer> p_F : subs_k(i, t_F,nextVarId)) {
                BigInteger  n_F = p_F._1();
                Sub         s_F = p_F._2();
                int nextVarId_F = p_F._3();

                Type t_X = s_F.apply(alpha);

                for (ABC<BigInteger,Sub,Integer> p_X : subs_k(j, t_X, nextVarId_F)) {
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

                        AB<AppTree,Integer> F_tree_p = genOne(i, t_F_skolemized, isNormalizationPerformed, false, nextVarId_X);
                        AB<AppTree,Integer> X_tree_p = genOne(j, t_X_skolemized, isNormalizationPerformed, false, F_tree_p._2());

                        AppTree F_tree = F_tree_p._1();
                        AppTree X_tree = X_tree_p._1();

                        if (F_tree == null || X_tree == null) {throw new Error("Null subtrees, should be unreachable.");}

                        F_tree.deskolemize(t_F_skolemized_p._2());
                        X_tree.deskolemize(t_X_skolemized_p._2());

                        F_tree.applySub(s_X);

                        Sub s_FX = Sub.dot(s_X, s_F); //TODO #bejval-restrikt-pokus .restrict(t); -- tady nemusim, jen se aplikuje na t a zahodí

                        AppTree FX_tree = AppTree.mk(F_tree, X_tree, s_FX.apply(t));

                        if (isNormalizationPerformed) {
                            FX_tree.applySub(fromNF);
                            log.put("fromNF",fromNF.toJson());
                        }

                        FX_tree.updateDebugInfo(info -> info.put("log",log));

                        if (isTopLevel && !FX_tree.isStrictlyWellTyped()) {
                            JSONObject trace = FX_tree.getTypeTrace();
                            Log.it("!!! TREE IS NOT SWT: "+trace);
                            writeErrorTreeToFile(trace);
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


    // todo asi smazat !
    /*
    private AB<String,Sub> generateOne(int k, Type type) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}

        AB<Type,Sub> nf = normalize(type);
        Type t = nf._1();

        SizeTypeData sizeTypeData = getSizeTypeData(k, nf);

        BigInteger num = sizeTypeData.computeNum();

        if (F.isZero(num)) {return null;}

        BigInteger ball = F.nextBigInteger(num, rand);
        if (ball == null) {throw new Error("Ball null check failed, should be unreachable.");}

        if (k == 1) {

            for (AB<String,Type> p : gamma) {
                String s = p._1();
                Type t_s = p._2();

                Type t_s_fresh = fresh(t_s, t);
                Sub mu = Sub.mgu(t, t_s_fresh);

                if (!mu.isFail()) {

                    if (F.isZero(ball)) {
                        AB<String,Sub> res = AB.mk(s, mu.restrict(t));
                        return denormalize(res, nf);
                    }

                    ball = ball.subtract(BigInteger.ONE);
                }
            }

            throw new Error("Ball not exhausted (k=1), should be unreachable.");
        }

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

                        Type t_F_selected = s_F.apply(t_F);
                        Type t_X_selected = s_X.apply(t_X);

                        AB<String,Sub> F_res = generateOne(i, t_F_selected.skolemize());
                        AB<String,Sub> X_res = generateOne(j, t_X_selected.skolemize());

                        if (F_res == null || X_res == null) {throw new Error("Null subtrees, should be unreachable.");}

                        String FX = mkAppString(F_res._1(),X_res._1());
                        Sub  s_FX = Sub.dot(s_X, s_F).restrict(t);

                        AB<String,Sub> FX_res = AB.mk(FX, s_FX);
                        return denormalize(FX_res, nf);
                    }

                    ball = ball.subtract(n_FX);
                }
            }
        }

        throw new Error("Ball not exhausted (k>1), should be unreachable.");
    }*/



    // -- CORE ---------------------------------------------

    private List<ABC<BigInteger,Sub,Integer>> subs_k(int k, Type t, int nextVarId) {
        AB<Type,Sub> nf = normalize(t);
        SizeTypeData sizeTypeData = getSizeTypeData(k, nf, nextVarId);
        List<ABC<BigInteger,Sub,Integer>> decodedSubs = decodeSubs(sizeTypeData,nextVarId);
        return denormalize(decodedSubs, nf);
    }

    private SizeTypeData getSizeTypeData(int k, AB<Type,Sub> nfData, int nextVarId) {
        return getSizeTypeData(k, nfData._1(), nextVarId);
    }

    private SizeTypeData getSizeTypeData(int k, Type t, int nextVarId) {
        TypeData typeData = typeDataMap.computeIfAbsent(t.toString(), key->new TypeData());
        SizeTypeData sizeTypeData = typeData.getSizeTypeData(k);

        if (!sizeTypeData.isComputed()) {
            List<ABC<BigInteger,Sub,Integer>> subs = core_k(k, t, subs_1(gamma), this::subs_ij, LSolver::packSubs, nextVarId);
            sizeTypeData.set(encodeSubs(subs),nextVarId);
        }

        return sizeTypeData;
    }

    private List<ABC<BigInteger,Sub,Integer>> subs_ij(int i, int j, Type t, int nextVarId) {
        AB<Type,Sub> nf = normalize(t);
        List<ABC<BigInteger,Sub,Integer>> subs = core_ij(i, j, nf._1(), this::subs_k, BigInteger::multiply, LSolver::packSubs, nextVarId);
        return denormalize(subs, nf);
    }

    // -- generate all -------------------------------------

    // TODO | Tato metoda by mohla být statická, nijak nevyužívá předpočítanou strukturu.
    // TODO | Q: Dá se pří téhle operaci nějak využít že máme předpočítaný substituce?
    // TODO | A: Substituce můžem použít, abychom nezkoušeli zbytečný uličky;
    // TODO |    a navíc si můžem v podobnym duchu předpočítat i termy.
    // TODO |    Zatimbych to ale přeskočil a radši udělal generateOne.
    private List<ABC<String,Sub,Integer>> ts_k(int k, Type t, int nextVarId) {
        AB<Type,Sub> nf = normalize(t);
        List<ABC<String,Sub,Integer>> ts = core_k(k,nf._1(), ts_1(gamma), this::ts_ij, xs->xs,nextVarId);
        return denormalize(ts, nf);
    }

    private List<ABC<String,Sub,Integer>> ts_ij(int i, int j, Type t, int nextVarId) {
        AB<Type,Sub> nf = normalize(t);
        List<ABC<String,Sub,Integer>> ts = core_ij(i,j,nf._1(), this::ts_k, LSolver::mkAppString, xs->xs, nextVarId);
        return denormalize(ts, nf);
    }

    // -- Utils for CORE ---------------------------------------------

    private ABC<BigInteger,Integer,Integer> encodeSub(ABC<BigInteger,Sub,Integer> subData) {
        Sub sub = subData._2();
        String sub_str = sub.toString();
        Integer sub_id = sub2id.get(sub_str);

        if (sub_id == null) {
            sub_id = subsList.size();
            subsList.add(sub);
            sub2id.put(sub_str, sub_id);
        }

        BigInteger num = subData._1();
        int nextVarId  = subData._3();
        return ABC.mk(num,sub_id,nextVarId);
    }

    private List<ABC<BigInteger,Integer,Integer>> encodeSubs(List<ABC<BigInteger,Sub,Integer>> subs) {
        return F.map(subs, this::encodeSub);
    }

    private List<ABC<BigInteger,Sub,Integer>> decodeSubs(SizeTypeData sizeTypeData, int nextVarId) {

        List<ABC<BigInteger,Integer,Integer>> encodedSubs = sizeTypeData.getSubsData();
        int nextVarId_input = sizeTypeData.getNextVarId_input();

        List<ABC<BigInteger,Sub,Integer>> unIncremented = F.map(encodedSubs, p -> ABC.mk( p._1(), subsList.get(p._2()), p._3()));

        if (nextVarId <= nextVarId_input) {
            return unIncremented;
        }

        return F.map(unIncremented, subData -> incrementDecodedSub(subData, nextVarId_input, nextVarId));

    }

    private static ABC<BigInteger,Sub,Integer> incrementDecodedSub(ABC<BigInteger,Sub,Integer> subData, int nextVarId_input, int nextVarId) {

        int delta = nextVarId - nextVarId_input;
        Sub sub = subData._2();

        Set<Integer> codomainVarIds = sub.getCodomainVarIds();

        Sub incrementSub = new Sub();

        // todo | neni to nejaky celý blbě? rozmyslet co když to je poprvý volany že nextVarId_input = 0, že to je naky chlupatý
        // todo | ...

        for (Integer codomVarId : codomainVarIds) {
            if (codomVarId >= nextVarId_input) {
                incrementSub.add(codomVarId, new TypeVar(codomVarId + delta)); // TODO nekurvěj to nějak skolemizovaný ?
            }
        }

        Sub newSub = Sub.dot(incrementSub,sub);

        return ABC.mk(subData._1(), newSub, subData._3() + delta);
    }

    /* asi 2 blobosti, radši přehodnotit interpretaci volaní skolemizovaneho getOne..
    private static void denormalizeAppTree(AppTree appTree, Sub t2nf) {
        Sub nf2t = t2nf.inverse();
        appTree.applySub(nf2t);
    }
    private static Type mkAppTreeType(Sub s_FX, AB<Type,Sub> nfData) {
        Type goalNF = nfData._1();
        Sub  fromNF = nfData._2().inverse();
        return fromNF.apply(s_FX.apply(goalNF)).deskolemize();
    }*/

    private static <A> ABC<A,Sub,Integer> denormalize(ABC<A,Sub,Integer> x, AB<Type,Sub> nfData) {
        Function<ABC<A,Sub,Integer>,ABC<A,Sub,Integer>> f = mkDenormalizator(nfData);
        return f.apply(x);
    }

    private static <A> List<ABC<A,Sub,Integer>> denormalize(List<ABC<A,Sub,Integer>> xs, AB<Type,Sub> nfData) {
        Function<ABC<A,Sub,Integer>,ABC<A,Sub,Integer>> f = mkDenormalizator(nfData);
        return F.map(xs, f);
    }

    private static <A> Function<ABC<A,Sub,Integer>,ABC<A,Sub,Integer>> mkDenormalizator(AB<Type,Sub> nfData) {
        Sub  t2nf = nfData._2();
        Sub  nf2t = t2nf.inverse();
        return p -> {
            A a = p._1();
            int nextVarId = p._3();

            Sub sub_nf = p._2();
            Sub s1 = Sub.dot(sub_nf,t2nf);
            Sub sub = Sub.dot(nf2t, s1);

            return ABC.mk(a,sub,nextVarId); // TODO opravdu stačí jen zkopírovat nextVarId, určitě promyslet do hloubky !!!
        };
    }






    // -- STATIC FUNS : core of the method -----------------------------------------------------

    private static TriFun<Integer,Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_k(List<AB<String,Type>> gamma) {
        return (k,t,nextVarId) -> core_k(k, t, subs_1(gamma), subs_ij(gamma), LSolver::packSubs, nextVarId);
    }


    private static TetraFun<Integer,Integer,Type,Integer,List<ABC<BigInteger,Sub,Integer>>> subs_ij(List<AB<String,Type>> gamma) {
        return (i,j,t,nextVarId) -> core_ij(i, j, t, subs_k(gamma), BigInteger::multiply, LSolver::packSubs, nextVarId);
    }

    private static TriFun<Integer,Type,Integer,List<ABC<String,Sub,Integer>>> ts_k(List<AB<String,Type>> gamma) {
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

            AB<Type,Integer> t_s_p =  fresh(t_s,t,nextVarId); // todo ještě promyslet kterou přesně variantu //t_s.freshenVars(nextVarId); //fresh(t_s, t);
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


    private static AB<Type,Integer> fresh(Type typeToFresh, Type typeToAvoid, int nextVarId) {
        int startVarId = Math.max(typeToAvoid.getNextVarId(),nextVarId);
        return typeToFresh.freshenVars(startVarId);
    }

    private static Type fresh(Type typeToFresh, Type typeToAvoid) {
        int startVarId = typeToAvoid.getNextVarId();
        Sub old2fresh = new Sub();
        AB<Type,Integer> p = typeToFresh.freshenVars(startVarId, old2fresh);
        return p._1();
    }

    private static AB<Type,Sub> normalize(Type t) {

        Sub t2nf = new Sub();
        int startVarId = t.getNextVarId_onlySkolemVars(); //0;
        Type nf = t.freshenVars(startVarId, t2nf)._1();

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

    private static JSONArray sizeTypeDataToJson(List<ABC<BigInteger,Integer,Integer>> subs) {
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

    private static void tests_treeGenerating(Checker ch,int k_max, int numSamples){
        Log.it("\n== TREE GENERATING TESTS =======================================================\n");

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

        Type t = Types.parse("(P A (P A A)) -> (P A (P A A))");
        for (int k = 1; k <= k_max; k++) {
            testTreeGenerating(ch, k, t, gamma, numSamples);
        }
    }

    /*
    private static void tests_lambdaDags(Checker ch, int k_max, int numSamples) {
        Log.it("\n== LAMBDA DAGS TESTS =======================================================\n");

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


        //Type t_0 = Types.parse("(P a (P b c)) -> (P c (P b a))");
        //test_ts_k(ch, 7, t_0, gamma); // TODO vyřešit otázky nastolené tímto testem !



        Type t = Types.parse("(P A (P A A)) -> (P A (P A A))");
        for (int k = 1; k <= k_max; k++) {
            testGenerating(ch, k, t, gamma, numSamples);
        }

        //TODO pro k = 5 exituje anomální zóna
        //...
        //<(((s s) (k fst)) k),{}> 1
        //<(((s s) (k k)) k),{}> 0
        //<(((s s) (k mkP)) k),{}> 1
        //<(((s s) (k snd)) k),{}> 0
        //<(((s s) (s k)) k),{}> 1
        //...


        // todo pro s.num(7, ((P A (P A A)) -> (P A (P A A)))) = 37596
        // ...
        // 700
        // !!! [KO 46] <((((s deDag) (k k)) (mkDag s)) k),{}> is not in genAll list.

    }*/

    /*private static void separateError1() {

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

        Type t = Types.parse("(P A (P A A)) -> (P A (P A A))");
        int k = 5;

        long seed = 1;

        LSolver s = new LSolver(gamma, new Random(seed));

        AB<String,Sub> p_gen = s.generateOne(k,t);

        List<AB<String,Sub>> allTrees = s.ts_k(k,t);

        Set<String> testMap = new TreeSet<>();

        for (AB<String,Sub> tree : allTrees) {
            testMap.add(tree.toString());
        }

        if (testMap.contains(p_gen.toString())) {
            Log.it("Mám ho!");
            Log.it("seed: "+seed);
            Log.it("tree: "+p_gen);
        } else {
            Log.it("Zkus to znova..");
        }

    }*/

    private static void testTreeGenerating(Checker ch, int k, Type t, List<AB<String,Type>> gamma, int numSamples) {
        String argStr = "("+k+", "+t+")";

        LSolver s = new LSolver(gamma, ch.getRandom());

        Log.it_noln("s.num"+argStr+" = ");
        BigInteger num = s.getNum(k,t);
        Log.it(num);

        Log.it_noln("s.generateOne"+argStr+" = ");
        AppTree tree = s.genOne(k, t);
        Log.it(tree);

        if (F.isZero(num) || tree == null) {
            ch.is(F.isZero(num) && tree == null, "num = 0 iff genOne = null");
        }

        if (!F.isZero(num) && num.compareTo(BigInteger.valueOf(100000)) < 0) {

            int intNum = num.intValueExact();

            Log.it_noln("|s.ts_k"+argStr+"| = ");
            List<ABC<String,Sub,Integer>> allTrees = s.ts_k(k,t,0); // TODO ??????????????????????????????????????????? 0 je ok?
            Log.it(allTrees.size());

            ch.is(tree != null, "genOne not null");
            ch.is(intNum == allTrees.size(), "num = |genAll|");

            if (intNum < 40000) {

                Map<String,Integer> testMap = new TreeMap<>();

                for (ABC<String,Sub,Integer> tree_p : allTrees) {
                    testMap.put(tree_p._1(),0);
                }

                //Log.list(allTrees);

                double sampleRate = ((double)numSamples) / intNum;

                boolean allGeneratedWereInGenAll = true;
                boolean allTreesWereStrictlyWellTyped = true;
                for (int i = 0; i < numSamples; i++){

                    if ((i+1)%100 == 0) {
                        Log.it(i+1);
                    }

                    AppTree newTree = s.genOne(k, t);

                    if (newTree != null) {

                        if (!newTree.isStrictlyWellTyped()) {
                            ch.fail("tree is not strictly well-typed: "+newTree+"\n"+newTree.getTypeTrace().toString());
                            allTreesWereStrictlyWellTyped = false;
                        }

                        String key = newTree.toRawString();

                        if (testMap.containsKey(key)) {
                            testMap.compute(key, (_key,n) -> n+1);
                        } else {
                            allGeneratedWereInGenAll = false;
                            ch.fail(key +" is not in genAll list.");
                        }

                    } else {
                        ch.fail("generated tree is null");
                    }
                }

                ch.is(allGeneratedWereInGenAll,"All generated trees were in GenAll list.");
                ch.is(allTreesWereStrictlyWellTyped,"All trees were strictly well typed.");


                Log.it("\nSample rate : "+ sampleRate);
                for (Map.Entry<String,Integer> e : testMap.entrySet()) {

                    String tree_p = e.getKey();
                    int numGenerated = e.getValue();

                    Log.it(tree_p +" "+ numGenerated);

                }


            }

        }

        Log.it();


        //throw new TODO();
    }

    /*
    private static void testGenerating(Checker ch, int k, Type t, List<AB<String,Type>> gamma, int numSamples) {
        String argStr = "("+k+", "+t+")";

        LSolver s = new LSolver(gamma, ch.getRandom());

        Log.it_noln("s.num"+argStr+" = ");
        BigInteger num = s.getNum(k,t);
        Log.it(num);

        Log.it_noln("s.generateOne"+argStr+" = ");
        AB<String,Sub> p_gen = s.generateOne(k, t);
        Log.it(p_gen);

        if (F.isZero(num) || p_gen == null) {
            ch.is(F.isZero(num) && p_gen == null, "num = 0 iff genOne = null");
        }

        if (!F.isZero(num) && num.compareTo(BigInteger.valueOf(100000)) < 0) {

            int intNum = num.intValueExact();

            Log.it_noln("|s.ts_k"+argStr+"| = ");
            List<AB<String,Sub>> allTrees = s.ts_k(k,t);
            Log.it(allTrees.size());

            ch.is(p_gen != null, "genOne not null");
            ch.is(intNum == allTrees.size(), "num = |genAll|");

            if (intNum < 40000) {

                Map<String,Integer> testMap = new TreeMap<>();

                for (AB<String,Sub> tree : allTrees) {
                    testMap.put(tree.toString(),0);
                }

                //Log.list(allTrees);

                //int sampleRate = 100;
                //int numSamples = sampleRate * intNum;

                double sampleRate = ((double)numSamples) / intNum;

                boolean allGeneratedWereInGenAll = true;
                for (int i = 0; i < numSamples; i++){

                    if ((i+1)%100 == 0) {
                        Log.it(i+1);
                    }

                    AB<String,Sub> newTree = s.generateOne(k, t);

                    if (newTree != null) {

                        String key = newTree.toString();

                        if (testMap.containsKey(key)) {
                            testMap.compute(key, (_key,n) -> n+1);
                        } else {
                            allGeneratedWereInGenAll = false;
                            ch.fail(key +" is not in genAll list.");
                        }

                    } else {
                        ch.fail("generated tree is null");
                    }
                }
                ch.is(allGeneratedWereInGenAll,"All generated trees were in GenAll list.");


                Log.it("\nSample rate : "+ sampleRate);
                for (Map.Entry<String,Integer> e : testMap.entrySet()) {

                    String tree = e.getKey();
                    int numGenerated = e.getValue();

                    Log.it(tree +" "+ numGenerated);

                }


            }

        }

        Log.it();
    }*/

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

        List<ABC<String, Sub,Integer>> ts = ts_k(gamma).apply(k, t_nf, 0); // todo 0 ?
        Log.it("-- ts_"+k+"(gamma, t_nf) ------------");
        Log.listLn(ts);

        List<ABC<BigInteger,Sub,Integer>> subs = subs_k(gamma).apply(k, t_nf, 0); // todo 0 ?
        Log.it("-- subs_"+k+"(gamma, t_nf) ----------");
        Log.listLn(subs);

        Log.it("Creating LSolver ... initial state:");
        LSolver solver = new LSolver(gamma, ch.getRandom());
        Log.it(solver);

        List<ABC<BigInteger,Sub,Integer>> subs2 = solver.subs_k(k, t_nf, 0); // todo 0 ?
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
