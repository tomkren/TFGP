package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeSym;
import cz.tomkren.fishtron.types.TypeTerm;
import cz.tomkren.fishtron.types.TypeVar;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/** Created by tom on 2. 2. 2017.*/

public class NF2 {

    private Renaming renaming_skol;
    private Renaming renaming_vars;

    private TreeMap<Integer,Integer> tab_skol;
    private TreeMap<Integer,Integer> tab_vars;
    private int n_skol;
    private int n_vars;

    public NF2(Type t) {

        tab_skol = new TreeMap<>();
        tab_vars = new TreeMap<>();

        n_skol = 0;
        n_vars = t.getNextVarId_onlySkolemVars();

        nf(t);

        renaming_skol = new Renaming(tab_skol);
        renaming_vars = new Renaming(tab_vars);
    }


    private void nf(Type t) {
        if (t instanceof TypeSym) {
            nf_sym((TypeSym) t);
        } else if (t instanceof TypeVar) {
            nf_var((TypeVar) t);
        } else if (t instanceof TypeTerm) {
            nf_term((TypeTerm) t);
        } else {
            throw new Error("Unsupported type construct.");
        }
    }

    private void nf_sym(TypeSym sym) {
        Integer sid = sym.getSkolemId();
        if (sid != null) {
            Integer newSid = tab_skol.get(sid);
            if (newSid == null) {
                tab_skol.put(sid, n_skol);
                n_skol ++;
            }
        }
    }

    private void nf_var(TypeVar var) {
        int id = var.getId();
        Integer newId = tab_vars.get(id);
        if (newId == null) {
            tab_vars.put(id, n_vars);
            n_vars++;
        }
    }

    private void nf_term(TypeTerm term) {
        List<Type> args = term.getArgs();
        for (Type t : args) {nf(t);}
    }


}
