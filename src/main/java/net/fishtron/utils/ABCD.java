package net.fishtron.utils;

// modifikovatelná čtveřice
public class ABCD<A,B,C,D> {

    public static <A,B,C,D> ABCD<A,B,C,D> mk(A a, B b, C c, D d) {
        return new ABCD<>(a,b,c,d);
    }

    private A a;
    private B b;
    private C c;
    private D d;

    public ABCD(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public A _1() {return a;}
    public B _2() {return b;}
    public C _3() {return c;}
    public D _4() {return d;}

    public void set_1(A a) {this.a = a;}
    public void set_2(B b) {this.b = b;}
    public void set_3(C c) {this.c = c;}
    public void set_4(D d) {this.d = d;}


    @Override
    public String toString() {
        return "<"+ a +","+ b +","+ c +","+ d +">";
    }
}
