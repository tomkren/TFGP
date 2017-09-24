package cz.tomkren.fishtron.apps;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.operators.UntypedRampedHalfAndHalf;
import cz.tomkren.fishtron.sandbox.*;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.List;

/** Created by tom on 22.2.2016.*/

public class AntLibs {

    public static Object l = "l";
    public static Object r = "r";
    public static Object m = "m";
    public static Object d = "d";

    public static JSONArray ifa(Object a, Object b) {return new JSONArray(Arrays.asList("ifa",a,b));}
    public static JSONArray pr2(Object a, Object b) {return new JSONArray(Arrays.asList("pr2",a,b));}
    public static JSONArray pr3(Object a, Object b, Object c) {return new JSONArray(Arrays.asList("pr3",a,b,c));}


    public static Type A = Types.parse("A");


    public static SmartLibrary koza = SymProvider.mkLib(
            new TComb0<>("l",   A,       l),
            new TComb0<>("r",   A,       r),
            new TComb0<>("m",   A,       m),
            new TComb2<>("ifa", A,A,A,   AntLibs::ifa),
            new TComb2<>("pr2", A,A,A,   AntLibs::pr2),
            new TComb3<>("pr3", A,A,A,A, AntLibs::pr3)
    );

    public static SmartLibrary buildAnt = SymProvider.mkLib(
            new TComb0<>("l",   A,       l),
            new TComb0<>("r",   A,       r),
            new TComb0<>("m",   A,       m),
            new TComb0<>("d",   A,       d),
            new TComb2<>("ifa", A,A,A,   AntLibs::ifa),
            new TComb2<>("pr2", A,A,A,   AntLibs::pr2),
            new TComb3<>("pr3", A,A,A,A, AntLibs::pr3)
    );

    public static void main(String[] args) {
        Checker ch = new Checker();

        JsonEvalInterface evaluator = new JsonEvalInterface("http://localhost:8080");

        JSONArray test1 = ifa(m,l);
        JSONArray test2 = ifa(m,pr3(l,pr2(ifa(m,r),pr2(r,pr2(l,r))),pr2(ifa(m,l),m)));


        ch.it(evaluator.getInt("perfectScore"), 89);

        ch.it(evaluator.eval("evalAnts", new JSONArray(Arrays.asList(test1,test2)), x->(int)x), "[11, 89]");


        IndivGenerator<PolyTree> generator = new UntypedRampedHalfAndHalf(AntLibs.koza, ch.getRandom(), true);

        int numIndivs = 500;
        List<PolyTree> trees = generator.generate(numIndivs);
        List<Object> values = F.map(trees, FitIndiv::computeValue);
        JSONArray jsonIndivs = new JSONArray(values);


        List<Double> scores = evaluator.eval("evalAnts", jsonIndivs, x->(double)(int)x);
        Log.it( "10 best scores (served in double): "+ F.take(10,F.sort(scores,x->-x)));


        ch.results();

        // vliv vykreslovacího módu na haskell serveru
        // -1 ... 40.27 sec
        // -2 ...  8.44 sec
        // -3 ...  6.21 sec
        // -4 ...  6.13 sec

    }



}
