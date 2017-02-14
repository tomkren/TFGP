package cz.tomkren.fishtron.ugen.eval;

import java.util.function.Function;

/** Created by user on 14. 2. 2017. */

public class EvalTester {

    public static void main(String[] args) {

        FunCode2 plus = x -> (y ->  ((double)x) + ((double)y));

        EvalLib lib = new EvalLib(
                "plus", plus,
                "1", 1
        );



    }

}
