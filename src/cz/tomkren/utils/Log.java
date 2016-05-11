package cz.tomkren.utils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class Log {

    private static Log log = new Log();
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static Log it (Object o, boolean isSilent) {
        if (!isSilent) {
            it(o);
        }
        return log;
    }

    private static long startTime;
    public static void start() {
        startTime = System.nanoTime();
    }
    public static void stop() {
        Log.it("It took "+(System.nanoTime()-startTime)/1E9 +" seconds...");
    }

    public static String date(Date d) {
        if (d == null) {
            return null;
        }
        return dateFormat.format(d);
    }
    public static Date parseDate(String str) {
        try {return dateFormat.parse(str);}
        catch (Exception e) {throw new Error("Log.parseDate: Wrong date format! Here it is:"+str);}
    }

    public static Log it (Object o) {
        System.out.println( o );
        return log;
    }

    public static Log it_noln (Object o) {
        System.out.print(o);
        return log;
    }

    public static Log itln (Object o) {
        System.out.println( o );
        System.out.println();
        return log;
    }

    public static Log it () {
        System.out.println();
        return log;
    }

    public static Log err (Object o) {
        System.err.println( o );
        return log;
    }

    public static Log list (List<?> list) {
        list.forEach(System.out::println);
        return log;
    }

    public static Log collection (Collection<?> list) {
        list.forEach(System.out::println);
        return log;
    }

    public static Log array (Object[] os) {
        for (Object o : os) {
            System.out.println(o);
        }
        return log;
    }

    public static <T> String listLn(List<T> xs, Function<T,String> show) {
        StringBuilder sb = new StringBuilder();
        for (T x :xs) {
            sb.append( show.apply(x) ).append( '\n' );
        }
        return sb.toString();
    }

}
