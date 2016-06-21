package cz.tomkren.fishtron.types;

import cz.tomkren.utils.AB;

import java.util.Set;

/** Created by tom on 7.11.2015.*/

public class TypeVar implements Type {

    private final int id;

    public TypeVar(int id) {
        this.id = id;
    }

    @Override
    public Type applyMiniSub(int varId, Type type) {
        return id == varId ? type : this;
    }
    @Override
    public Type applySub(Sub sub) {
        Type t = sub.get(id);
        return t == null ? this : t;
    }

    @Override
    public AB<Type, Integer> freshenVars(int nextVarId, Sub newVars) {
        TypeVar newVar = (TypeVar) newVars.get(id);
        if (newVar == null) {
            newVar = new TypeVar(nextVarId);
            newVars.add(id,newVar);
            nextVarId++;
        }
        return new AB<>(newVar,nextVarId);
    }

    @Override
    public int getNextVarId(int acc) {
        return Math.max(acc,id+1);
    }

    @Override
    public void getVarIds(Set<Integer> ret) {
        ret.add(id);
    }

    @Override
    public String toString() {
        return "x"+id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeVar typeVar = (TypeVar) o;

        if (id != typeVar.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
