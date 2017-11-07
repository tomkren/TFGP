package net.fishtron.gen.cache.data;

import net.fishtron.types.Sub;
import net.fishtron.utils.F;
import org.json.JSONArray;

/**  Created by user on 8. 2. 2017. */

public class SubData {

    private final Sub sub;
    private int numUsed;

    public SubData(Sub sub) {
        this(sub, 1);
    }

    private SubData(Sub sub, int numUsed) {
        this.sub = sub;
        this.numUsed = numUsed;
    }

    public void incrementNumUsed() {
        numUsed ++;
    }

    public Sub getSub() {return sub;}
    public int getNumUsed() {return numUsed;}


    public JSONArray toJson() {
        return F.arr(sub.toJson(), numUsed);
    }

    public static SubData fromJson(Object json) {
        JSONArray data = (JSONArray) json;
        Sub newSub = new Sub(data.getJSONObject(0));
        return new SubData(newSub, data.getInt(1));
    }
}
