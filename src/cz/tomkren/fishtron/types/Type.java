package cz.tomkren.fishtron.types;

import cz.tomkren.utils.AB;

import java.util.Set;

/** Created by tom on 7.11.2015.*/

public interface Type {

    Type applyMiniSub(int varId, Type type);
    Type applySub(Sub sub);

    AB<Type,Integer> freshenVars(int startVarId, Sub newVars);

    void getVarIds(Set<Integer> ret);

    int getNextVarId(int acc);

    default int getNextVarId() {
        return getNextVarId(0);
    }

}