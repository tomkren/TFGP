package cz.tomkren.fishtron.ugen.cache.data;

import cz.tomkren.fishtron.types.Sub;

/**Created by tom on 5. 2. 2017.*/

public class EncodedTs1Res {
    private final String s;
    private final int sub_id;
    private final int nextVarId;

    public EncodedTs1Res(String s, int sub_id, int nextVarId) {
        this.s = s;
        this.sub_id = sub_id;
        this.nextVarId = nextVarId;
    }

    public String getSym() {return s;}
    public int getSub_id() {return sub_id;}
    public int getNextVarId() {return nextVarId;}
}
