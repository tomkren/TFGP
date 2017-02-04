package cz.tomkren.fishtron.ugen.cache;

import java.math.BigInteger;

/** Created by user on 2. 2. 2017.*/
class EncodedSubsRes {

    private final BigInteger num;
    private final int sub_id;
    private final int nextVarId; // todo krásné by bylo kdyby to tu nebylo

    EncodedSubsRes(BigInteger num, int sub_id, int nextVarId) {
        this.num = num;
        this.sub_id = sub_id;
        this.nextVarId = nextVarId;
    }

    BigInteger getNum() {return num;}
    int getSub_id() {return sub_id;}
    int getNextVarId() {return nextVarId;}
}
