package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.*;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Created by tom on 13. 9. 2016.*/

public class LSolverTester {

    // TODO decide (2)
    private static int initNextVarDecision_forTesting(Type t) {
        return t.getNextVarId(); // vs. return 0;
    }

    private static final Gamma g_testGamma = Gamma.mk(
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

    private static final Type g_testGoal = Types.parse("(P A (P A A)) -> (P A (P A A))");


    public static void main(String[] args) {
        boolean wasOk = true;
        int k = 6;
        int seed = 0;//452;
        while (wasOk) {
            wasOk = separateError_strictlyWellTyped(seed, g_testGamma, k, g_testGoal, LSolver.Opts.mkDefault());
            Log.it("----------------------------------");
            seed++;
        }
    }

    public static void main_(String[] args) {
        Checker ch = new Checker(7404398919224944163L);

        LSolver.Opts opts = LSolver.Opts.mkDefault();

        testNormalizations(ch);
        tests_subs_1(ch, opts);
        tests_subs_k(ch, opts);

        tests_treeGenerating(ch, 6, 100, opts);

        ch.results();
    }

    private static boolean separateError_strictlyWellTyped(int seed, Gamma gamma, int k, Type t, LSolver.Opts opts) {

        Checker ch = new Checker((long) seed);

        LSolver s = new LSolver(opts, gamma, ch.getRandom());

        boolean wasOk = true;

        AppTree newTree = s.genOne(k, t);
        if (newTree != null) {
            boolean isStrictlyWellTyped = newTree.isStrictlyWellTyped(gamma);

            ch.is(isStrictlyWellTyped, "Is tree strictly well typed?");

            JSONObject typeTrace = newTree.getTypeTrace();
            Log.it(typeTrace);

            if (!isStrictlyWellTyped) {
                Log.it("tree is not strictly well-typed: " + newTree + "\n" + typeTrace.toString());
                AppTree.writeErrorTreeToFile(typeTrace);
                wasOk = false;
            }
        }

        if (!wasOk) {
            ch.results();
        }

        return wasOk;
    }



    private static void tests_treeGenerating(
            Checker ch,int k_max, int numSamples, LSolver.Opts opts)
    {
        Log.it("\n== TREE GENERATING TESTS =======================================================\n");

        Type t = Types.parse("(P A (P A A)) -> (P A (P A A))");
        for (int k = 1; k <= k_max; k++) {
            testTreeGenerating(ch, k, t, g_testGamma, numSamples,opts);
        }
    }


    private static void testTreeGenerating(Checker ch, int k, Type t, Gamma gamma, int numSamples, LSolver.Opts opts) {
        String argStr = "("+k+", "+t+")";

        LSolver s = new LSolver(opts, gamma, ch.getRandom());

        Log.it_noln("s.num"+argStr+" = ");
        BigInteger num = s.getNum(k, t, initNextVarDecision_forTesting(t));
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
            List<ABC<String,Sub,Integer>> allTrees = s.ts_k(k,t, initNextVarDecision_forTesting(t));
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

                        if (!newTree.isStrictlyWellTyped(gamma)) {
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
    }


    private static void tests_subs_k(Checker ch, LSolver.Opts opts) {
        Log.it("\n== ts_k & subs_k tests ===================================================\n");

        Gamma gamma1 = Gamma.mk(
                "f", "X -> X",
                "seri", "(a -> b) -> ((b -> c) -> (a -> c))"
        );

        test_ts_k(ch, 1, "X -> X", gamma1, opts);
        test_ts_k(ch, 2, "X -> X", gamma1, opts);
        test_ts_k(ch, 3, "X -> X", gamma1, opts);
    }

    private static void tests_subs_1(Checker ch, LSolver.Opts opts) {
        Log.it("\n== ts_1 & subs_1 tests ===================================================\n");

        Gamma gamma1 = Gamma.mk(
                "s", "(a -> (b -> c)) -> ((a -> b) -> (a -> c))",
                "s2","(x5 -> (x0 -> x1)) -> ((x5 -> x0) -> (x5 -> x1))",
                "s3","(y5 -> (x0 -> x1)) -> ((y5 -> x0) -> (y5 -> x1))",
                "k", "a -> (b -> a)",
                "k2","x1 -> (x0 -> x1)",
                "+", "Int -> (Int -> Int)",
                "42", "Int",
                "magicVal", "alpha"
        );

        test_ts_k(ch, 1, "Int -> Int", gamma1, opts);
        test_ts_k(ch, 1, "x1 -> x0",   gamma1, opts);
    }

    private static void test_ts_k(Checker ch, int k, String tStr, Gamma gamma, LSolver.Opts opts) {
        test_ts_k(ch, k, Types.parse(tStr), gamma, opts);
    }

    private static void test_ts_k(Checker ch, int k, Type t, Gamma gamma, LSolver.Opts opts) {

        NF p_nf = new NF(opts.isNormalizationPerformed(), t);
        Type t_nf = p_nf.getTypeInNF();
        Sub t2nf  = p_nf.getToNF();

        Log.it();
        Log.it("-- LIB gamma -------------");
        Log.listLn(gamma.getSymbols());

        Log.it("-- GOAL TYPE t -----");
        Log.it("t: "+t);
        Log.it("t_nf: "+t_nf);
        Log.it("t2nf: "+t2nf);

        ch.it(t2nf.apply(t), t_nf.toString());
        Log.it();

        List<ABC<String, Sub,Integer>> ts = LSolver.ts_k(gamma).apply(k, t_nf, initNextVarDecision_forTesting(t_nf));
        Log.it("-- ts_"+k+"(gamma, t_nf) ------------");
        Log.listLn(ts);

        List<ABC<BigInteger,Sub,Integer>> subs = LSolver.subs_k(gamma).apply(k, t_nf, initNextVarDecision_forTesting(t_nf));
        Log.it("-- subs_"+k+"(gamma, t_nf) ----------");
        Log.listLn(subs);

        Log.it("Creating LSolver ... initial state:");
        LSolver solver = new LSolver(opts, gamma, ch.getRandom());
        Log.it(solver);

        List<ABC<BigInteger,Sub,Integer>> subs2 = solver.subs_k(k, t_nf, initNextVarDecision_forTesting(t_nf));
        Log.it("-- LSolver.subs_"+k+"(gamma, t_nf) ----------");
        Log.listLn(subs);

        Log.it("... LSolver after subs_k call:");
        Log.it(solver);


        ch.list(subs2,subs);

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
        checkNormalisation(ch, "(x1 -> (x4 -> (x4 -> (x5 -> (x66 -> (x0 -> (x0 -> (x3 -> (x77 -> (x4 -> (x66 -> (x5 -> (x77 -> (x88 -> (x1 -> x2)))))))))))))))");
        checkNormalisation(ch, "(x10 -> (x0 -> (x4 -> (x55 -> (x4 -> (x55 -> (x0 -> (x33 -> (x8 -> (x7 -> (x6 -> (x5 -> (x7 -> (x8 -> (x6 -> x2)))))))))))))))");
    }

    private static void checkNormalisation(Checker ch, String tStr) {
        checkNormalisation(ch, Types.parse(tStr));
    }

    private static void checkNormalisation(Checker ch, Type t) {
        NF p = new NF(t);
        Type typeInNF  = p.getTypeInNF();
        Sub toNF = p.getToNF();
        Sub fromNF = p.getFromNF();

        ch.it(t);
        ch.it(typeInNF);
        ch.it(toNF);
        Log.it("----------------------------------");
        ch.it(toNF.apply(t),typeInNF.toString());
        ch.it(fromNF.apply(toNF.apply(t)),t.toString());

        Log.it();
    }



}
