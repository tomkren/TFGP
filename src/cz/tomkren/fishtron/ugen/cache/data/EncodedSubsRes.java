package cz.tomkren.fishtron.ugen.cache.data;

import cz.tomkren.utils.F;
import org.json.JSONArray;

import java.math.BigInteger;

/** Created by user on 2. 2. 2017.*/
public class EncodedSubsRes {

    private final BigInteger num;
    private final int sub_id;
    private final int nextVarId; // TODO není zbytečný, když se posouvá? (23.7.17)

    public EncodedSubsRes(BigInteger num, int sub_id, int nextVarId) {
        this.num = num;
        this.sub_id = sub_id;
        this.nextVarId = nextVarId;
    }

    public BigInteger getNum() {return num;}
    public int getSub_id() {return sub_id;}
    public int getNextVarId() {return nextVarId;}

    public JSONArray toJson() {
        return F.arr(num.toString(), sub_id, nextVarId);
    }

    public static EncodedSubsRes fromJson(Object data) {
        JSONArray d = (JSONArray) data;
        return new EncodedSubsRes(new BigInteger(d.getString(0)), d.getInt(1), d.getInt(2));
    }
}
