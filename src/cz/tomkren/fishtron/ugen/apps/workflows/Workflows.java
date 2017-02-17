package cz.tomkren.fishtron.ugen.apps.workflows;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eval.*;
import cz.tomkren.fishtron.workflows.MyList;
import cz.tomkren.fishtron.workflows.TypedDag;
import cz.tomkren.utils.*;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Created by user on 14. 2. 2017. */

public class Workflows {

    public static void main(String[] args) {
        //test_generating();
        test_evaluating(64);
    }

    public static final Type  goal  = Types.parse("Dag D LD");
    public static final Gamma gamma = Gamma.mk(
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

    private static final Type Dag = Types.parse("Dag");
    private static final Type D   = Types.parse("D");
    private static final Type LD  = Types.parse("LD");

    public static final EvalLib lib = EvalLib.mk(
            "dia",        (TD.Op3) TypedDag::dia,
            "dia0",       (TD.Op2) TypedDag::dia0,
            "split",      (TD.DL)  TypedDag::split,
            "cons",       (TD.OL)  MyList::cons,
            "nil",        MyList.NIL,

            "PCA",         mkMethod("PCA"),
            "kBest",       mkMethod("kBest"),
            "kMeans",      mkMethod("kMeans"),
            "copy",        mkMethod("copy"),
            "SVC",         mkMethod("SVC"),
            "logR",        mkMethod("logR"),
            "gaussianNB",  mkMethod("gaussianNB"),
            "DT",          mkMethod("DT"),
            "vote",        mkMethod("vote"),

            "stacking",    (TD.Op2) TypedDag::stacking,
            "stacker",     mkMethod("stacker"),

            "boosting",    (TD.DLD) TypedDag::boosting,
            "booBegin",    mkMethod("booBegin"),
            "booster",     (TD.Op) TypedDag::booster,
            "booEnd",      mkMethod("booEnd")
    );

    private static EvalCode mkMethod(String name) {
        return (params,type) -> {
            AA<Type> p = getDagInOutTypes(type);
            JSONObject jsonParams = params == null ? new JSONObject() : params.toJson();
            return new TypedDag(name, p._1(), p._2(), jsonParams, null);
        };
    }

    private static AA<Type> getDagInOutTypes(Type type) {
        if (type instanceof TypeTerm) {
            TypeTerm tt = (TypeTerm) type;
            List<Type> args = tt.getArgs();
            if (args.size() == 3 && args.get(0).equals(Dag)) {
                return new AA<>(args.get(1),args.get(2));
            }
        }
        throw new Error("Type "+type+" was expected to be (Dag a b) type!");
    }


    // -- Testing ----------------------------------

    private static void test_evaluating(int k_max) {
        Checker ch = new Checker();
        EvalTester.testLib(ch, k_max, lib, gamma, goal, true, dag -> ((TypedDag)dag).toJson());
        ch.results();
    }

    private static void test_generating() {
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

                Log.it( k +"\t\t\t| "+ num + " \t\t\t| "+
                        F.prettyDouble(buildTime, 3)+ " s   \t| "+
                        F.prettyDouble(meanGenOneTime, 3)+" s");



            } else {
                Log.it( k +"\t\t\t| "+ num + " \t\t\t| "+
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
