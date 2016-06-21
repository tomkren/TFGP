package cz.tomkren.fishtron.types;

import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Created by tom on 7.11.2015.*/

public class TypeTerm implements Type {

    private final List<Type> args;

    public TypeTerm(List<Type> ts) {
        this.args = ts;
    }

    public TypeTerm(Type... types) {
        this.args = Arrays.asList(types);
    }

    // TODO následující dvě by šli úsporněji, gerujic novej jen když třeba
    @Override
    public Type applyMiniSub(int varId, Type type) {
        return new TypeTerm( F.map(args, t -> t.applyMiniSub(varId, type) ) );
    }

    public List<Type> getArgs() {
        return args;
    }

    @Override
    public Type applySub(Sub sub) {
        return new TypeTerm( F.map(args, t -> t.applySub(sub)) );
    }

    @Override
    public AB<Type, Integer> freshenVars(int startVarId, Sub newVars) {
        List<Type> args2 = new ArrayList<>(args.size());
        int nextId = startVarId;
        for (Type t : args) {
            AB<Type,Integer> p = t.freshenVars(nextId, newVars);
            args2.add(p._1());
            nextId = p._2();
        }
        return new AB<>(new TypeTerm(args2), nextId);
    }

    @Override
    public int getNextVarId(int acc) {
        for (Type arg : args) {
            acc = arg.getNextVarId(acc);
        }
        return acc;
    }

    @Override
    public void getVarIds(Set<Integer> ret) {
        args.forEach(t->t.getVarIds(ret));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append('(');

        for (Type t : args) {
            sb.append(t).append(' ');
        }

        sb.deleteCharAt(sb.length()-1);

        sb.append(')');

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeTerm typeTerm = (TypeTerm) o;

        return !(args != null ? !args.equals(typeTerm.args) : typeTerm.args != null);
    }

    @Override
    public int hashCode() {
        return args != null ? args.hashCode() : 0;
    }


}