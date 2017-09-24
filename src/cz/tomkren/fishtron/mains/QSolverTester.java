package cz.tomkren.fishtron.mains;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import cz.tomkren.fishtron.reusegen2.QSolver;
import cz.tomkren.fishtron.sandbox.*;
import cz.tomkren.fishtron.terms.SmartLibrary;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.function.Function;

/** Created by user on 21. 6. 2016.*/

public class QSolverTester {



    public static int f(int x) {return x+1;}
    public static Object seri(Function<Object,Object> f, Function<Object,Object> g, Object x) {
        return g.apply(f.apply(x));
    }


    public static SmartLibrary lib_0 = SymProvider.mkLib(
            new TComb1<>("f", "X", "X", QSolverTester::f),
            new TComb3<>("seri", "-> a b", "-> b c", "a", "c", QSolverTester::seri)
    );

    public static void main(String[] args) {
        try {


            //String jsonConfigFilename = "configs/dageva/config_QSolverTest_1.json";
            String jsonConfigFilename = "configs/dageva/config_stacking_ListAnot2.json";

            String configStr = Files.toString(new File(jsonConfigFilename), Charsets.UTF_8);
            JSONObject config = new JSONObject(configStr);

            //Type goalType = Types.parse("-> X X");
            Type goalType = Types.parse(config.getString("goalType"));

            String classPrefix = "cz.tomkren.fishtron.workflows.";
            SmartLibrary lib = SmartLibrary.mk(classPrefix, DagEvaTester.testParamsInfo, config.getJSONArray("lib"));




            Long seed = config.has("seed") ? config.getLong("seed") : null;
            Checker checker = new Checker(seed);
            Random rand = checker.getRandom();
            if (seed == null) {config.put("seed", checker.getSeed());}







            // --------------------

            int toTreeSize = 8;

            QSolver qSolver = new QSolver(lib, rand);

            //int i = 3;

            //BigInteger num = qSolver.getNum(goalType, i);
            //Log.it( "\n\ngetNum(" + goalType + ", " + i + ") = " + num );

            //Log.it("----------------------------------------------------------------------");
            //AppTree ti = qSolver.generateOne(goalType, i);
            //Log.it(" ... "+ti);
            //AppTree ti2 = qSolver.generateOne(goalType, i);
            //Log.it(" ... "+ti2);


            Log.it("\n\nQSolver\n---------");
            for (int i = 1; i <= toTreeSize; i++) {
                BigInteger num = qSolver.getNum(goalType, i);
                Log.it("getNum(" + goalType + ", " + i + ") = " + num);
                Log.list(F.map(qSolver.generateAll(goalType, i), x->"  "+x));
            }


        } catch (IOException e) {
            Log.itln("Config file error: "+e.getMessage());
        } catch (JSONException e) {
            Log.itln("JSON error: " + e.getMessage());
        }
    }

}
