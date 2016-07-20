package cz.tomkren.fishtron.latticegen;

import cz.tomkren.utils.AB;

import java.math.BigInteger;
import java.util.List;

/** Created by user on 18. 7. 2016. */

public class SizeData {

    private List<AB<Integer,BigInteger>> subsData;

    public SizeData() {
        subsData = null;
    }

    boolean isComputed() {
        return subsData != null;
    }

    public void setSubsData(List<AB<Integer, BigInteger>> subsData) {
        this.subsData = subsData;
    }

    public List<AB<Integer, BigInteger>> getSubsData() {
        return subsData;
    }
}
