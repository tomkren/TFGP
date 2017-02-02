package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.utils.F;

import java.math.BigInteger;
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
            int nextSkolemId = t.getNextVarId_onlySkolemVars(); //TODO časem by chtělo efektivnějc, tzn normalizovat i skolemčata..
            typeInNF = t.freshenVars(nextSkolemId, t2nf)._1();

            toNF = t2nf.toRenaming();
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

    List<SubsRes> denormalize(List<SubsRes> xs) {
        if (isNormalizationPerformed()) {
            getFromNF(); // ensures existence of fromNF
            return F.map(xs, this::denormalize_internal);
        } else {
            return xs;
        }
    }

    private SubsRes denormalize_internal(SubsRes p) {

        Sub sub_nf = p.getSigma();
        Sub s1 = Sub.dot(sub_nf,toNF);
        Sub sub = Sub.dot(fromNF, s1);

        BigInteger a = p.getNum();
        int nextVarId = p.getNextVarId();
        return new SubsRes(a,sub,nextVarId); // TODO opravdu stačí jen zkopírovat nextVarId, určitě promyslet do hloubky !!!
    }

}
