package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.utils.Checker;


/**Created by tom on 11.03.2017.*/

public class FinalPlaza {

    public static void main(String[] args) {
        Checker ch = new Checker();

        Rule rule = Rule.mk(0,1, null, 1,8);

        CellWorld w = new CellWorld("final", "core01", rule, ch.getRandom(), false);

        w.writeState();
        w.step();
        w.writeState();
        w.step();
        w.writeState();
        w.step(10);
        w.writeState();
        w.step(100);
        w.writeState();
        w.step(99);
        w.writeState();

        ch.results();
    }

}
