package cz.tomkren.fishtron.ugen;

import net.fishtron.types.Sub;
import net.fishtron.types.Type;
import net.fishtron.types.TypeVar;
import cz.tomkren.fishtron.ugen.data.PreSubsRes;
import cz.tomkren.fishtron.ugen.data.PreTs1Res;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.fishtron.ugen.data.TsRes;
import cz.tomkren.fishtron.ugen.trees.AppTree;
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


    // TODO refactorovoat prekrejvajici se kod tech dovou do jedny
    private AB<AB<Sub,Integer>,AppTree> moveSubAndTree(Sub sub, AppTree appTree) {
        TreeSet<Integer> codomainVarIds = sub.getCodomainVarIds();

        Sub deltaSub = new Sub();
        int nextVarId = tnvi_n;

        for (Integer varId : codomainVarIds) {
            if (varId >= tnvi_0) {
                deltaSub.add(varId, new TypeVar(nextVarId));
                nextVarId ++;
            }
        }

        AppTree movedTree = appTree.applySub_new(deltaSub);

        Sub movedSub = Sub.dot(deltaSub, sub).restrict(t);
        return AB.mk(AB.mk(movedSub, nextVarId), movedTree);
    }



    private boolean isMoveNeeded() {
        if (tnvi_n < tnvi_0) {throw new Error("Assert failed: tnvi_n < tnvi_0.");}
        return tnvi_n > tnvi_0;
    }

    private Ts1Res movePreTs1Res(PreTs1Res preRes)    {return new Ts1Res(preRes.getSym(), moveSub(preRes.getSigma()));}
    private Ts1Res moveTs1Res(Ts1Res res)             {return new Ts1Res(res.getSym(), moveSub(res.getSigma()));}
    private SubsRes movePreSubsRes(PreSubsRes preRes) {return new SubsRes(preRes.getNum(), moveSub(preRes.getSigma()));}
    private SubsRes moveSubsRes(SubsRes res)          {return new SubsRes(res.getNum(), moveSub(res.getSigma()));}
    private TsRes moveTsRes(TsRes res) {
        AB<AB<Sub,Integer>,AppTree> mr = moveSubAndTree(res.getSigma(), res.getTree());
        return new TsRes(mr._2(), mr._1());
    }


    public static List<Ts1Res> movePreTs1Results(Type t, int n, List<PreTs1Res> ts1results_unmoved) {
        Mover mover = new Mover(t, n);
        return F.map(ts1results_unmoved, mover::movePreTs1Res);
    }

    static List<SubsRes> movePreSubsResults(Type t, int n, List<PreSubsRes> results_unmoved) {
        Mover mover = new Mover(t, n);
        return F.map(results_unmoved, mover::movePreSubsRes);
    }

    public static List<TsRes> moveTsResults(Type t, int n, List<TsRes> results_unmoved) {
        Mover mover = new Mover(t, n);
        return F.map(results_unmoved, mover::moveTsRes);
    }

    // Intended for moving results generated for old_n = 0, now needed for n.
    public static List<Ts1Res> moveTs1Results_0(Type t, int n, List<Ts1Res> ts1results_0) {
        Mover mover = new Mover(t, n);
        return mover.isMoveNeeded() ? F.map(ts1results_0, mover::moveTs1Res) : ts1results_0;
    }

    // Intended for moving results generated for old_n = 0, now needed for n.
    public static List<SubsRes> moveSubsResults_0(Type t, int n, List<SubsRes> results_0) {
        Mover mover = new Mover(t, n);
        return mover.isMoveNeeded() ? F.map(results_0, mover::moveSubsRes) : results_0;
    }

}
