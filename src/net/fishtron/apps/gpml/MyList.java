package net.fishtron.apps.gpml;

/** Created by tom on 7.11.2015.*/

import net.fishtron.utils.Checker;

import java.util.ArrayList;
import java.util.List;

public interface MyList {

    boolean isNil();

    int len();

    List<Object> toList();
    void toList(List<Object> acc);

    <T> List<T> toList(Class<T> type);
    <T> void toList(Class<T> type, List<T> acc);

    static Cons cons(Object x, MyList xs) {return new Cons(x, xs);}
    static Nil nil() {return NIL;}

    Nil NIL = new Nil();

    static MyList mk(Object... xs) {
        MyList acc = new Nil();
        for (int i = xs.length-1; i >= 0; i--) {
            acc = new Cons(xs[i],acc);
        }
        return acc;
    }

    class Nil implements MyList {
        public Nil() {}
        public boolean isNil() {return true;}
        @Override public String toString() {return "[]";}

        @Override public List<Object> toList() {return new ArrayList<>();}
        @Override public void toList(List<Object> acc) {}

        @Override public <T> List<T> toList(Class<T> type) {return new ArrayList<>();}
        @Override public <T> void toList(Class<T> type, List<T> acc) {}

        @Override public int len() {return 0;}
    }

    class Cons implements MyList {
        private Object x;
        private MyList xs;

        public Cons(Object x, MyList xs) {
            this.x  = x;
            this.xs = xs;
        }

        public Cons(Object x, Object xs) {
            this(x, (MyList) xs);
        }

        @Override public boolean isNil() {return false;}

        public Object head() { return x; }
        public Object tail() { return xs; }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(x.toString());
            MyList rest = xs;
            while (!rest.isNil()) {
                Cons tail = (Cons) rest;
                sb.append(",").append(tail.x);
                rest = tail.xs;
            }
            sb.append("]");
            return sb.toString();
        }


        @Override
        public int len() {return 1 + xs.len();}

        @Override
        public void toList(List<Object> acc) {
            acc.add(x);
            xs.toList(acc);
        }

        @Override
        public List<Object> toList() {
            List<Object> ret = new ArrayList<>(len());
            toList(ret);
            return ret;
        }


        @Override
        public <T> void toList(Class<T> type, List<T> acc) {
            acc.add(type.cast(x));
            xs.toList(type, acc);
        }

        @Override public <T> List<T> toList(Class<T> type) {
            List<T> ret = new ArrayList<>(len());
            toList(type, ret);
            return ret;
        }

    }

    static void main(String[] args) {
        Checker ch = new Checker();

        MyList list = new Cons(1,new Cons(2, new Cons(3, new Nil())));
        ch.it(list, "[1,2,3]");

        ch.it(mk(1,2,3), "[1,2,3]");
        ch.it(mk("a","b","c"), "[a,b,c]");
        ch.it(mk(), "[]");


        ch.results();
    }
}