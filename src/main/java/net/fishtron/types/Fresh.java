package net.fishtron.types;

import net.fishtron.utils.AB;

/** Created by user on 2. 2. 2017. */

public class Fresh {
    private final Type freshType;
    private final int nextVarId;

    public Fresh(Type typeToFresh, Type typeToAvoid, int n) {
        int n1 = typeToAvoid.getNextVarId(n);
        int n2 = typeToFresh.getNextVarId(n1);
        AB<Type,Integer> res = typeToFresh.freshenVars(n2);
        freshType = res._1();
        nextVarId = res._2();
    }

    public Type getFreshType() {return freshType;}
    public int getNextVarId() {return nextVarId;}
}
