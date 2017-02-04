package cz.tomkren.fishtron.ugen.cache.data;

import java.math.BigInteger;

/** Created by user on 2. 2. 2017.*/
public class EncodedSubsRes {

    private final BigInteger num;
    private final int sub_id;
    //private final int nextVarId; // to-do krásné by bylo kdyby to tu nebylo

    public EncodedSubsRes(BigInteger num, int sub_id/*, int nextVarId*/) {
        this.num = num;
        this.sub_id = sub_id;
        //this.nextVarId = nextVarId;
    }

    public BigInteger getNum() {return num;}
    public int getSub_id() {return sub_id;}
    //int getNextVarId() {return nextVarId;}
}
