package cz.tomkren.fishtron.latticegen;

import cz.tomkren.utils.AB;

import java.math.BigInteger;
import java.util.List;

/** Created by user on 18. 7. 2016. */

class SizeTypeData {

    private List<AB<BigInteger,Integer>> subsData;

    SizeTypeData() {
        subsData = null;
    }

    boolean isComputed() {
        return subsData != null;
    }

    void setSubsData(List<AB<BigInteger,Integer>> subsData) {
        this.subsData = subsData;
    }

    List<AB<BigInteger,Integer>> getSubsData() {
        return subsData;
    }
}
