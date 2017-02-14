package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**  Created by user on 31. 1. 2017. */

public class Gamma {

    private final List<AB<String, Type>> gamma;

    private Gamma(List<AB<String, Type>> gamma) {
        this.gamma = gamma;
    }

    public List<AB<String, Type>> getSymbols() {
        return gamma;
    }

    JSONArray toJson() {
        return F.jsonMap(gamma, p -> F.arr(p._1(),p._2().toString()));
    }

    public static Gamma mk(String... strs) {
        if (strs.length % 2 != 0) {throw new Error("There must be an even number of gamma strings.");}
        List<AB<String,Type>> ret = new ArrayList<>(strs.length/2);
        for (int i = 0; i < strs.length; i+=2) {
            ret.add(new AB<>(strs[i], Types.parse(strs[i+1])));
        }
        return new Gamma(ret);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (AB<String,Type> p : gamma) {
            String s = p._1();
            Type t_s = p._2();

            sb.append(s).append(" : ").append(Types.prettyPrint2(t_s)).append('\n');

        }

        return sb.toString();
    }
}