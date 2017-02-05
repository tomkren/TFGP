package cz.tomkren.fishtron.ugen.data;

import cz.tomkren.fishtron.types.Sub;
import java.math.BigInteger;

/** Created by tom on 2. 2. 2017. */

public class PreSubsRes {
    private BigInteger num;
    private final Sub sigma;

    public PreSubsRes(BigInteger num, Sub sigma) {
        this.num = num;
        this.sigma = sigma;
    }

    public BigInteger getNum() {return num;}
    public Sub getSigma() {return sigma;}
}
