package cz.tomkren.fishtron.ugen.cache;

import java.math.BigInteger;

/** Created by user on 2. 2. 2017.*/
class EncodedSubsRes {

    private final BigInteger num;
    private final int encodedSub;
    private final int nextVarId;

    EncodedSubsRes(BigInteger num, int encodedSub, int nextVarId) {
        this.num = num;
        this.encodedSub = encodedSub;
        this.nextVarId = nextVarId;
    }

    BigInteger getNum() {return num;}
    int getEncodedSub() {return encodedSub;}
    int getNextVarId() {return nextVarId;}
}
