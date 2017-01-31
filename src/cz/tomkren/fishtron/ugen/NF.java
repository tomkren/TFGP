package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.utils.ABC;
import cz.tomkren.utils.F;

import java.util.List;

/** Created by tom on 13. 9. 2016. */

class NF {

    private final Type typeInNF;
    private final Sub toNF;
    private Sub fromNF;

    NF(Type t) {
        this(true, t);
    }

    NF(boolean isNormalizationPerformed, Type t) {

        if (isNormalizationPerformed) {
            Sub t2nf = new Sub();

            // Skolemovský symboly zatim nenormalizujem, ale musíme zajistit aby se normalizací neklešly..
            int startVarId = t.getNextVarId_onlySkolemVars(); //TODO časem by chtělo efektivnějc, tzn normalizovat i skolemčata..
            typeInNF = t.freshenVars(startVarId, t2nf)._1();

            toNF = t2nf.toRenaming(t);
            fromNF = null;

            if (toNF.isFail()) {
                throw new Error("Unable to construct renaming 'toNF': "+toNF.getFailMsg());
            }
        } else {
            typeInNF = t;
            toNF     = null;
            fromNF   = null;
        }
    }

    private boolean isNormalizationPerformed() {
        return toNF != null;
    }


    Sub getFromNF() {
        if (isNormalizationPerformed() && fromNF == null) {
            fromNF = toNF.inverse();
        }
        return fromNF;
    }

    Type getTypeInNF() {return typeInNF;}
    Sub getToNF() {return toNF;}

    <A> List<ABC<A,Sub,Integer>> denormalize(List<ABC<A,Sub,Integer>> xs) {
        if (isNormalizationPerformed()) {
            getFromNF(); // ensures existence of fromNF
            return F.map(xs, this::denormalize_internal);
        } else {
            return xs;
        }
    }

    private <A> ABC<A,Sub,Integer> denormalize_internal(ABC<A,Sub,Integer> p) {

        Sub sub_nf = p._2();
        Sub s1 = Sub.dot(sub_nf,toNF);
        Sub sub = Sub.dot(fromNF, s1);

        A a = p._1();
        int nextVarId = p._3();
        return ABC.mk(a,sub,nextVarId); // TODO opravdu stačí jen zkopírovat nextVarId, určitě promyslet do hloubky !!!
    }

}
