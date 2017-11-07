package net.fishtron.utils;

// TODO | myslim že tu je debilně udělaný něco možná, občas tam naběhne stackOverflow když to je extra velký,
// todo | tak nějak nezávisle otestovat a dyštak najít oficiální alternativu jak se má dělat když člověk chce funkcionální seznamy

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// nil pomocí null

public class Listek <T> {
    private T head;
    private Listek<T> tail;

    public Listek(T head, Listek<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    public static <A> Listek<A> mk(A x, Listek<A> xs) {
        return new Listek<>(x, xs);
    }

    public List<T> toList() {
        List<T> ret = new ArrayList<>();
        addToList(ret);
        return ret;
    }

    public static <A> List<A> toList(Listek<A> xs) {
        return xs == null ? new ArrayList<>(0) : xs.toList();
    }

    public static <A> List<A> toReverseList(Listek<A> xs) {
        return Lists.reverse(toList(xs));
    }

    public void addToList(List<T> ret) {
        ret.add(head);
        if (tail != null) {
            tail.addToList(ret);
        }
    }

    public static <A> Listek<A> mkSingleton(A x) {
        return new Listek<>(x,null);
    }

    public static <A> Listek<A> fromList(List<A> xs) {
        return F.list(xs).foldr(null, Listek::new);
    }

    public T getHead() {
        return head;
    }

    public Listek<T> getTail() {
        return tail;
    }

    @Override
    public String toString() {
        return "[ "+ toString_() +" ]";
    }

    private String toString_() {
        return head.toString() + (tail == null ? "" : " "+tail.toString_());
    }




    public static void main(String[] args) {
        Checker ch = new Checker();

        ch.it(Listek.fromList(Collections.emptyList()), "null");
        ch.it(Listek.fromList(Arrays.asList(1,2,3)), "[ 1 2 3 ]");

        ch.it(Listek.fromList(Arrays.asList(1,2,3)).toList() );


        ch.results();
    }
}
