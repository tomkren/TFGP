package cz.tomkren.fishtron.ugen.apps.cellplaza.v1;

import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Checker;
import net.fishtron.utils.F;
import cz.tomkren.utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**Created by tom on 12.03.2017.*/

public class MiniPlaza {

    public static void main(String[] args) {
        Checker ch = new Checker();
        main_new(ch);
        ch.results();
    }


    private static void main_old(Checker ch) {
        Random rand = ch.getRandom();

        int numRules = (int)Math.pow(2,18);
        int ruleCode = rand.nextInt(numRules);
        Log.it(ruleCode +"/"+ numRules);
        Rule rule = Rule.mk(ruleCode);  //Rule.mk(0,1, null, 1,8);

        List<String> coreNames = Arrays.asList("core01","core02","core03","core04","core05","core06","core07");
        String coreName = F.randomElement(coreNames, ch.getRandom());

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
    }

    private static void main_new(Checker ch) {
        Random rand = ch.getRandom();



        Gen gen = new Gen(CellLib.bitGamma_1, ch);
        AppTree tree = gen.genOne(CellLib.bitIndivSize_1, CellLib.bitGoal_1);
        tree = tree.randomizeParams(CellLib.allParamsInfo_bitVersion_1, rand);

        Log.it(tree.toString());

        EvalLib lib = CellLib.bitLib_1;

        Object indivObj = lib.eval(tree);
        if (!(indivObj instanceof AB)) {throw new Error("Indiv object is not a pair.");}
        AB indivPair = (AB) indivObj;

        Object ruleObj = indivPair._1();
        Rule rule = (Rule) ruleObj;

        Object imgObj  = indivPair._2();
        String imgName = (String) imgObj;

        Log.it();

        String indivFilename = runRule(rule, imgName, 30);
        Log.it("test indivFilename = "+indivFilename);

        Log.it(tree.toString());
    }

    private static int nextIndivId = 1;

    static String runRule(Rule rule, String imgName, int numSteps) {
        CellWorld w = new CellWorld("mini_100", imgName, rule, null /*todo ... null oke pokud se nepoužíva gradient ale prasarna */, false);
        w.step(numSteps);
        String indivFilename = w.writeState(nextIndivId);
        nextIndivId ++;
        return indivFilename;
    }

}
