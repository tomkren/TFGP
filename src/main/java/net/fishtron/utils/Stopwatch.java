package net.fishtron.utils;

/** Created by user on 8. 2. 2017. */

public class Stopwatch {
    private long startTime;
    private final int precision;

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public Stopwatch() {
        this(3);
    }

    public Stopwatch(int precision) {
        this.precision = precision;
        reset();
    }

    public String restart() {
        return "\t["+reset()+" s]";
    }

    public double reset() {
        double previousTime = getTime(precision);
        this.startTime = System.nanoTime();
        return previousTime;
    }

    public double getTime() {
        return (System.nanoTime()-startTime)/1E9;
    }

    public double getTime(int precision) {
        return F.prettyDouble(getTime(), precision);
    }

}