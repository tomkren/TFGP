package cz.tomkren.fishtron.types;

import com.google.common.base.Joiner;
import cz.tomkren.fishtron.ugen.nf.MkNF;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;

import java.util.Arrays;
import java.util.List;

/** Created by tom on 7.11.2015.*/

public class Types {

    public static final TypeSym PAIR   = new TypeSym("P");
    public static final TypeSym VECTOR = new TypeSym("V");
    public static final TypeSym SUCC   = new TypeSym("S");
    public static final TypeSym ZERO   = new TypeSym("0");

    public static final TypeSym FUN_ARROW = new TypeSym("->");
    public static final TypeSym BOX_ARROW = new TypeSym("=>");

    public static boolean isSameType(Type t1, Type t2) {
        return t1.toString().equals(t2.toString());
    }

    public static boolean isRenamedType(Type t1, Type t2) {

        Type t1_var_NF = MkNF.onlyVarsNF(t1);
        Type t2_var_NF = MkNF.onlyVarsNF(t2);

        return isSameType(t1_var_NF, t2_var_NF);
    }


    public static Type mkFunType(Type argType, Type returnType) {
        return new TypeTerm(Types.FUN_ARROW, argType, returnType);
    }

    public static List<Type> fromSyntaxSugar(List<Type> ts) {
        if (ts.size() == 3 && ts.get(1).equals(FUN_ARROW)) {
            return Arrays.asList(ts.get(1), ts.get(0), ts.get(2));
        }
        return ts;
    }

    public static List<Type> toSyntaxSugar(List<Type> ts) {
        if (ts.size() == 3 && ts.get(0).equals(FUN_ARROW)) {
            return Arrays.asList(ts.get(1), ts.get(0), ts.get(2));
        }
        return ts;
    }

    public static String prettyPrint(Type t) {
        if (t instanceof TypeTerm) {
            TypeTerm tt = (TypeTerm) t;
            List<Type> args = tt.getArgs();
            if (args.size() == 3 && args.get(0).equals(FUN_ARROW)) {
                return "("+ prettyPrint(args.get(1)) +" "+ FUN_ARROW +" "+ prettyPrint(args.get(2))+")";
            } else {
                List<String> strArgs = F.map(args, Types::prettyPrint);
                return "("+ Joiner.on(' ').join(strArgs)+")";
            }
        } else {
            return t.toString();
        }
    }

    public static String prettyPrint2(Type t) {
        return prettyPrint2(t, true);
    }

    private static String prettyPrint2(Type t, boolean skipParens) {
        if (t instanceof TypeTerm) {
            TypeTerm tt = (TypeTerm) t;
            List<Type> args = tt.getArgs();
            String p1 = skipParens ? "" : "(";
            String p2 = skipParens ? "" : ")";
            if (isFunType(t)) {

                Type arg1 = args.get(1);
                Type arg2 = args.get(2);

                boolean skip1 = !isFunType(arg1);

                return p1+prettyPrint2(arg1,skip1)+" "+FUN_ARROW+" "+prettyPrint2(arg2,true)+p2;
            } else {

                List<String> strArgs = F.map(args, arg -> prettyPrint2(arg, false) );

                return p1+ Joiner.on(' ').join(strArgs)+ p2;
            }
        } else {
            return t.toString();
        }
    }

    public static boolean isFunType(Type t) {
        if (t instanceof TypeTerm) {
            List<Type> args = ((TypeTerm) t).getArgs();
            return args.size() == 3 && args.get(0).equals(FUN_ARROW);
        } else {
            return false;
        }
    }

    public static AA<Type> splitFunType(Type funType) {
        if (funType instanceof TypeTerm) {
            List<Type> args = ((TypeTerm) funType).getArgs();

            if (args.size() == 3) {

                Type arrowSym   = args.get(0);
                Type argType    = args.get(1);
                Type returnType = args.get(2);

                if (arrowSym instanceof TypeSym && arrowSym.equals(FUN_ARROW)) {

                    return new AA<>(argType, returnType);

                } else {
                    throw new Error("funType's 1st arg must be TypeSym '"+FUN_ARROW+"'; funType: "+funType);
                }
            } else {
                throw new Error("funType must have 3 parts; funType: "+funType);
            }
        } else {
            throw new Error("funType must be TypeTerm, but it is: "+funType);
        }
    }

    public static void main(String[] args) {
        Checker ch = new Checker();

        //TODO | návrh lepšího testování:
        //TODO | udělat generátor typů (asi musi fikanej aby ne furt fail) a unifikovat, když non-fail
        //TODO | tak navíc pustit na puvodních termech a otestovat rovnost


        mguTest(ch, "A x1 x2", "A x1 x2", "{}");
        mguTest(ch, "A x1 x2", "A x1 x3", "{ x2 = x3 }");
        mguTest(ch, "A x1 x3", "A x1 x2", "{ x3 = x2 }");
        mguTest(ch, "A x1 x3", "B x1 x2", "fail");
        mguTest(ch, "A x1", "B x1", "fail");
        mguTest(ch, "A (B Int x3) x2", "A (B x1 x3) x2", "{ x1 = Int }");

        mguTest(ch, "A (List x1 x3) x2", "A (List x1 x3) Book", "{ x2 = Book }");
        mguTest(ch, "List Str", "List x3", "{ x3 = Str }");
        mguTest(ch, "A (List Str)", "A (List Str)", "{}");
        mguTest(ch, "A (List x3)", "A (List x3)", "{}");
        mguTest(ch, "-> (List Int Str) x2", "-> (List x1 x3) Book", "{ x1 = Int , x2 = Book , x3 = Str }");
        mguTest(ch, "A (List Str)", "A (List x3)", "{ x3 = Str }");
        mguTest(ch, "A (List Str)", "A (List Str)", "{}");
        mguTest(ch, "(A B)", "(A x1)", "{ x1 = B }");

        mguTest(ch, "D (List Int (S  x66) ) x2", "D (List x1 (S (G x12 x15) ) ) (Book x1 x2) ", "fail");
        mguTest(ch, "D (List Int (S  x66) ) x4", "D (List x1 (S (G x12 x15) ) ) (Book x1 x2) ", "{ x1 = Int , x4 = (Book Int x2) , x66 = (G x12 x15) }");

        mguTest(ch, "D (List Int (S  x66        ) )  x4",
                "D (List x1  (S (G x12 x15) ) ) (Book x66 x2)",
                "{ x1 = Int , x4 = (Book (G x12 x15) x2) , x66 = (G x12 x15) }");


        ch.results();
    }


    public static void mguTest(Checker ch, String typeStr1, String typeStr2, String result) {

        Type t1 = parse(typeStr1);
        Type t2 = parse(typeStr2);

        ch.it("typeStr1: "+typeStr1 + "\t\t parsed: "+t1);
        ch.it("typeStr2: "+typeStr2 + "\t\t parsed: "+t2);


        Sub sub = Sub.mgu(t1, t2);

        if (result == null) {ch.it( sub );}
        else {ch.it(sub, result);}

        if (sub.isFail()) {
            ch.it("FAIL CAUSE: "+ sub.getFailMsg());
        } else {
            ch.eqStr( sub.apply(t1) , sub.apply(t2) );
            ch.eq( sub.apply(t1) , sub.apply(t2) );
        }

        ch.it("-------------------------------------------------------------------------------------------");
    }

    public static void mguTest(Checker ch, String typeStr1, String typeStr2) {
        mguTest(ch, typeStr1, typeStr2, null);
    }

    public static Type parse(String str) {
        return (new TypeParser()).parse(str);
    }


    public static List<Type> freshenVars(List<Type> ts, int startVarId) {
        AB<Type,Integer> p = new TypeTerm(ts).freshenVars(startVarId, new Sub());
        return ((TypeTerm)p._1()).getArgs();
    }


    public static int fromNat(Type type) {
        if (type instanceof TypeTerm) {
            List<Type> args = ((TypeTerm)type).getArgs();
            Type op  = args.get(0);
            Type arg = args.get(1);
            if (SUCC.equals(op)) {
                return 1 + fromNat(arg);
            } else {
                throw new Error(type +" is not a type natural number. [2]");
            }
        } else if (ZERO.equals(type)) {
            return 0;
        } else {
            throw new Error(type +" is not a type natural number. [1]");
        }
    }
}
