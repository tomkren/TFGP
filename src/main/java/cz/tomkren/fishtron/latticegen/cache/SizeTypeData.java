package cz.tomkren.fishtron.latticegen.cache;

import net.fishtron.utils.ABC;

import java.math.BigInteger;
import java.util.List;

/** Created by user on 18. 7. 2016. */

class SizeTypeData {

    private List<ABC<BigInteger,Integer,Integer>> subsData;
    private Integer t_nvi;

    SizeTypeData() {
        subsData = null;
        t_nvi = null;
    }

    boolean isComputed() {
        return subsData != null;
    }

    void set(List<ABC<BigInteger,Integer,Integer>> subsData, int t_nvi) {
        this.subsData = subsData;
        this.t_nvi = t_nvi;
    }

    List<ABC<BigInteger,Integer,Integer>> getSubsData() {
        return subsData;
    }

    Integer t_nvi() {
        return t_nvi;
    }

    BigInteger computeNum() {
        BigInteger sum = BigInteger.ZERO;
        for (ABC<BigInteger,Integer,Integer> sd : subsData) {
            sum = sum.add(sd._1());
        }
        return sum;
    }

}
