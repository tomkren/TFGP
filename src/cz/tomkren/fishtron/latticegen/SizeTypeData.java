package cz.tomkren.fishtron.latticegen;

import cz.tomkren.utils.ABC;

import java.math.BigInteger;
import java.util.List;

/** Created by user on 18. 7. 2016. */

class SizeTypeData {

    private List<ABC<BigInteger,Integer,Integer>> subsData;
    private Integer nextVarId_input;

    SizeTypeData() {
        subsData = null;
        nextVarId_input = null;
    }

    boolean isComputed() {
        return subsData != null;
    }

    void set(List<ABC<BigInteger,Integer,Integer>> subsData, int nextVarId_input) {
        this.subsData = subsData;
        this.nextVarId_input = nextVarId_input;
    }

    List<ABC<BigInteger,Integer,Integer>> getSubsData() {
        return subsData;
    }

    Integer getNextVarId_input() {
        return nextVarId_input;
    }

    BigInteger computeNum() {
        BigInteger sum = BigInteger.ZERO;
        for (ABC<BigInteger,Integer,Integer> sd : subsData) {
            sum = sum.add(sd._1());
        }
        return sum;
    }

}
