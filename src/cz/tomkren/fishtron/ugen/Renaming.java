package cz.tomkren.fishtron.ugen;

import java.util.*;

/** Created by tom on 2. 2. 2017.*/

class Renaming {

    private TreeMap<Integer,Integer> table;
    private TreeMap<Integer,Integer> reverseTable;
    private String failMsg;

    private boolean put(int var1, int var2) {
        return safePut(table, var1, var2) && safePut(reverseTable, var2, var1);
    }

    private boolean safePut(TreeMap<Integer,Integer> t, int key, int val) {
        if (t.get(key) == null) {
            t.put(key, val);
            return true;
        } else {
            setFail("safePut fail: x"+val+" is more times in dom or codom!");
            return false;
        }
    }

    Renaming(TreeMap<Integer,Integer> tab) {

        table        = new TreeMap<>();
        reverseTable = new TreeMap<>();
        failMsg = null;

        Set<Integer> domTabu = new HashSet<>();
        Set<Integer> codomTabu = new HashSet<>();

        List<Integer> domTodo = new ArrayList<>();
        List<Integer> codomTodo = new ArrayList<>();

        for(Map.Entry<Integer,Integer> e : tab.entrySet()) {

            // entry: var1 -> var2
            int var1 = e.getKey();
            int var2 = e.getValue();

            if (var1 != var2) {
                domTabu.add(var1);
                codomTabu.add(var2);

                if (!put(var1, var2)) {return;}
            }
        }

        for(Map.Entry<Integer,Integer> e : table.entrySet()) {
            int var1 = e.getKey();
            int var2 = e.getValue();

            if (domTabu.contains(var2)) {

                if (reverseTable.get(var1) == null) {
                    codomTodo.add(var1);
                }

            } else if (codomTabu.contains(var1)) {

                if (table.get(var2) == null) {
                    domTodo.add(var2);
                }


            } else {
                if(!put(var2, var1)) {return;}
            }
        }

        if (domTodo.size() != codomTodo.size()) {
            setFail("toRenaming fail: dom&codom todo lists must have equal size, but: "+domTodo+" vs "+codomTodo);
            return;
        }

        for (int i = 0; i < domTodo.size(); i++) {
            int fromId = domTodo.get(i);
            int toId = codomTodo.get(i);
            if(!put(fromId, toId)) {return;}
        }
    }


    private void setFail(String error) {
        failMsg = error;
    }



}
