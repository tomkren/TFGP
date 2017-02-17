package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.types.TMap;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.apps.workflows.Workflows;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.utils.*;
import org.json.JSONObject;

import java.util.*;

/** Created by tom on 15. 2. 2017.*/

// TODO kontrolovat zda se fakt strom změnil (třeba na 5 pokusů)

public class BasicAppTreeXover implements Operator<AppTreeIndiv> {

    private final double operatorProbability;
    private final Random rand;
    private final int maxTreeSize;
    private final JSONObject opts;

    public BasicAppTreeXover(JSONObject opts, Random rand) {
        this.opts = opts;
        this.operatorProbability = opts.getDouble("probability");
        this.maxTreeSize = opts.getInt("maxTreeSize");
        this.rand = rand;
    }

    public BasicAppTreeXover(double operatorProbability, int maxTreeSize, Random rand) {
        this(F.obj("probability", operatorProbability, "maxTreeSize", maxTreeSize), rand);
    }

    @Override public JSONObject getOperatorInfo() {return opts;}
    @Override public double getWeight() {return operatorProbability;}
    @Override public int getNumInputs() {return 2;}

    @Override
    public List<AppTreeIndiv> operate(List<AppTreeIndiv> parents) {
        AppTreeIndiv mum = parents.get(0);
        AppTreeIndiv dad = parents.get(1);
        EvalLib lib = mum.getLib();

        AA<AppTree> children = xover(mum.getTree(), dad.getTree());

        AppTreeIndiv child1 = new AppTreeIndiv(children._1(), lib);
        AppTreeIndiv child2 = new AppTreeIndiv(children._2(), lib);
        return Arrays.asList(child1,child2);
    }


    private AA<AppTree> xover(AppTree mum, AppTree dad) {

        TMap<SubtreePos> mumPoses = mum.getAllSubtreePoses_byTypes();
        TMap<SubtreePos> dadPoses = dad.getAllSubtreePoses_byTypes();

        Map<Type,AA<List<SubtreePos>>> intersection = TMap.intersection(mumPoses, dadPoses);

        int numPossiblePairs = getNumPossiblePairs(intersection);
        int ball = rand.nextInt(numPossiblePairs);
        AA<SubtreePos> selectedPoses = selectPoses(ball, intersection);
        SubtreePos mumPos = selectedPoses._1();
        SubtreePos dadPos = selectedPoses._2();

        AA<AppTree> children = AppTree.xover(mum, dad, mumPos, dadPos);

        return new AA<>(
                children._1().size() <= maxTreeSize ? children._1() : mum ,
                children._2().size() <= maxTreeSize ? children._2() : dad
        );
    }

    private static int getNumPossiblePairs(Map<Type, AA<List<SubtreePos>>> intersection) {
        int sum = 0;
        for (Map.Entry<Type,AA<List<SubtreePos>>> e : intersection.entrySet()) {
            List<SubtreePos> mumList = e.getValue()._1();
            List<SubtreePos> dadList = e.getValue()._2();
            sum += mumList.size() * dadList.size();
        }
        return sum;
    }

    private AA<SubtreePos> selectPoses(int ball, Map<Type,AA<List<SubtreePos>>> intersection) {
        int sum = 0;
        for (Map.Entry<Type,AA<List<SubtreePos>>> e : intersection.entrySet()) {
            AA<List<SubtreePos>> pair = e.getValue();
            List<SubtreePos> mumList = pair._1();
            List<SubtreePos> dadList = pair._2();

            sum += mumList.size() * dadList.size();
            if (sum > ball) {
                SubtreePos mumPos = F.randomElement(mumList,rand);
                SubtreePos dadPos = F.randomElement(dadList,rand);
                return new AA<>(mumPos, dadPos);
            }
        }
        throw new Error("Unreachable!");
    }


    public static void main(String[] args) {
        Checker ch = new Checker(5525384885671817405L);

        Gen gen = new Gen(Workflows.gamma, ch.getRandom());

        AppTree mum = gen.genOne(20, Workflows.goal);
        AppTree dad = gen.genOne(15, Workflows.goal);

        ch.it(mum);
        ch.it(dad);

        ch.it(mum.toStringWithTypes());
        ch.it(dad.toStringWithTypes());

        TMap<SubtreePos> mumPoses = mum.getAllSubtreePoses_byTypes();
        TMap<SubtreePos> dadPoses = dad.getAllSubtreePoses_byTypes();
        Map<Type,AA<List<SubtreePos>>> intersection = TMap.intersection(mumPoses, dadPoses);
        int numPossiblePairs = getNumPossiblePairs(intersection);

        Log.it("numPossiblePairs: "+numPossiblePairs);

        for (Map.Entry<Type,AA<List<SubtreePos>>> e : intersection.entrySet()) {
            Type type = e.getKey();
            Log.it(type);
        }

        BasicAppTreeXover xOver = new BasicAppTreeXover(1.0, 50, ch.getRandom());

        int numTries = 100000;
        Map<String,Integer> combos = new TreeMap<>(AppTree.compareStrs);
        for (int i = 0; i < numTries; i++) {
            AA<AppTree> children = xOver.xover(mum, dad);
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
