package net.fishtron.apps.gpml.dag;

import net.fishtron.apps.gpml.MyList;
import net.fishtron.eval.EvalCode;
import net.fishtron.eval.EvalLib;
import net.fishtron.eval.EvalTester;
import net.fishtron.gen.Gen;
import net.fishtron.trees.AppTree;
import net.fishtron.trees.Gamma;
import net.fishtron.trees.GammaSym;
import net.fishtron.trees.params.Params;
import net.fishtron.types.Type;
import net.fishtron.types.TypeTerm;
import net.fishtron.types.Types;
import net.fishtron.utils.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/** Created by user on 14. 2. 2017. */

public class NewSimpleWorkflows {

    public static void main(String[] args) {
        //test_generating();
        test_evaluating(64);

        //test_dynamicMagic();
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

    private static final Gamma gamma_general = Gamma.mk(
            "dia",        "(Dag D D) -> ((Dag D (V LD n an)) -> ((Dag (V LD n an) LD) -> (Dag D LD)))",
            "dia0",       "(Dag D (V LD n an)) -> ((Dag (V LD n an) LD) -> (Dag D LD))",
            "split",      "(Dag D (V D n an)) -> ((V (Dag D LD) n an) -> (Dag D (V LD n an)))",
            "cons",       "a -> ((V a n an) -> (V a (S n) an))",
            "nil",        "V a 0 an"
    );

    private static final Gamma gamma_stacking = Gamma.mk(
            "stacking",   "(Dag (V LD n Copy) D) -> ((Dag D LD) -> (Dag (V LD n Copy) LD))",
            "stacker",    "Dag (V LD (S(S n)) Copy) D"
    );

    private static final Gamma gamma_boosting = Gamma.mk(
            "boosting",   "(Dag D Boo) -> ((V (Dag Boo Boo) (S(S n)) an) -> ((Dag Boo LD) -> (Dag D LD)))",
            "booBegin",   "Dag D Boo",
            "booster",    "(Dag D LD) -> (Dag Boo Boo)",
            "booEnd",     "Dag Boo LD"
    );


    private static final Type Dag = Types.parse("Dag");

    public static final EvalLib lib = EvalLib.mk(
            "dia",        (NewSimpleTD.Op3) NewSimpleTypedDag::dia,
            "dia0",       (NewSimpleTD.Op2) NewSimpleTypedDag::dia0,
            "split",      (NewSimpleTD.DL)  NewSimpleTypedDag::split,
            "cons",       (NewSimpleTD.OL)  MyList::cons,
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

            "stacking",    (NewSimpleTD.Op2) NewSimpleTypedDag::stacking,
            "stacker",     mkMethod("stacker"),

            "boosting",    (NewSimpleTD.DLD) NewSimpleTypedDag::boosting,
            "booBegin",    mkMethod("booBegin"),
            "booster",     (NewSimpleTD.Op) NewSimpleTypedDag::booster,
            "booEnd",      mkMethod("booEnd")
    );

    private static final EvalLib lib_general = EvalLib.mk(
            "dia",        (NewSimpleTD.Op3) NewSimpleTypedDag::dia,
            "dia0",       (NewSimpleTD.Op2) NewSimpleTypedDag::dia0,
            "split",      (NewSimpleTD.DL)  NewSimpleTypedDag::split,
            "cons",       (NewSimpleTD.OL)  MyList::cons,
            "nil",        MyList.NIL
    );

    private static final EvalLib lib_stacking = EvalLib.mk(
            "stacking",    (NewSimpleTD.Op2) NewSimpleTypedDag::stacking,
            "stacker",     mkMethod("stacker")
    );

    private static final EvalLib lib_boosting = EvalLib.mk(
            "boosting",    (NewSimpleTD.DLD) NewSimpleTypedDag::boosting,
            "booBegin",    mkMethod("booBegin"),
            "booster",     (NewSimpleTD.Op) NewSimpleTypedDag::booster,
            "booEnd",      mkMethod("booEnd")
    );

    public static AB<EvalLib,Gamma> mkLibAndGamma(JSONObject methods) {

        List<Gamma> gammas = new ArrayList<>();
        List<EvalLib> libs = new ArrayList<>();

        addAndCheck(lib_general, gamma_general, libs, gammas);

        addLibAndGamma(methods, "basic", "Dag D LD", libs, gammas);
        addLibAndGamma(methods, "preprocessing", "Dag D D", libs, gammas);
        addLibAndGamma(methods, "splitter_disjoint", "Dag D (V D (S(S n)) Disj)", libs, gammas);
        addLibAndGamma(methods, "splitter_copy", "Dag D (V D (S(S n)) Copy)", libs, gammas);
        addLibAndGamma(methods, "merger", "Dag (V LD (S(S n)) an) LD", libs, gammas);

        if (methods.getBoolean("stacking")) {
            addAndCheck(lib_stacking, gamma_stacking, libs, gammas);
        }

        if (methods.getBoolean("boosting")) {
            addAndCheck(lib_boosting, gamma_boosting, libs, gammas);
        }

        EvalLib unionLib = EvalLib.union(libs);
        Gamma unionGamma = Gamma.union(gammas);

        return AB.mk(unionLib, unionGamma);
    }

    private static void addLibAndGamma(JSONObject methods, String groupName, String typeStr,
                                       List<EvalLib> libs, List<Gamma> gammas) {
        JSONArray names = methods.getJSONArray(groupName);
        EvalLib lib = EvalLib.mk(names, NewSimpleWorkflows::mkMethod);

        Type commonType = Types.parse(typeStr);

        Gamma gamma = new Gamma(F.map(names, name -> {
            if (!(name instanceof String)) {throw new Error("symbols must be strings!");}
            return new GammaSym((String) name, commonType, false);
        }));

        addAndCheck(lib, gamma, libs, gammas);
    }

    private static void addAndCheck(EvalLib lib, Gamma gamma, List<EvalLib> libs, List<Gamma> gammas) {

        if (lib.size() != gamma.size()) {
            throw new Error("lib size and gamma size must be equal!");
        }

        for (AB<String,Type> p : gamma.getSymbols()) {
            String sym = p._1();
            if (!lib.contains(sym)) {
                throw new Error("lib does not contain symbol "+sym+", which is in the gamma!");
            }
        }

        libs.add(lib);
        gammas.add(gamma);
    }

    private static EvalCode mkMethod(String name) {
        return (leaf,eval) -> {
            Type type = leaf.getType();
            Params params = leaf.getParams();
            AA<Type> p = getDagInOutTypes(type);
            JSONObject jsonParams = params == null ? new JSONObject() : params.toJson();
            return new NewSimpleTypedDag(name, p._1(), p._2(), jsonParams, null);
        };
    }

    private static AA<Type> getDagInOutTypes(Type type) {
        if (type instanceof TypeTerm) {
            List<Type> args = ((TypeTerm) type).getArgs();
            if (args.size() == 3 && args.get(0).equals(Dag)) {
                return new AA<>(args.get(1),args.get(2));
            }
        }
        throw new Error("Type "+type+" was expected to be (Dag a b) type!");
    }


    // -- Testing ----------------------------------




    private static void test_evaluating(int k_max) {
        Checker ch = new Checker();
        EvalTester.testLib(ch, k_max, lib, gamma, goal, true, dag -> ((NewSimpleTypedDag)dag).toJson());
        ch.results();
    }

    private static void test_generating() {
        Checker ch = new Checker();

        Log.it();
        Log.it("Goal = "+goal);
        Log.it("Gamma =");
        Log.it(gamma);
        Log.it();

        Gen gen = new Gen(Gen.Opts.mkDefault(), gamma, ch);

        int max_k = 64;
        int numToGenerate = 10000;

        boolean allTreesWereStrictlyWellTyped = true;
        List<AppTree> treeExamples = new ArrayList<>();

        Log.it("Num trees to generate for each size: "+numToGenerate+"\n");

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
                        ch.fail("Tree is not strictly well-typed: "+tree+"\n"+tree.getTypeTrace().toString());
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
