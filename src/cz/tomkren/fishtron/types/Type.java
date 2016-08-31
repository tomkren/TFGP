package cz.tomkren.fishtron.types;

import cz.tomkren.utils.AB;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/** Created by tom on 7.11.2015.*/

public interface Type {

    Type applyMiniSub(int varId, Type type);
    Type applySub(Sub sub);

    AB<Type,Integer> freshenVars(int startVarId, Sub newVars);

    Type skolemize(Set<Integer> idsAcc);
    Type deskolemize(Set<Integer> ids);

    default AB<Type,Set<Integer>> skolemize() {
        Set<Integer> acc = new HashSet<>();
        Type skolemizedType = skolemize(acc);
        return AB.mk(skolemizedType, acc);
    }

    void getVarIds(Set<Integer> acc);

    int getNextVarId(int acc);

    default int getNextVarId() {
        return getNextVarId(0);
    }

    default Set<Integer> getVarIds() {
        Set<Integer> acc = new TreeSet<>();
        getVarIds(acc);
        return acc;
    }

    Object toJson();

}