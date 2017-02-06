package cz.tomkren.fishtron.ugen.tests;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.Mover;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.TsRes;
import cz.tomkren.fishtron.ugen.nf.NF;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.Log;

import java.util.List;

/** Created by Tomáš Křen on 5.2.2017. */

public class GenTester {

    public static void main(String[] args) {
        test_2();
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

    private static void test_2() {
        Checker ch = new Checker(7404398919224944163L);

        testNormalizations(ch);

        Gen.Opts opts = Gen.Opts.mkDefault();
        tests_subs_1(ch, opts);
        tests_subs_k(ch, opts);

        // todo
        //tests_treeGenerating(ch, 6, 100, opts);

        ch.results();
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

        List<TsRes> ts = StaticGen.ts_k(gamma, k, t_nf, 0);
        Log.it("-- ts_"+k+"(gamma, t_nf) ------------");
        Log.listLn(ts);

        List<SubsRes> subs_unmoved = StaticGen.subs_k(gamma, k, t_nf, 0);
        Log.it("-- subs_"+k+"(gamma, t_nf) : UNMOVED ----------");
        Log.listLn(subs_unmoved);

        List<SubsRes> subs_moved = Mover.moveSubsResults(t_nf, 0, subs_unmoved);

        Log.it("-- ... and MOVED, but unpacked ----------");
        Log.listLn(subs_moved);

        List<SubsRes> subs_moved_packed = Gen.pack(subs_moved);

        Log.it("-- ... and MOVED and PACKED ----------");
        Log.listLn(subs_moved_packed);

        List<SubsRes> subs2 = gen.subs(k, t_nf, 0);
        Log.it("-- LSolver.subs("+k+", t_nf, 0) ----------");
        Log.listLn(subs2);

        ch.list(subs2,subs_moved_packed);

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
