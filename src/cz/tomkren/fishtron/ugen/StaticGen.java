package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Created by user on 1. 2. 2017.*/

public class StaticGen {

    private static List<SubsRes> subs_1(Gamma gamma, Type t, int nextVarId) {
        List<Ts1Res> ts1_results = Gen.ts_1(gamma, t, nextVarId); // todo asi lepší předávat ts1_result pač ho chcem počítat jen jednou
        List<SubsRes> unpackedResults = F.map(ts1_results, Ts1Res::toSubsRes);
        return Gen.pack(unpackedResults);
    }

    private static List<SubsRes> subs_ij(Gamma gamma, int i, int j, Type t, int n) {
        List<SubsRes> ret = new ArrayList<>();

        AB<TypeVar,Integer> res_alpha = Gen.newVar(t, n);
        TypeVar alpha = res_alpha._1();
        int     n1    = res_alpha._2();

        Type t_F = Types.mkFunType(alpha, t);

        for (SubsRes res_F : subs_k(gamma, i, t_F, n1)) {
            Type t_X = res_F.getSigma().apply(alpha);

            for (SubsRes res_X : subs_k(gamma, j, t_X, res_F.getNextVarId())) {
                BigInteger num_FX = res_F.getNum().multiply(res_X.getNum());
                Sub      sigma_FX = Sub.dot(res_X.getSigma(), res_F.getSigma()).restrict(t);
                ret.add(new SubsRes(num_FX, sigma_FX, res_X.getNextVarId()));
            }
        }

        //todo zde neni potřeba packovat, packovat stačí subs_k, rozmyslet co je efektivnější
        return ret; //pack(ret);
    }


    private static List<SubsRes> subs_k(Gamma gamma, int k, Type t, int nextVarId) {
        if (k < 1) {
            throw new Error("k must be > 0, it is "+k);
        } else if (k == 1) {
            return subs_1(gamma, t, nextVarId);
        } else {
            List<SubsRes> ret = new ArrayList<>();
            for (int i = 1; i < k; i++) {
                ret.addAll(subs_ij(gamma, i, k - i, t, nextVarId));
            }
            return Gen.pack(ret);
        }
    }

}
