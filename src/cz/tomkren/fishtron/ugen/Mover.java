package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.ugen.data.PreSubsRes;
import cz.tomkren.fishtron.ugen.data.PreTs1Res;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import java.util.List;
import java.util.TreeSet;

/**  Created by Tomáš Křen on 5.2.2017. */

public class Mover {

    private final int tnvi_0;
    private final int tnvi_n;
    private final Type t;

    private Mover(Type t, int n) {
        this.t = t;
        tnvi_0 = t.getNextVarId(0);
        tnvi_n = Math.max(tnvi_0, n);
    }

    private AB<Sub,Integer> moveSub(Sub sub) {
        TreeSet<Integer> codomainVarIds = sub.getCodomainVarIds();

        Sub deltaSub = new Sub();
        int nextVarId = tnvi_n;

        for (Integer varId : codomainVarIds) {
            if (varId >= tnvi_0) {
                deltaSub.add(varId, new TypeVar(nextVarId));
                nextVarId ++;
            }
        }

        Sub movedSub = Sub.dot(deltaSub, sub).restrict(t);
        return AB.mk(movedSub, nextVarId);
    }

    private Ts1Res moveTs1Res(PreTs1Res preRes) {
        Sub sub = preRes.getSigma();
        AB<Sub,Integer> moveSubRes = moveSub(sub);
        return new Ts1Res(preRes.getSym(), moveSubRes._1(), moveSubRes._2());
    }

    private SubsRes moveSubsRes(PreSubsRes preRes) {
        Sub sub = preRes.getSigma();
        AB<Sub, Integer> moveSubRes = moveSub(sub);
        return new SubsRes(preRes.getNum(), moveSubRes._1(), moveSubRes._2());
    }

    public static List<Ts1Res> moveTs1Results(Type t, int n, List<PreTs1Res> ts1results_unmoved) {
        Mover mover = new Mover(t, n);
        return F.map(ts1results_unmoved, mover::moveTs1Res);
    }

    public static List<SubsRes> moveSubsResults(Type t, int n, List<PreSubsRes> results_unmoved) {
        Mover mover = new Mover(t, n);
        return F.map(results_unmoved, mover::moveSubsRes);
    }

}
