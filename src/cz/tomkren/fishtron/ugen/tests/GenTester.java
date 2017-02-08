package cz.tomkren.fishtron.ugen.tests;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.TsRes;
import cz.tomkren.fishtron.ugen.nf.NF;
import cz.tomkren.utils.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Created by Tomáš Křen on 5.2.2017. */

public class GenTester {

    public static void main(String[] args) {
        test_1();
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

    private static void test_1() {
        Checker ch = new Checker(7404398919224944163L);

        testNormalizations(ch);

        Gen.Opts opts = Gen.Opts.mkDefault();
        tests_subs_1(ch, opts);
        tests_subs_k(ch, opts);
        tests_treeGenerating(ch, 8/*6*/, 100000, opts);

        ch.results();
    }

    private static void tests_treeGenerating(Checker ch,int k_max, int numSamples, Gen.Opts opts) {
        Log.it("\n== TREE GENERATING TESTS =======================================================\n");
        for (int k = 1; k <= k_max; k++) {
            testTreeGenerating(ch, k, g_testGoal, g_testGamma, numSamples,opts);
        }
    }

    private static void testTreeGenerating(Checker ch, int k, Type t, Gamma gamma, int numSamples, Gen.Opts opts) {

        Log.it("--  k = "+k+"  ---------------------------------------------------------");

        String argStr = "("+k+", "+t+")";

        Gen gen = new Gen(opts, gamma, ch.getRandom());

        Stopwatch stopwatch = new Stopwatch(3);

        Log.it_noln("gen.getNum"+argStr+" = ");
        BigInteger num = gen.getNum(k, t);
        Log.it(num + stopwatch.restart());

        Log.it(gen.getCache().getCachedSubsStats());

        Log.it_noln("StaticGen.getNum"+argStr+" = ");
        BigInteger num2 = StaticGen.getNum(gamma, k, t);
        Log.it(num2 + stopwatch.restart());

        Log.it_noln("(2) gen.getNum"+argStr+" = ");
        BigInteger num3 = gen.getNum(k, t);
        Log.it(num3 + stopwatch.restart());

        Log.it_noln("gen.subs"+argStr+".size() = ");
        int numSubs = gen.subs(k, t, 0).size();
        Log.it(numSubs + stopwatch.restart());

        Log.it_noln("s.genOne"+argStr+" = ");
        AppTree tree = gen.genOne(k, t);
        Log.it(tree + stopwatch.restart());

        ch.is(num.equals(num2), "gen.getNum = StaticGen.getNum");
        ch.is(num.equals(num3), "gen.getNum = gen.getNum(second time..)");


        if (F.isZero(num) || tree == null) {
            ch.is(F.isZero(num) && tree == null, "num = 0 iff genOne = null");
        }

        if (!F.isZero(num) && num.compareTo(BigInteger.valueOf(100000)) < 0) {
            int intNum = num.intValueExact();

            Log.it_noln("|s.ts_k"+argStr+"| = ");
            List<TsRes> allTrees = StaticGen.ts(gamma, k, t, 0);
            Log.it(allTrees.size() + stopwatch.restart());

            ch.is(tree != null, "genOne not null");
            ch.is(intNum == allTrees.size(), "num = |genAll|");

            if (intNum < 40000) {

                Map<String,Integer> testMap = new TreeMap<>();

                for (TsRes tsRes : allTrees) {
                    testMap.put(tsRes.getTree().toRawString(),0);
                }

                double sampleRate = ((double)numSamples) / intNum;
                boolean allGeneratedWereInGenAll = true;
                boolean allTreesWereStrictlyWellTyped = true;

                double sumGenOneTime = 0.0;

                for (int i = 0; i < numSamples; i++){
                    if ((i+1)%(k<=6?1000:100) == 0) {
                        Log.it(i+1+" trees generated (k="+k+") .. so far mean genOne time: "+ sumGenOneTime / i);
                    }

                    Stopwatch sw = new Stopwatch(5);
                    AppTree newTree = gen.genOne(k, t);

                    double genOneTime = sw.getTime();
                    sumGenOneTime += genOneTime;

                    if (newTree != null) {

                        if (!newTree.isStrictlyWellTyped(gamma)) {
                            ch.fail("tree is not strictly well-typed: "+newTree+"\n"+newTree.getTypeTrace().toString());
                            allTreesWereStrictlyWellTyped = false;
                        }

                        String key = newTree.toRawString();

                        if (testMap.containsKey(key)) {
                            testMap.compute(key, (_key,n) -> n+1);
                        } else {
                            ch.fail(key +" is not in genAll list.");
                            allGeneratedWereInGenAll = false;
                        }

                    } else {
                        ch.fail("generated tree is null");
                    }
                }

                ch.is(allGeneratedWereInGenAll,"All generated trees were in GenAll list.");
                ch.is(allTreesWereStrictlyWellTyped,"All trees were strictly well typed.");

                Log.it();

                int minNumGenerated = Integer.MAX_VALUE;
                int maxNumGenerated = -1;

                for (Map.Entry<String, Integer> e : testMap.entrySet()) {
                    String tree_p = e.getKey();
                    int numGenerated = e.getValue();
                    if (intNum < 100) {Log.it(tree_p + " " + numGenerated);}

                    if (numGenerated > maxNumGenerated) {maxNumGenerated = numGenerated;}
                    if (numGenerated < minNumGenerated) {minNumGenerated = numGenerated;}
                }

                Log.it();
                Log.it("Sample rate: "+ sampleRate);
                Log.it("Num samples: "+ numSamples);
                Log.it("Sum genOne time: "+ sumGenOneTime);
                Log.it("mean genOne time: "+ sumGenOneTime / numSamples);
                Log.it("minNumGenerated: "+ minNumGenerated);
                Log.it("maxNumGenerated: "+ maxNumGenerated);
            }
        }

        Log.it();
    }


    private static void tests_subs_k(Checker ch, Gen.Opts opts) {
        Log.it("\n== ts_k & subs_k tests ==================================================\n");

        Gamma gamma1 = Gamma.mk(
                "f", "X -> X",
                "seri", "(a -> b) -> ((b -> c) -> (a -> c))"
        );

        test_ts_k(ch, 1, "X -> X", gamma1, opts);
        test_ts_k(ch, 2, "X -> X", gamma1, opts);
        test_ts_k(ch, 3, "X -> X", gamma1, opts);
    }

    private static void tests_subs_1(Checker ch, Gen.Opts opts) {
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

    private static void test_ts_k(Checker ch, int k, String tStr, Gamma gamma, Gen.Opts opts) {
        test_ts_k(ch, k, Types.parse(tStr), gamma, opts);
    }

    private static void test_ts_k(Checker ch, int k, Type t, Gamma gamma, Gen.Opts opts) {

        NF nf = new NF(opts.isNormalizationPerformed(), t);
        Type t_nf = nf.getTypeInNF();

        Log.it();
        Log.it("-- LIB gamma -------------");
        Log.listLn(gamma.getSymbols());

        Log.it("-- GOAL TYPE t -----");
        Log.it("t: "+t);
        Log.it("t_nf: "+t_nf);
        //Log.it("t2nf: "+t2nf); TODO udělat vypisování toNF type transformace

        ch.it(nf.toNF(t), t_nf.toString());
        Log.it();

        Log.it("Creating Gen ... initial state:");
        Gen gen = new Gen(opts, gamma, ch.getRandom());
        Log.it(gen);

        List<TsRes> ts = StaticGen.ts(gamma, k, t_nf, 0);
        Log.it("-- ts_"+k+"(gamma, t_nf) ------------");
        Log.listLn(ts);

        List<SubsRes> subs = StaticGen.subs(gamma, k, t_nf, 0);
        Log.it("-- StaticGen.subs("+k+", t_nf, 0) ----------");
        Log.listLn(subs);

        List<SubsRes> subs2 = gen.subs(k, t_nf, 0);
        Log.it("-- Gen.subs("+k+", t_nf, 0) ----------");
        Log.listLn(subs2);

        ch.list(subs2,subs);

        Log.it("-------------------------------------------------------");

    }

    private static void testNormalizations(Checker ch) {

        Log.it("\n== normalization tests ===================================================\n");

        Type t1 = Types.parse("(x111 -> (x11 -> x1)) -> ((x111 -> x11) -> (x111 -> x1))");
        ch.it(t1);
        ch.it(((TypeTerm)t1).fold(Object::toString, Object::toString) +"\n");
        checkNF(ch, t1);

        checkNF(ch,
            "(x0 -> (x11 -> x1)) -> ((x0 -> x11) -> (x0 -> x1))",
            "(x2 -> (x1 -> x0)) -> ((x2 -> x1) -> (x2 -> x0))",
            "(x2 -> (x0 -> x1)) -> ((x2 -> x0) -> (x2 -> x1))",
            "(x1 -> (x4 -> (x4 -> (x5 -> (x66 -> (x0 -> (x0 -> (x3 -> (x77 -> (x4 -> (x66 -> (x5 -> (x77 -> (x88 -> (x1 -> x2)))))))))))))))",
            "(x10 -> (x0 -> (x4 -> (x55 -> (x4 -> (x55 -> (x0 -> (x33 -> (x8 -> (x7 -> (x6 -> (x5 -> (x7 -> (x8 -> (x6 -> x2)))))))))))))))"
        );
    }

    private static void checkNF(Checker ch, String... tStrs) {
        for (String tStr : tStrs) {
            checkNF(ch, Types.parse(tStr));
        }
    }

    private static void checkNF(Checker ch, Type t) {
        NF nf = new NF(t);
        Type typeInNF  = nf.getTypeInNF();

        ch.it(t);
        ch.it(typeInNF);
        // ch.it(toNF); TODO udělat vypisování toNF TypeTransformace
        Log.it("----------------------------------");
        ch.it(nf.toNF(t),typeInNF.toString());
        ch.it(nf.fromNF(nf.toNF(t)),t.toString());

        Log.it();
    }


}
