package net.fishtron.eva.simple;

import net.fishtron.eva.Operator;
import net.fishtron.trees.SubtreePos;
import net.fishtron.types.TMap;
import net.fishtron.types.Type;
import net.fishtron.trees.AppTree;
import net.fishtron.gen.Gen;
import net.fishtron.eval.EvalLib;
import net.fishtron.utils.AA;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONObject;

import java.util.*;

//import n et.t odo.apps.workflows.Workflows;
import net.fishtron.apps.gpml.dag.NewSimpleWorkflows;



/** Created by tom on 15. 2. 2017.*/

// TODO kontrolovat zda se fakt strom změnil (třeba na 5 pokusů)

public class BasicAppTreeXover implements Operator<AppTreeIndiv> {

    private final double operatorProbability;
    private final Random rand;
    private final int maxTreeSize;
    private final JSONObject opts;

    BasicAppTreeXover(JSONObject opts, Random rand) {
        this.opts = opts;
        this.operatorProbability = opts.getDouble("probability");
        this.maxTreeSize = opts.getInt("maxTreeSize");
        this.rand = rand;
    }

    @Override public JSONObject getOperatorInfo() {return opts;}
    @Override public double getWeight() {return operatorProbability;}
    @Override public int getNumInputs() {return 2;}

    @Override
    public List<AppTreeIndiv> operate(List<AppTreeIndiv> parents) {
        AppTreeIndiv mum = parents.get(0);
        AppTreeIndiv dad = parents.get(1);
        EvalLib lib = mum.getLib();

        AA<AppTree> children = AppTree.xover(mum.getTree(), dad.getTree(), maxTreeSize, rand);

        AppTreeIndiv child1 = new AppTreeIndiv(children._1(), lib);
        AppTreeIndiv child2 = new AppTreeIndiv(children._2(), lib);
        return Arrays.asList(child1,child2);
    }




    public static void main(String[] args) {
        Checker ch = new Checker(5525384885671817405L);

        Gen gen = new Gen(NewSimpleWorkflows.gamma, ch);

        AppTree mum = gen.genOne(20, NewSimpleWorkflows.goal);
        AppTree dad = gen.genOne(15, NewSimpleWorkflows.goal);

        ch.it(mum);
        ch.it(dad);

        ch.it(mum.toStringWithTypes());
        ch.it(dad.toStringWithTypes());

        TMap<SubtreePos> mumPoses = mum.getAllSubtreePoses_byTypes();
        TMap<SubtreePos> dadPoses = dad.getAllSubtreePoses_byTypes();
        Map<Type,AA<List<SubtreePos>>> intersection = TMap.intersection(mumPoses, dadPoses);
        int numPossiblePairs = AppTree.getNumPossibleXoverPairs(intersection);

        Log.it("numPossiblePairs: "+numPossiblePairs);

        for (Map.Entry<Type,AA<List<SubtreePos>>> e : intersection.entrySet()) {
            Type type = e.getKey();
            Log.it(type);
        }

        int maxTreeSize = 50;

        int numTries = 100000;
        Map<String,Integer> combos = new TreeMap<>(AppTree.compareStrs);
        for (int i = 0; i < numTries; i++) {
            AA<AppTree> children = AppTree.xover(mum, dad, maxTreeSize, ch.getRandom());
            combos.merge(children.toString(), 1, F::plus);
        }

        double expectedHits = numTries / numPossiblePairs;

        int sum = 0;
        for (Map.Entry<String,Integer> combo : combos.entrySet()) {
            int num = (int) Math.round(combo.getValue() / expectedHits);
            Log.it(num +" \t "+ combo.getKey());
            sum += num;
        }

        ch.it(sum, numPossiblePairs);



    }


}
