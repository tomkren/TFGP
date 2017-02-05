package cz.tomkren.fishtron.ugen.nf;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeSym;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.utils.F;

import java.util.*;

/** Created by tom on 2. 2. 2017.*/

class Renaming {

    private TreeMap<Integer,Integer> table;
    private TreeMap<Integer,Integer> reverseTable;

    Renaming(TreeMap<Integer,Integer> tab) {

        table        = new TreeMap<>();
        reverseTable = new TreeMap<>();

        //Set<Integer> domTabu = new HashSet<>();
        //Set<Integer> codomTabu = new HashSet<>();

        List<Integer> domTodo = new ArrayList<>();
        List<Integer> codomTodo = new ArrayList<>();

        for (Map.Entry<Integer,Integer> e : tab.entrySet()) {
            int var1 = e.getKey();
            int var2 = e.getValue();
            if (var1 != var2) {
                //domTabu.add(var1);
                //codomTabu.add(var2);
                putBothWays(var1, var2);
            }
        }

        for (Map.Entry<Integer,Integer> e : table.entrySet()) {
            int var1 = e.getKey();
            int var2 = e.getValue();
            if (table.containsKey(var2) /*domTabu.contains(var2)*/) {
                if (!reverseTable.containsKey(var1) /*reverseTable.get(var1) == null*/) {
                    codomTodo.add(var1);
                }
            } else if (reverseTable.containsKey(var1) /*codomTabu.contains(var1)*/) {
                if (!table.containsKey(var2)/*table.get(var2) == null*/) {
                    domTodo.add(var2);
                }
            } else {
                putBothWays(var2, var1);
            }
        }

        if (domTodo.size() != codomTodo.size()) {
            throw new Error("Renaming fail: dom&codom todo lists must have equal size, but: "+domTodo+" vs "+codomTodo);
        }

        for (int i = 0; i < domTodo.size(); i++) {
            int fromId = domTodo.get(i);
            int toId = codomTodo.get(i);
            putBothWays(fromId, toId);
        }
    }

    private void putBothWays(int var1, int var2) {
        safePut(table, var1, var2);
        safePut(reverseTable, var2, var1);
    }

    private void safePut(TreeMap<Integer,Integer> t, int key, int val) {
        if (t.get(key) == null) {
            t.put(key, val);
        } else {
            throw new Error("safePut fail: x"+val+" is more times in dom or codom!");
        }
    }


    int applyReverse(int varId) {
        Integer ret = reverseTable.get(varId);
        return (ret == null) ? varId : ret;
    }

    Type applyAsVars(Type t) {
        return applyAsVars(table, t);
    }

    Type applyReverseAsVars(Type t) {
        return applyAsVars(reverseTable, t);
    }

    Type applyAsSkolems(Type t) {
        return applyAsSkolems(table, t);
    }

    Type applyReverseAsSkolems(Type t) {
        return applyAsSkolems(reverseTable, t);
    }

    private static Type applyAsVars(TreeMap<Integer,Integer> tab, Type t) {
        TreeMap<Integer,TypeVar> varTab = new TreeMap<>();
        for (Map.Entry<Integer,Integer> e : tab.entrySet()) {
            varTab.put(e.getKey(), new TypeVar(e.getValue()));
        }
        return applyVarTab(varTab, t);
    }

    private static Type applyAsSkolems(TreeMap<Integer,Integer> tab, Type t) {
        TreeMap<Integer,TypeSym> skolemTab = new TreeMap<>();
        for (Map.Entry<Integer,Integer> e : tab.entrySet()) {
            skolemTab.put(e.getKey(), new TypeSym(e.getValue()));
        }
        return applySkolemTab(skolemTab, t);
    }


    private static Type applyVarTab(TreeMap<Integer,TypeVar> varTab, Type t) {
        if (t instanceof TypeSym) {
            return t;
        } else if (t instanceof TypeVar) {
            TypeVar var = (TypeVar) t;
            TypeVar newVar = varTab.get(var.getId());
            return (newVar == null) ? var : newVar;
        } else if (t instanceof TypeTerm) {
            TypeTerm term = (TypeTerm) t;
            List<Type> newArgs = F.map(term.getArgs(), arg -> applyVarTab(varTab, arg));
            return new TypeTerm(newArgs);
        } else {
            throw new Error("Unsupported type construct.");
        }
    }

    private static Type applySkolemTab(TreeMap<Integer,TypeSym> skolemTab, Type t) {
        if (t instanceof TypeSym) {
            TypeSym sym = (TypeSym) t;
            Integer sid = sym.getSkolemId();
            if (sid == null) {
                return sym;
            } else {
                TypeSym newSkolem = skolemTab.get(sid);
                return (newSkolem == null) ? sym : newSkolem;
            }
        } else if (t instanceof TypeVar) {
            return t;
        } else if (t instanceof TypeTerm) {
            TypeTerm term = (TypeTerm) t;
            List<Type> newArgs = F.map(term.getArgs(), arg -> applySkolemTab(skolemTab, arg));
            return new TypeTerm(newArgs);
        } else {
            throw new Error("Unsupported type construct.");
        }
    }



}
