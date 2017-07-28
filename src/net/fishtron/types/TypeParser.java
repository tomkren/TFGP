package net.fishtron.types;

import cz.tomkren.utils.Checker;
import java.util.*;

/** Created by tom on 7.11.2015. */

// todo | zobecnit aby šlo použít i pro parsování termovejch stromů
public class TypeParser {

    private Map<String,Integer> varNameToId;
    private Map<Integer,String> varIdToName;
    private int nextIdCandidate;

    public TypeParser() {
        reset();
    }

    private void reset() {
        varNameToId = new HashMap<>();
        varIdToName = new HashMap<>();
        nextIdCandidate = 0;
    }

    private void putVar(int id, String name) {
        varIdToName.put(id, name);
        varNameToId.put(name, id);
    }

    private int getNewVarId() {
        if (varIdToName.get(nextIdCandidate) == null) {
            int newId = nextIdCandidate;
            nextIdCandidate++;
            return newId;
        } else {
            nextIdCandidate++;
            return getNewVarId();
        }
    }


    public Type parse(String str) {

        String[] parts = str.replaceAll("\\("," \\( ").replaceAll("\\)"," \\) ").trim().split("\\s+");

        Stack<Type> stack = new Stack<>();

        for (String part : parts) {

            if ("(".equals(part)) {
                stack.add(null);
                continue;
            }

            if (")".equals(part)) {
                stack.add( merge(stack) );
                continue;
            }

            int varId = idFromVarStr(part);
            stack.add( varId==-1 ? new TypeSym(part) : new TypeVar(varId) );
        }

        Type ret = merge(stack);
        if (!stack.isEmpty()) {return null;}
        return ret;
    }

    private static Type merge(Stack<Type> stack) {
        if (stack.empty()) {return null;}
        if (stack.size() == 1) {return stack.pop();}

        LinkedList<Type> ts = new LinkedList<>();

        Type t = stack.empty() ? null : stack.pop() ;

        while (t != null) {
            ts.addFirst(t);
            t = stack.empty() ? null : stack.pop() ;
        }

        List<Type> permutedTs = Types.fromSyntaxSugar(ts);

        return new TypeTerm(permutedTs);
    }

    private int idFromVarStr(String name) {
        if (name != null && name.length() >= 1) {

            char firstChar = name.charAt(0);

            if (firstChar == 'x') {

                try {
                    int id = Integer.parseInt(name.substring(1));
                    if (varIdToName.get(id) != null || id < 0) {
                        return idFromNamedVar(name);
                    }
                    putVar(id, "x"+id);
                    return id;
                } catch (NumberFormatException e) {
                    return idFromNamedVar(name);
                }

            } else if (Character.isLowerCase(firstChar)) {

                return idFromNamedVar(name);

            }
        }
        return -1;
    }

    private int idFromNamedVar(String name) {
        Integer id = varNameToId.get(name);
        if (id == null) {id = getNewVarId();}
        putVar(id, name);
        return id;
    }

    public static void main(String[] args) {
        Checker ch = new Checker();

        TypeParser tp = new TypeParser();

        // "TypedDag.dia( TypedDag: a => a , TypedDag: a => (V b n) , TypedDag: (V b n) => b ) : a => b"

        ch.it(tp.idFromVarStr("a"), 0);
        ch.it(tp.idFromVarStr("x0"), 1);
        ch.it(tp.idFromVarStr("X0"), -1);
        ch.it(tp.idFromVarStr("$0"), -1);
        ch.it(tp.idFromVarStr("x1"), 2);
        ch.it(tp.idFromVarStr("x3"), 3);
        ch.it(tp.idFromVarStr("x123"), 123);
        ch.it(tp.idFromVarStr("y123"), 4);
        ch.it(tp.idFromVarStr("x-2"), 5);
        ch.it(tp.idFromVarStr("x0"), 1);
        ch.it(tp.idFromVarStr(""), -1);
        ch.it(tp.idFromVarStr(null), -1);
        ch.it(tp.idFromVarStr("xx1"), 6);

        tp.reset();

        ch.it( tp.parse("a => a") , "(x0 => x0)");


        ch.results();
    }

}
