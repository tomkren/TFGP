package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.Gamma;

/**Created by tom on 12.03.2017.*/

public class CellLib {

    private  static final Type goal = Types.parse("S -> (N -> S)");

    private static final Gamma gamma = Gamma.mk(
            "s", "(a -> (b -> c)) -> ((a -> b) -> (a -> c))",
            "k", "a -> (b -> a)",

            "if",    "B -> (x -> (x -> x))",
            "not",   "B -> B",
            "or",    "B -> (B -> B)",
            "and",   "B -> (B -> B)",

            "==",    "(Eq a) -> (a -> (a -> B))",
            "<",     "(Ord a) -> (a -> (a -> B))",

            "alive", "S",
            "dead",  "S",

            "0",     "N",
            "1",     "N",
            "2",     "N",
            "3",     "N",
            "4",     "N",
            "5",     "N",
            "6",     "N",
            "7",     "N",
            "8",     "N",

            "eqS",   "Eq S",
            "eqN",   "Eq N",
            "ordN",  "Ord N"
        );

}
