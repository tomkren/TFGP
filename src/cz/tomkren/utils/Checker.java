package cz.tomkren.utils;

import java.util.List;
import java.util.Random;

public class Checker {

    private int sum;
    private int ok;
    private int ko;

    private long startTime;
    private long seed;
    private Random rand;

    public Checker(Long seed) {
        this(seed, false);
    }

    public Checker(Long seed, boolean silent) {

        sum = 0;
        ok = 0;
        ko = 0;

        this.seed = seed == null ? (new Random().nextLong()) : seed;

        rand = new Random(this.seed);

        if (!silent) {
            Log.it("seed : "+this.seed+"L\n");
        }

        startTime();
    }

    public Checker(boolean silent) {
        this(null, silent);
    }

    public Checker() {
        this(null);
    }

    public void results() {
        String hlaska = ko == 0 ? ":)" : ":( nééééé" ;
        Log.it("\n"+ok+" OK, "+ko+" KO.   "+hlaska);
        Log.it("Seed was: "+seed+"L");
        showTime();
    }

    private void startTime() {
        startTime = System.nanoTime();
    }
    private void showTime() {
        Log.it("It took "+getTime() +" seconds...");
    }

    public double getTime() {
        return (System.nanoTime()-startTime)/1E9;
    }

    public Random getRandom() {
        return rand;
    }

    public long getSeed() {
        return seed;
    }

    public Checker it(Object o) {
        Log.it(o);
        return this;
    }

    public Checker itln(Object o) {
        Log.itln(o);
        return this;
    }

    public Checker ln() {
        Log.it();
        return this;
    }

    public Checker list(List<?> xs) {
        Log.list(xs);
        return this;
    }

    public Checker it(Object o, String shouldBe) {
        check(shouldBe, o);
        return this;
    }

    public Checker itln(Object o, String shouldBe) {
        check(shouldBe, o);
        Log.it();
        return this;
    }

    public Checker it(Object o, int shouldBe) {
        check(Integer.toString(shouldBe), o);
        return this;
    }

    public Checker it(Object o, boolean shouldBe) {
        check(Boolean.toString(shouldBe), o);
        return this;
    }

    public Checker is(boolean shouldBeTrue) {
        checkCore(shouldBeTrue, "<bool>", "<bool>");
        return this;
    }

    public Checker eq(Object x, Object y) {
        checkCore(x.equals(y), x+" =eqs= "+y , x+" SHOULD equals() "+y );
        return this;
    }

    public Checker eqStr(Object x, Object y) {
        checkCore(x.toString().equals(y.toString()), x+" =str= "+y , x+" SHOULD eqStr "+y );
        return this;
    }

    public Checker eqStrLn(Object x, Object y) {
        checkCore((x.toString()).equals(y.toString()), "\n"+x+" \n=str=\n \n"+y , x+" SHOULD eqStr "+y );
        return this;
    }

    public Checker eqStrSilent(Object x, Object y) {
        checkCore((x.toString()).equals(y.toString()), ".. =str= .." , x+" SHOULD eqStr "+y );
        return this;
    }

    private void checkCore (boolean shouldBeTrue, String okMsg, String koMsg) {
        sum++;
        if (shouldBeTrue) { Log.it("[OK "+sum+"] "+okMsg); ok++; }
        else  { System.err.println("[KO "+sum+"] "+koMsg); ko++; }
    }

    public void check( String shouldBe, Object o ) {

        String str = ( o == null ? "null" : o.toString() );
        checkCore(shouldBe.equals(str), str, str+" SHOULD BE "+shouldBe );
    }

    public void check(Object o) {
        Log.it(o);
    }


}
