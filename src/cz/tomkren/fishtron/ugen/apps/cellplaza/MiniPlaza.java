package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**Created by tom on 12.03.2017.*/

public class MiniPlaza {

    public static void main(String[] args) {
        Checker ch = new Checker();
        Random rand = ch.getRandom();

        int numRules = (int)Math.pow(2,18);
        int ruleCode = rand.nextInt(numRules);
        Log.it(ruleCode +"/"+ numRules);

        List<String> coreNames = Arrays.asList("core01","core02","core03","core04","core05","core06","core07");

        Rule rule = /*Rule.mk(ruleCode); //*/ Rule.mk(0,1, null, 1,8);
        String coreName = F.randomElement(coreNames, rand);

        CellWorld w = new CellWorld("mini_100", coreName, rule, ch.getRandom(), false);

        int numSteps = 100;

        w.writeState();
        for (int s = 0; s < numSteps; s++) {
            w.step();
            w.writeState();
        }

        Log.it();
        Log.it("ruleCode = "+ruleCode);
        Log.it("coreName = "+coreName);

        ch.results();
    }

}
