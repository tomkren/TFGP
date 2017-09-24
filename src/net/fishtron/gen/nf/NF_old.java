package net.fishtron.gen.nf;

import net.fishtron.types.Sub;
import net.fishtron.types.Type;
import net.fishtron.gen.data.SubsRes;
import net.fishtron.utils.F;

import java.math.BigInteger;
import java.util.List;

/** Created by tom on 13. 9. 2016. */

public class NF_old {

    private final Type typeInNF;
    private final Sub toNF;
    private Sub fromNF;

    NF_old(Type t) {
        this(true, t);
    }

    public NF_old(boolean isNormalizationPerformed, Type t) {

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


    public Sub getFromNF() {
        if (isNormalizationPerformed() && fromNF == null) {
            fromNF = toNF.inverse();
        }
        return fromNF;
    }

    public Type getTypeInNF() {return typeInNF;}
    Sub getToNF() {return toNF;}

    public List<SubsRes> denormalize(List<SubsRes> xs) {
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

        BigInteger num = p.getNum();
        int nextVarId = p.getNextVarId(); // TODO opravdu stačí jen zkopírovat nextVarId, určitě promyslet do hloubky !!! 4.2.17: imho se musí taky prohnat fromNF
        return new SubsRes(num,sub,nextVarId);
    }

}
