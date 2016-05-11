package cz.tomkren.utils;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Nums {

    public static List<Double> range(double from, double to, double step) {

        int n = 1 + (int) Math.floor( (to - from) / step );

        List<Double> ret = new ArrayList<>(n);

        for (int i=0; i<n ; i++) {
            ret.add(from + i*step);
        }

        return ret;
    }

    public static long numCombinations(int n, int k) {
        if (k>n || n<0 || k<0) {return 0;}
        long ret = 1;
        for (int i = 0; i < k; i++) {
            int up   = n-i;
            int down = i+1;
            ret = (ret*up)/down;
        }
        return ret;
    }

    public static boolean lt(int i, BigInteger big) {
        return BigInteger.valueOf(i).compareTo(big) == -1;
    }

    public static boolean lt(BigInteger big, int i) {
        return big.compareTo(BigInteger.valueOf(i)) == -1;
    }

    public static int min(int i, BigInteger big) {
        return BigInteger.valueOf(i).compareTo(big) == -1 ? i : big.intValue();
    }

    public static int min(BigInteger big, int i) {
        return big.compareTo(BigInteger.valueOf(i)) == -1 ? big.intValue() : i;
    }

    public static void main(String[] args) {
        Checker c = new Checker();

        c.check( range(-1,1,0.1) );
        c.check("[0.0, 0.6]", range(0,1,0.6) );
        c.check("[0.0, 0.6, 1.2]", range(0,1.2,0.6) );

        c.check("2598960", numCombinations(52,5) );
        c.check("0", numCombinations(52,-5) );
        c.check("1", numCombinations(0,0) );
        c.check("0", numCombinations(0,10) );
        c.check("1", numCombinations(4,0) );

        c.check("true", lt(0,BigInteger.ONE) );
        c.check("false", lt(0,BigInteger.ZERO) );
        c.check("false", lt(10,BigInteger.ZERO) );

        c.check("false", lt(BigInteger.ONE,0) );
        c.check("false", lt(BigInteger.ZERO,0) );
        c.check("true", lt(BigInteger.ZERO,10) );

        c.check("1", min(156,BigInteger.ONE) );
        c.check("0", min(BigInteger.ZERO,178) );
        c.check("0", min(10,BigInteger.ZERO) );

        c.results();

    }

}
