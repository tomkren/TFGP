package cz.tomkren.fishtron.ugen.data;

import cz.tomkren.fishtron.types.Sub;

import java.math.BigInteger;

/** Created by tom on 2. 2. 2017. */

public class SubsRes {
    private BigInteger num;
    private final Sub sigma;
    private int nextVarId;

    public SubsRes(BigInteger num, Sub sigma, int nextVarId) {
        this.num = num;
        this.sigma = sigma;
        this.nextVarId = nextVarId;
    }

    public void setNum(BigInteger num) {this.num = num;}
    public void setNextVarId(int nextVarId) {this.nextVarId = nextVarId;}


    public BigInteger getNum() {return num;}
    public Sub getSigma() {return sigma;}
    public int getNextVarId() {return nextVarId;}

    @Override
    public String toString() {
        return "SubsRes{" +
                "num=" + num +
                ", sigma=" + sigma +
                ", nextVarId=" + nextVarId +
                '}';
    }
}
