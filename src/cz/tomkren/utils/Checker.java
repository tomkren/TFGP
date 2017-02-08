package cz.tomkren.utils;

import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Checker {

    private int sum;
    private int ok;
    private int ko;

    private long startTime;
    private long seed;
    private Random rand;
    private boolean isSeedRandom;

    private StringBuilder errors;

    public static Checker mk(JSONObject config) {
        return new Checker( config.has("seed") ? config.getLong("seed") : null, false);
    }

    public Checker(Long seed) {
        this(seed, false);
    }

    public Checker(Long seed, boolean silent) {

        sum = 0;
        ok = 0;
        ko = 0;

        errors = new StringBuilder();

        isSeedRandom = (seed == null);
        this.seed = isSeedRandom ? (new Random().nextLong()) : seed;

        rand = new Random(this.seed);

        if (!silent) {
            Log.it("seed : "+this.seed+"L ("+getSeedOrigin()+")");
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

        if (ko > 0) {
            Log.it("== ERRORS ================================================");
            Log.it(errors.toString());
        }

        Log.it("\n"+ok+" OK, "+ko+" KO.   "+hlaska);
        Log.it("Seed was: "+seed+"L ("+getSeedOrigin()+")");
        showTime();
    }

    private String getSeedOrigin() {
        return isSeedRandom ? "random" : "fixed";
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

    public Checker list(List<?> os, List<?> shouldBes) {

        /*sum++;
        if (os.size() == shouldBes.size()) {
            ok++;
        }else {
            fail("sizes of lists do not match.");
            return this;
        }*/

        boolean sizesMatch = checkCore(os.size() == shouldBes.size(),
                "LIST CHECK ... (sizes match)",
                "LIST CHECK fail: sizes of lists do not match ("+os.size()+" vs "+shouldBes.size()+").");

        if (sizesMatch) {
            for (int i = 0; i < os.size(); i++) {
                Object o = os.get(i);
                String shouldBe = shouldBes.get(i).toString();

                check(shouldBe, o);
            }
        }

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

    public Checker is(boolean shouldBeTrue, String comment) {
        checkCore(shouldBeTrue, comment, comment);
        return this;
    }

    public Checker fail(String comment) {
        checkCore(false, null, comment);
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

    private boolean checkCore(boolean shouldBeTrue, String okMsg, String koMsg) {
        sum++;
        String koInfo = ko == 0 ? "" : " <"+ko+"ERRORS occurred before>";
        if (shouldBeTrue) {
            Log.it("[OK "+sum+koInfo+"] "+okMsg);
            ok++;
            return true;
        } else {
            String koStr = "!!! [KO "+sum+koInfo+"] "+koMsg;
            Log.it(koStr);
            errors.append(koStr).append('\n');
            //System.err.println(koStr);
            ko++;
            return false;
        }
    }

    public void check( String shouldBe, Object o ) {

        String str = ( o == null ? "null" : o.toString() );
        checkCore(shouldBe.equals(str), str, str+" SHOULD BE "+shouldBe );
    }

    public void check(Object o) {
        Log.it(o);
    }


}
