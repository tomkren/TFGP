package cz.tomkren.fishtron.ugen.nf;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.utils.F;

import java.util.List;
import java.util.Map;

/** Created by user on 3. 2. 2017. */

public class NF {

    private Renaming renaming_skol;
    private Renaming renaming_vars;
    private Type typeInNF;
    private int t_nvi;


    public NF(boolean isNormalizationPerformed, Type t) {
        if (isNormalizationPerformed) {

            MkNF renamings = new MkNF(t);
            renaming_skol = renamings.getRenaming_skol();
            renaming_vars = renamings.getRenaming_vars();

            typeInNF = toNF(t);
            t_nvi = t.getNextVarId();

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

    public List<SubsRes> denormalizeIf(List<SubsRes> results_NF, int input_nvi) {
        if (renaming_vars == null) {return results_NF;}
        return F.map(results_NF, r -> denormalizeOne(r, input_nvi));
    }

    private SubsRes denormalizeOne(SubsRes subsRes_NF, int input_nvi) {
        Sub sigma_nf = subsRes_NF.getSigma();
        Sub sigma = new Sub();

        int nextVarId = Math.max(t_nvi, input_nvi);

        for (Map.Entry<Integer,Type> e : sigma_nf.getTable().entrySet()) {
            int varId_NF = e.getKey();
            Type tau_NF = e.getValue();

            int varId = renaming_vars.applyReverse(varId_NF);
            Type tau = fromNF(tau_NF);

            sigma.add(varId, tau);

            // nemusím protože to je obsažený už v t_nvi:
            // if (varId > nextVarId) {nextVarId = varId;}

            int tau_nvi = tau.getNextVarId();
            if (tau_nvi > nextVarId) {
                nextVarId = tau_nvi;
            }
        }

        return new SubsRes(subsRes_NF.getNum() ,sigma, nextVarId);
    }

}
