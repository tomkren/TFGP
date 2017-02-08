package cz.tomkren.fishtron.ugen.cache.data;

import cz.tomkren.fishtron.types.Sub;
import org.json.JSONObject;

/**  Created by user on 8. 2. 2017. */

public class SubData {

    private final Sub sub;
    private int numUsed;

    public SubData(Sub sub) {
        this.sub = sub;
        numUsed  = 1;
    }

    public void incrementNumUsed() {
        numUsed ++;
    }

    public Sub getSub() {return sub;}
    public int getNumUsed() {return numUsed;}

    public JSONObject toJson() {
        return sub.toJson();
    }
}
