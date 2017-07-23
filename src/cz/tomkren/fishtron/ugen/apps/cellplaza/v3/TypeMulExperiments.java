package cz.tomkren.fishtron.ugen.apps.cellplaza.v3;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeSym;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.eval.*;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.trees.Leaf;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;


import java.util.List;
import java.util.function.Function;

/** Created by tom on 29.05.2017. */

public class TypeMulExperiments {

    public static void main(String[] args) {
        Checker ch = new Checker();

        test_1(ch);

        ch.results();
    }

    private static void test_1(Checker ch) {
        int k_max = 100;
        Type goal = mkImgType(500);
        EvalTester.testLib(ch, k_max, lib, gamma, goal);
    }

    public static final Gamma gamma = Gamma.mk(
            "a1",  "Mul a 1 a",
            "1a",  "Mul 1 a a",
            "xab", "(Mul a b c) -> (Mul (T x a) b (T x c))",
            "axb", "(Mul a b c) -> (Mul a (T x b) (T x c))",

            "img", "Img a",
            "replace", "(Mul a b c) -> ((Img a) -> ((Img b) -> (Img c)))"
    );

    public static final EvalLib lib = EvalLib.mk(
            "a1",  null,
            "1a",  null,
            "xab", (Fun) x->x,
            "axb", (Fun) x->x,
            "img", new PokusImg(),
            "replace", (F3) TypeMulExperiments::fakeReplace // todo
    );

    //public static final Type goal = mkImgType(500); //Types.parse("Img (T 2 (T 2 (T 5 (T 5 5))))");

    private static final TypeSym T = new TypeSym("T");
    private static final TypeSym Img = new TypeSym("Img");



    private static Type mkImgType(int width) {
        return new TypeTerm(Img, mkTNumber(width));
    }

    private static Type mkTNumber(int n) {
        List<Integer> ps = F.factorizeInt(n);

        int last = ps.size() - 1;
        List<Integer> xs = ps.subList(0,last);
        Type z = mkTPrime(ps.get(last));

        return F.foldr(xs, z, TypeMulExperiments::mkTTimes );
    }

    private static Type mkTTimes(int p, Type acc) {
        return new TypeTerm(T, mkTPrime(p), acc);
    }

    private static Type mkTPrime(int p) {
        return new TypeSym(Integer.toString(p));
    }

    private static Object fakeReplace(Object mul, Object img_a, Object img_b) {
        if (mul != null) {throw new Error("Null mul is expected.");}
        return AB.mk(img_a, img_b);
    }

    private static int getImageWidth(Type imgType) {
        if (imgType instanceof TypeTerm) {
            List<Type> args = ((TypeTerm) imgType).getArgs();
            if (args.size() != 2) {throw new Error("imgType must have length 2. It is: "+ imgType);}
            if (!args.get(0).equals(Img)) {throw new Error("imgType must start with Img. It is: "+imgType);}
            Type width = args.get(1);
            return evalTNumber(width);
        } else {
            throw new Error("imgType must be of a form 'Img w', but it is: "+ imgType);
        }
    }

    private static int evalTNumber(Type tNumber) {
        if (tNumber instanceof TypeSym) {
            TypeSym tSym = (TypeSym) tNumber;
            try {
                return Integer.parseInt(tSym.toString());
            } catch (NumberFormatException e) {
                throw new Error("Symbol "+tSym+" is not a parsable number.");
            }
        } else if (tNumber instanceof TypeTerm) {
            List<Type> args = ((TypeTerm) tNumber).getArgs();
            if (args.size() != 3) {throw new Error("TNumber typeTerm must have length 3. It is: "+tNumber);}
            Type opType = args.get(0);
            if (opType instanceof TypeSym) {
                TypeSym op = (TypeSym) opType;
                if (!op.equals(T)) {throw new Error("TNumber typeTerm must start with TypeSym T. It is: "+tNumber);}

                int a = evalTNumber(args.get(1));
                int b = evalTNumber(args.get(2));

                return a * b;

            } else {
                throw new Error("TNumber typeTerm must start with TypeSym. It is: "+tNumber);
            }
        } else {
            throw new Error("Legal TNumber must be composed of TypeSym and TypeTerms only. It is: "+tNumber);
        }
    }


    public static class PokusImg implements EvalCode {
        @Override
        public Object evalCode(Leaf leaf, Function<AppTree, Object> evalFun) {
            return getImageWidth(leaf.getType());
        }
    }


}
