package cz.tomkren.fishtron.ugen.apps;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import cz.tomkren.utils.Stopwatch;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Created by user on 14. 2. 2017. */

public class Workflows {

    private static final Type  goal  = Types.parse("Dag D LD");
    private static final Gamma gamma = Gamma.mk(
            "dia",        "(Dag D D) -> ((Dag D (V LD n an)) -> ((Dag (V LD n an) LD) -> (Dag D LD)))",
            "dia0",       "(Dag D (V LD n an)) -> ((Dag (V LD n an) LD) -> (Dag D LD))",
            "split",      "(Dag D (V D n an)) -> ((V (Dag D LD) n an) -> (Dag D (V LD n an)))",
            "cons",       "a -> ((V a n an) -> (V a (S n) an))",
            "nil",        "V a 0 an",

            "PCA",        "Dag D D",
            "kBest",      "Dag D D",
            "kMeans",     "Dag D (V D (S(S n)) Disj)",
            "copy",       "Dag D (V D (S(S n)) Copy)",
            "SVC",        "Dag D LD",
            "logR",       "Dag D LD",
            "gaussianNB", "Dag D LD",
            "DT",         "Dag D LD",
            "vote",       "Dag (V LD (S(S n)) an) LD",

            "stacking",   "(Dag (V LD n Copy) D) -> ((Dag D LD) -> (Dag (V LD n Copy) LD))",
            "stacker",    "Dag (V LD (S(S n)) Copy) D",

            "boosting",   "(Dag D Boo) -> ((V (Dag Boo Boo) (S(S n)) an) -> ((Dag Boo LD) -> (Dag D LD)))",
            "booBegin",   "Dag D Boo",
            "booster",    "(Dag D LD) -> (Dag Boo Boo)",
            "booEnd",     "Dag Boo LD"
    );

    public static void main(String[] args) {
        Checker ch = new Checker();

        Log.it();
        Log.it("Goal = "+goal);
        Log.it("Gamma =");
        Log.it(gamma);
        Log.it();

        Gen gen = new Gen(Gen.Opts.mkDefault(), gamma, ch.getRandom());

        int max_k = 64;
        int numToGenerate = 10000;

        boolean allTreesWereStrictlyWellTyped = true;
        List<AppTree> treeExamples = new ArrayList<>();

        Log.it("Num trees to generate for each size: "+numToGenerate);
        Log.it();

        Log.it("Tree size   | Num trees     | build time    | mean genOne time ");
        Log.it("------------|---------------|---------------|------------------");

        for (int k = 1; k <= max_k; k++) {
            Stopwatch swBuildTime = new Stopwatch();

            BigInteger num = gen.getNum(k, goal);

            double buildTime = swBuildTime.getTime();

            if (!F.isZero(num)) {

                double sumGenOneTime = 0.0;

                for (int i = 0; i < numToGenerate; i++){
                    Stopwatch swOneTree = new Stopwatch();

                    AppTree tree = gen.genOne(k,goal);

                    double genOneTime = swOneTree.getTime();
                    sumGenOneTime += genOneTime;

                    if (!tree.isStrictlyWellTyped(gamma)) {
                        ch.fail("tree is not strictly well-typed: "+tree+"\n"+tree.getTypeTrace().toString());
                        allTreesWereStrictlyWellTyped = false;
                    }


                    if (i == 0) {
                        treeExamples.add(tree);
                    }
                }

                double meanGenOneTime = sumGenOneTime / numToGenerate;

                Log.it( k +"\t\t\t| "+
                        num + " \t\t\t| "+
                        F.prettyDouble(buildTime, 3)+ " s   \t| "+
                        F.prettyDouble(meanGenOneTime, 3)+" s");



            } else {
                Log.it(k +"\t\t\t| "+
                       num + " \t\t\t| "+
                       F.prettyDouble(buildTime, 3)+ " s   \t|");
            }


        }

        Log.it();
        Log.it("Total number of trees generated: "+ numToGenerate*max_k);
        ch.is(allTreesWereStrictlyWellTyped,"All trees were strictly well typed.");
        Log.it();
        Log.it("Tree examples:");
        Log.list(treeExamples);


        ch.results();
    }


}
