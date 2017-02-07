package cz.tomkren.fishtron.ugen.tests;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.Mover;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.PreSubsRes;
import cz.tomkren.fishtron.ugen.data.PreTs1Res;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.fishtron.ugen.data.TsRes;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Created by user on 1. 2. 2017.*/

class StaticGen {

    private static List<TsRes> ts_1(Gamma gamma, Type t, int n) {
        List<PreTs1Res> ts1_results_unmoved = Gen.ts1_static(gamma, t, n);
        List<Ts1Res> ts1_results = Mover.movePreTs1Results(t,n,ts1_results_unmoved);
        return F.map(ts1_results, r -> r.toTsRes(t));
    }

    private static List<SubsRes> subs_1(Gamma gamma, Type t, int n) {
        List<PreTs1Res> ts1_results_unmoved = Gen.ts1_static(gamma, t, n);
        List<PreSubsRes> subs_results_unmoved_unpacked = F.map(ts1_results_unmoved, PreTs1Res::toPreSubsRes);
        return Gen.pack(t, n, subs_results_unmoved_unpacked);
    }

    private static List<TsRes> ts_ij(Gamma gamma, int i, int j, Type t, int n) {
        List<TsRes> ret = new ArrayList<>();
        AB<TypeVar,Integer> res_alpha = Gen.newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int n1 = res_alpha._2();
        Type t_F = Types.mkFunType(alpha, t);
        for (TsRes res_F : ts(gamma, i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);
            for (TsRes res_X : ts(gamma, j, t_X, res_F.getNextVarId())) {
                Sub sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                AppTree tree_FX = AppTree.mk(res_F.getTree(), res_X.getTree(), sigma_FX.apply(t));
                ret.add(new TsRes(tree_FX, sigma_FX, res_X.getNextVarId()));
            }
        }
        return ret;
    }

    private static List<PreSubsRes> subs_ij(Gamma gamma, int i, int j, Type t, int n) {
        List<PreSubsRes> ret = new ArrayList<>();

        AB<TypeVar,Integer> res_alpha = Gen.newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int n1 = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (SubsRes res_F : subs(gamma, i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);

            for (SubsRes res_X : subs(gamma, j, t_X, res_F.getNextVarId())) {
                BigInteger num_FX = res_F.getNum().multiply(res_X.getNum());
                Sub sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                ret.add(new PreSubsRes(num_FX, sigma_FX /*, res_X.getNextVarId()*/));
            }
        }
        //zde neni potřeba packovat, packovat stačí subs
        return ret; //pack(ret);
    }


    static List<TsRes> ts(Gamma gamma, int k, Type t, int n) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}
        else if (k == 1) {return ts_1(gamma, t, n);}
        else {
            List<TsRes> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {ret.addAll(ts_ij(gamma, i, k-i, t, n));}
            return ret;
        }
    }

    static List<SubsRes> subs(Gamma gamma, int k, Type t, int n) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return subs_1(gamma, t, n);
        } else {
            List<PreSubsRes> acc = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                acc.addAll(subs_ij(gamma, i, k - i, t, n));
            }
            return Gen.pack(t, n, acc);
        }
    }

}
