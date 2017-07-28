package net.fishtron.types;

import cz.tomkren.utils.AB;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/** Created by tom on 7.11.2015.*/

public interface Type {

    Type applyMiniSub(int varId, Type type);
    Type applySub(Sub sub);

    AB<Type,Integer> freshenVars(int startVarId, Sub newVars);

    default AB<Type,Integer> freshenVars(int nextVarId) {
        return freshenVars(nextVarId, new Sub());
    }

    Type skolemize(Set<Integer> idsAcc);
    Type deskolemize(Set<Integer> ids);

    default AB<Type,Set<Integer>> skolemize() {
        Set<Integer> acc = new HashSet<>();
        Type skolemizedType = skolemize(acc);
        return AB.mk(skolemizedType, acc);
    }

    void getVarIds(TreeSet<Integer> acc);
    void getSkolemIds(TreeSet<Integer> acc);

    default TreeSet<Integer> getVarIds() {
        TreeSet<Integer> acc = new TreeSet<>();
        getVarIds(acc);
        return acc;
    }

    default TreeSet<Integer> getSkolemIds() {
        TreeSet<Integer> acc = new TreeSet<>();
        getSkolemIds(acc);
        return acc;
    }

    int getNextVarId(int acc);
    int getNextVarId_onlySkolemVars(int acc);

    default int getNextVarId() {
        return getNextVarId(0);
    }

    default int getNextVarId_onlySkolemVars() {
        return getNextVarId_onlySkolemVars(0);
    }

    default boolean hasTypeVars() {
        return !getVarIds().isEmpty();
    }

    Object toJson();

}