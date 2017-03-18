package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.data.GammaSym;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**  Created by user on 31. 1. 2017. */

public class Gamma {

    private final List<GammaSym> gamma;

    public Gamma(List<GammaSym> gammaSymList) {
        this.gamma = gammaSymList;
    }

    /*public Gamma(List<AB<String, Type>> gammaPairList) {
        this.gamma = F.map(gammaPairList, p -> new GammaSym(p._1(), p._2(), false));
    }*/

    private Gamma(List<Gamma> gammas, boolean checkUniqueness /*todo !!!*/) {
        int size = F.sumInt(F.map(gammas, g -> g.gamma.size()));
        gamma = new ArrayList<>(size);
        for (Gamma g : gammas) {
            gamma.addAll(g.gamma);
        }
    }

    public static Gamma union(List<Gamma> gammas) {
        return new Gamma(gammas, true);
    }

    public int size() {
        return gamma.size();
    }

    public List<AB<String, Type>> getSymbols() {
        return F.map(gamma, GammaSym::toNameTypePair);
    }

    JSONArray toJson() {
        return F.jsonMap(gamma, GammaSym::toJson);
    }

    /*public static Gamma mk(String... strs) {
        if (strs.length % 2 != 0) {throw new Error("There must be an even number of gamma strings.");}
        List<GammaSym> ret = new ArrayList<>(strs.length/2);
        for (int i = 0; i < strs.length; i+=2) {
            ret.add(new GammaSym(strs[i], Types.parse(strs[i+1]), false));
        }
        return new Gamma(ret);
    }*/

    public static Gamma mk(Object... objs) {

        List<GammaSym> ret = new ArrayList<>();

        int n = objs.length;
        int i = 0;

        while (i<n) {
            Object arg = objs[i];
            if (arg instanceof String) {

                String sym = (String) arg;

                if (i+1 >= n) {throw new Error("Arg on index "+(i+1)+" expected (because "+sym+" is a String), but missing.");}

                Object typeObj = objs[i+1];
                Type type;

                if (typeObj instanceof String) {
                    type = Types.parse((String) typeObj);
                } else if (typeObj instanceof Type) {
                    type = (Type) typeObj;
                } else {
                    throw new Error("Arg on index "+(i+1)+" must be either String or Type.");
                }

                ret.add(new GammaSym(sym, type, false));
                i += 2;

            } else if (arg instanceof GammaSym) {

                ret.add((GammaSym) arg);
                i += 1;

            } else {
                throw new Error("Arg on index "+i+" must be either String or GammaSym.");
            }
        }

        return new Gamma(ret);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (GammaSym gs : gamma) {
            sb.append(gs.toString()).append('\n');
        }
        return sb.toString();
    }
}