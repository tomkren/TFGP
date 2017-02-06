package cz.tomkren.fishtron.ugen.tests;


import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.nf.NF;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.Log;

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

        // todo
        //Gen.Opts opts = Gen.Opts.mkDefault();
        //tests_subs_1(ch, opts);
        //tests_subs_k(ch, opts);
        //tests_treeGenerating(ch, 6, 100, opts);

        ch.results();
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
        Log.it("----------------------------------");
        ch.it(nf.toNF(t),typeInNF.toString());
        ch.it(nf.fromNF(nf.toNF(t)),t.toString());

        Log.it();
    }


}
