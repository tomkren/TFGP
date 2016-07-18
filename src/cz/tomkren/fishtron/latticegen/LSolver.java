package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;

/** Created by tom on 18. 7. 2016.*/

public class LSolver {

    public static AB<Type,Sub> normalize(Type t) {
        Sub t2nf = new Sub();
        Type nf = t.freshenVars(0, t2nf)._1();
        Sub nf2t = t2nf.inverse();
        if (nf2t.isFail()) {throw new Error("Unable to construct inverse: "+nf2t.getFailMsg());}
        return new AB<>(nf,nf2t);
    }


    public static void main(String[] args) {
        Checker ch = new Checker();

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


        ch.results();
    }

    private static void checkNormalisation(Checker ch, Type t) {
        AB<Type,Sub> p = normalize(t);
        Type nf  = p._1();
        Sub nf2t = p._2();

        ch.it(nf2t.apply(nf),t.toString());
        ch.it(p);
        ch.it("");
    }
}
