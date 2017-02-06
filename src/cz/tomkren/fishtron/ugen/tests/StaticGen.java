package cz.tomkren.fishtron.ugen.tests;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.fishtron.ugen.data.TsRes;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import cz.tomkren.utils.TODO;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Created by user on 1. 2. 2017.*/

class StaticGen {

    private static List<TsRes> ts_1(Gamma gamma, Type t, int n) {
        List<Ts1Res> ts1_results = Gen.ts1_static(gamma, t, n);
        return F.map(ts1_results, r -> r.toTsRes(t));
    }

    private static List<SubsRes> subs_1(Gamma gamma, Type t, int n) {
        List<Ts1Res> ts1_results = Gen.ts1_static(gamma, t, n);
        List<SubsRes> unpackedResults = F.map(ts1_results, Ts1Res::toSubsRes);
        return Gen.pack(unpackedResults);
    }

    private static List<TsRes> ts_ij(Gamma gamma, int i, int j, Type t, int n) {
        List<TsRes> ret = new ArrayList<>();
        AB<TypeVar,Integer> res_alpha = Gen.newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int n1 = res_alpha._2();
        Type t_F = Types.mkFunType(alpha, t);
        for (TsRes res_F : ts_k(gamma, i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);
            for (TsRes res_X : ts_k(gamma, j, t_X, res_F.getNextVarId())) {
                Sub sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                AppTree tree_FX = AppTree.mk(res_F.getTree(), res_X.getTree(), sigma_FX.apply(t));
                ret.add(new TsRes(tree_FX, sigma_FX, res_X.getNextVarId()));
            }
        }
        return ret;
    }

    private static List<SubsRes> subs_ij(Gamma gamma, int i, int j, Type t, int n) {
        List<SubsRes> ret = new ArrayList<>();

        AB<TypeVar,Integer> res_alpha = Gen.newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int n1 = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (SubsRes res_F : subs_k(gamma, i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);

            for (SubsRes res_X : subs_k(gamma, j, t_X, res_F.getNextVarId())) {
                BigInteger num_FX = res_F.getNum().multiply(res_X.getNum());
                Sub sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                ret.add(new SubsRes(num_FX, sigma_FX, res_X.getNextVarId()));
            }
        }
        //zde neni potřeba packovat, packovat stačí subs_k
        return ret; //pack(ret);
    }


    static List<TsRes> ts_k(Gamma gamma, int k, Type t, int n) {
        if (k < 1) {throw new Error("k must be > 0, it is "+k);}
        else if (k == 1) {return ts_1(gamma, t, n);}
        else {
            List<TsRes> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {ret.addAll(ts_ij(gamma, i, k-i, t, n));}
            return ret;
        }
    }

    static List<SubsRes> subs_k(Gamma gamma, int k, Type t, int n) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return subs_1(gamma, t, n);
        } else {
            List<SubsRes> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                ret.addAll(subs_ij(gamma, i, k - i, t, n));
            }
            return Gen.pack(ret);
        }
    }

}
