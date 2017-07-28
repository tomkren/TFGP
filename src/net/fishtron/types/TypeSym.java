package net.fishtron.types;

import cz.tomkren.utils.AB;

import java.util.Set;
import java.util.TreeSet;

/** Created by tom on 7.11.2015.*/

public class TypeSym implements Type {

    private final String sym;

    public TypeSym(String sym) {this.sym = sym;}
    public TypeSym(int sid) {this("X"+sid);}

    @Override
    public Type applyMiniSub(int varId, Type type) {return this;}

    @Override
    public Type applySub(Sub sub) {return this;}

    @Override
    public AB<Type, Integer> freshenVars(int startVarId, Sub newVars) {
        return new AB<>(this,startVarId);
    }

    @Override
    public Type skolemize(Set<Integer> idsAcc) {
        return this;
    }

    @Override
    public Type deskolemize(Set<Integer> ids) {

        Integer sid = getSkolemId();

        if (sid == null || !ids.contains(sid)) {
            return this;
        } else {
            return new TypeVar(sid);
        }

        /*
        if (sym.charAt(0) == 'X') {
            try {
                int n = Integer.parseInt(sym.substring(1));
                return new TypeVar(n);
            } catch (NumberFormatException e) {
                return this;
            }
        }
        return this;
        */
    }

    public Integer getSkolemId() {
        if (sym.charAt(0) == 'X') {
            try {
                return Integer.parseInt(sym.substring(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public int getNextVarId(int acc) {
        Integer sid = getSkolemId();
        return sid == null ? acc : Math.max(acc,sid+1);
    }

    @Override
    public int getNextVarId_onlySkolemVars(int acc) {
        return getNextVarId(acc);
    }

    @Override
    public void getVarIds(TreeSet<Integer> ret) {}

    @Override
    public void getSkolemIds(TreeSet<Integer> acc) {
        Integer sid = getSkolemId();
        if (sid != null) {
            acc.add(sid);
        }
    }

    @Override public String toString() {return sym;}
    @Override public Object toJson()   {return sym;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeSym typeSym = (TypeSym) o;

        if (sym != null ? !sym.equals(typeSym.sym) : typeSym.sym != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sym != null ? sym.hashCode() : 0;
    }

}