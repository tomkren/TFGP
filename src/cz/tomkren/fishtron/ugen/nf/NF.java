package cz.tomkren.fishtron.ugen.nf;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.utils.F;
import cz.tomkren.utils.TODO;

import java.util.List;

/** Created by user on 3. 2. 2017. */

public class NF {

    private Renaming renaming_skol;
    private Renaming renaming_vars;
    private Type typeInNF;


    public NF(boolean isNormalizationPerformed, Type t) {
        if (isNormalizationPerformed) {

            MkNF renamings = new MkNF(t);
            renaming_skol = renamings.getRenaming_skol();
            renaming_vars = renamings.getRenaming_vars();

            typeInNF = toNF(t);

        } else {
            renaming_skol = null;
            renaming_vars = null;
            typeInNF = t;
        }
    }

    private Type toNF(Type t) {
        if (renaming_vars == null) {return t;}

        Type t1 = renaming_skol.applyAsSkolems(t);
        return renaming_vars.applyAsVars(t1);
    }

    private Type fromNF(Type t) {
        if (renaming_vars == null) {return t;}

        Type t1 = renaming_skol.applyReverseAsSkolems(t);
        return renaming_vars.applyReverseAsVars(t1);
    }

    public Type getTypeInNF() {
        return typeInNF;
    }

    public void denormalizeIf(AppTree tree) {
        if (renaming_vars != null) {
            tree.applyTypeTransform(this::fromNF);
        }
    }

    public List<SubsRes> denormalizeIf(List<SubsRes> xs) {
        if (renaming_vars == null) {return xs;}
        return F.map(xs, this::denormalizeOne);
    }

    private SubsRes denormalizeOne(SubsRes subsRes_NF) {
        Sub sigma_nf = subsRes_NF.getSigma();
        Sub sigma = new Sub();

        sigma_nf.forEach((varId_NF,tau_NF) -> {
            sigma.add(renaming_vars.applyReverse(varId_NF), fromNF(tau_NF));
        });

        throw new TODO();

        //int nextVarId = subsRes_NF.getNextVarId(); // TODO opravdu stačí jen zkopírovat nextVarId, určitě promyslet do hloubky !!! 4.2.17: imho se musí taky prohnat fromNF
        //return new SubsRes(subsRes_NF.getNum() ,sigma, nextVarId);
    }

}
