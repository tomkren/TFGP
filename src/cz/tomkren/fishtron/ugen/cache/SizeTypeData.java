package cz.tomkren.fishtron.ugen.cache;

import cz.tomkren.utils.ABC;

import java.math.BigInteger;
import java.util.List;

/** Created by user on 18. 7. 2016. */

class SizeTypeData {

    private List<EncodedSubsRes> subsData;
    //private Integer t_nvi;

    SizeTypeData() {
        subsData = null;
        //t_nvi = null;
    }

    boolean isComputed() {
        return subsData != null;
    }

    void set(List<EncodedSubsRes> subsData/*, int t_nvi*/) {
        this.subsData = subsData;
        /*this.t_nvi = t_nvi;*/
    }

    List<EncodedSubsRes> getSubsData() {
        return subsData;
    }

    //Integer t_nvi() {return t_nvi;}

    BigInteger computeNum() {
        BigInteger sum = BigInteger.ZERO;
        for (EncodedSubsRes sd : subsData) {
            sum = sum.add(sd.getNum());
        }
        return sum;
    }

}
