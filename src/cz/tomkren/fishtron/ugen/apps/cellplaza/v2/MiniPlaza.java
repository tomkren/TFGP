package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;

import java.util.Random;

/** Created by tom on 19.03.2017 */

public class MiniPlaza {

    public static void main(String[] args) {
        Checker ch = new Checker();
        main_1(ch);
        ch.results();
    }


    private static AB<AppTree,Rule> genBitRule(Random rand) {
        Gen gen = new Gen(Libs.gamma, rand);
        AppTree tree = gen.genOne(1, Libs.ruleType);
        tree = tree.randomizeParams(Libs.allParamsInfo, rand);
        Rule rule = (Rule) Libs.lib.eval(tree);
        return AB.mk(tree,rule);
    }

    private static void main_1(Checker ch) {
        Random rand = ch.getRandom();


        JSONArray names = Libs.allParamsInfo.getJSONObject("seedImg").getJSONArray("filename");

        String coreName = (String) F.randomElement(names, rand);


        AB<AppTree,Rule> rule = genBitRule(rand);

        CellWorld w = new CellWorld("mini_100", coreName, rule._2(), false);

        int numSteps = 100;

        w.writeState();
        for (int s = 0; s < numSteps; s++) {
            w.step();
            w.writeState();
        }

        Log.it();
        Log.it("ruleCode = "+rule._1());
        Log.it("coreName = "+coreName);

    }




}
