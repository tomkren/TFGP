package cz.tomkren.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Vector čísel pro vektorový operace na číslech..
 */
public class Vec {

    public static void main(String[] args) {

        Checker check = new Checker();

        int n = 6;
        Vec x = new Vec(n);
        Vec x1 = new Vec(new double[]{1,2,3,4,5,6});
        Vec x2 = new Vec(10,20,30,40,50,60);


        check.it(x, "[ 0.0 0.0 0.0 0.0 0.0 0.0 ]");
        check.it(x1,"[ 1.0 2.0 3.0 4.0 5.0 6.0 ]");
        check.it(x2, "[ 10.0 20.0 30.0 40.0 50.0 60.0 ]");

        check.it( add(x1,x2), "[ 11.0 22.0 33.0 44.0 55.0 66.0 ]");
        check.it( krat(x1, x2), "[ 10.0 40.0 90.0 160.0 250.0 360.0 ]");
        check.it( TIMES.compute(Arrays.asList(x1,x2)), "[ 10.0 40.0 90.0 160.0 250.0 360.0 ]");
        check.it( MINUS.compute(Arrays.asList(x1,x2)), "[ 10.0 40.0 90.0 160.0 250.0 360.0 ]");


        check.results();


    }

    private double[] xs;
    private int n;

    public Vec(int n) {
        this.n = n;
        xs = new double[n];
        for (int i = 0; i<n; i++) {xs[i] = 0;}
    }

    public Vec(double... ys){
        n = ys.length;
        xs = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = ys[i];
        }
    }

    public Vec(List<Double> ds) {
        n = ds.size();
        xs = new double[n];
        for (int i = 0; i<n; i++) {xs[i] = ds.get(i);}
    }

    public Vec (double from, double to, double step) {
        this(Nums.range(from, to, step));
    }

    public Vec(Vec parent, int i, double newVal) {
        n = parent.n;
        xs = new double[n];
        for (int j = 0; j<n; j++) {
            xs[j] = j==i ? newVal : parent.xs[j];
        }
    }

    public double get(int i) {return xs[i];}
    public int    size()     {return n;}

    public List<Double> toList() {
        List<Double> ret = new ArrayList<>(n);
        for (Double x : xs) {
            ret.add(x);
        }
        return ret;
    }

    public double max() {
        double ret = - Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if (xs[i] > ret) {
                ret = xs[i];
            }
        }
        return ret;
    }

    public double min() {
        double ret = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if (xs[i] < ret) {
                ret = xs[i];
            }
        }
        return ret;
    }

    public double rangeDelta() {
        return max() - min();
    }

    public static final Comb0 PLUS  = Comb0.mkVecFun2(Vec::plus);
    public static final Comb0 MINUS = Comb0.mkVecFun2(Vec::minus);
    public static final Comb0 TIMES = Comb0.mkVecFun2(Vec::krat);
    public static final Comb0 RDIV  = Comb0.mkVecFun2(Vec::rdiv);

    public static final Comb0 SIN  = Comb0.mkVecFun(Vec::sin);
    public static final Comb0 COS  = Comb0.mkVecFun(Vec::cos);
    public static final Comb0 EXP  = Comb0.mkVecFun(Vec::exp);
    public static final Comb0 RLOG = Comb0.mkVecFun(Vec::rlog);


    public static Vec plus (Vec x, Vec y) {return binOp((a,b)->a+b, x,y);}
    public static Vec minus(Vec x, Vec y) {return binOp((a,b)->a-b, x,y);}
    public static Vec krat (Vec x, Vec y) {return binOp((a,b)->a*b, x,y);}
    public static Vec rdiv (Vec x, Vec y) {return binOp((a,b)->b == 0 ? 1 : a/b, x,y);}

    public static Vec sin (Vec x) {return unarOp(Math::sin, x);}
    public static Vec cos (Vec x) {return unarOp(Math::cos, x);}
    public static Vec exp (Vec x) {return unarOp(Math::exp, x);}
    public static Vec rlog(Vec x) {return unarOp(a->a == 0 ? 0 : Math.log(Math.abs(a)) , x ); }

    public static Function<Vec,Vec> mkUnarOp(Function<Double,Double> f) {
        return xs -> unarOp(f,xs);
    }

    public static Vec unarOp(Function<Double, Double> f, Vec a) {
        int n = a.n;
        Vec ret = new Vec(n);
        for (int i = 0; i<n; i++) {
            ret.xs[i] =  f.apply(a.xs[i]);
        }
        return ret;
    }

    public static Vec binOp(BiFunction<Double,Double,Double> f,Vec a, Vec b) {
        int n = a.n;
        Vec ret = new Vec(n);
        for (int i = 0; i<n; i++) {
            ret.xs[i] =  f.apply(a.xs[i], b.xs[i]);
        }
        return ret;
    }

    public static Vec add(Vec a, Vec b) {
        int n = a.n;
        Vec ret = new Vec(n);
        for (int i = 0; i<n; i++) {
            ret.xs[i] = a.xs[i] + b.xs[i];
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(2*n);
        sb.append("[ ");
        for (int i = 0; i < n; i++) {
            sb.append( xs[i] ).append(' ');
        }
        sb.append(']');
        return sb.toString();
    }
}
