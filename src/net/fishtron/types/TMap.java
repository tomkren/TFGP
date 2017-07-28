package net.fishtron.types;

import cz.tomkren.utils.AA;

import java.util.*;
import java.util.function.Function;


/** Created by tom on 7.11.2015.*/

public class TMap<A> {

    private Map<Type,List<A>> map;

    public TMap() {
        map = new HashMap<>();
    }

    public TMap(List<A> xs, Function<A,Type> f) {
        this();
        for (A x : xs) {
            add(f.apply(x), x);
        }
    }

    // singleton
    public TMap(Type t, A x) {
        this();
        add(t, x);
    }

    public void add(Type t, A x) {
        List<A> slot = map.get(t);
        if (slot == null) {
            slot = new ArrayList<>();
            map.put(t, slot);
        }
        slot.add(x);
    }

    public void add(Type t, List<A> xs) {
        List<A> slot = map.get(t);
        if (slot == null) {
            slot = new ArrayList<>();
            map.put(t, slot);
        }
        slot.addAll(xs);
    }


    public void add(TMap<A> tMap) {
        for (Map.Entry<Type,List<A>> e : tMap.map.entrySet()) {
            add(e.getKey(),e.getValue());
        }
    }

    public static <A> TMap<A> merge(List<TMap<A>> tMaps) {
        TMap<A> ret = new TMap<>();
        tMaps.forEach(ret::add);
        return ret;
    }

    public static <A> Map<Type,AA<List<A>>> intersection(TMap<A> tMap1, TMap<A> tMap2) {

        Map<Type,AA<List<A>>> ret = new HashMap<>();

        for (Map.Entry<Type,List<A>> e : tMap1.entrySet()) {

            Type type = e.getKey();
            List<A> list2 = tMap2.get(type);

            if (list2 != null) {
                ret.put(type, new AA<>(e.getValue(),list2));
            }
        }

        return ret;
    }


    public List<A> get(Type t) {
        return map.get(t);
    }

    public boolean isEmpty() {
        return map.isEmpty();  // díky tomu že se jen přidává, tak jde jednoduše, ale pozor na to !!!
    }

    public Set<Map.Entry<Type,List<A>>> entrySet() {
        return map.entrySet();
    }

    public int size() {
        int size = 0;
        for (Map.Entry<Type,List<A>> e : map.entrySet()) {
            size += e.getValue().size();
        }
        return size;
    }

    public List<A> flatten() {
        List<A> ret = new ArrayList<>();
        for (Map.Entry<Type,List<A>> e : map.entrySet()) {
            ret.addAll(e.getValue());
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Type,List<A>> e : map.entrySet()) {
            Type type  = e.getKey();
            List<A> xs = e.getValue();

            sb.append(type.toString()).append(" :\n");

            for (A x : xs) {
                sb.append("  ").append(x.toString()).append("\n");
            }

        }
        return sb.toString();
    }

}

