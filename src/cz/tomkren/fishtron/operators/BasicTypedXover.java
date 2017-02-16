package cz.tomkren.fishtron.operators;

import cz.tomkren.fishtron.eva.Operator;
import cz.tomkren.fishtron.reusegen.QuerySolver;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.types.TMap;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.workflows.DataScientistLibs;

import cz.tomkren.utils.AA;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;

import org.json.JSONObject;
import java.util.*;

/** Created by tom on 1. 7. 2015. */

// TODO kontrolovat zda se fakt strom změnil (třeba na 5 pokusů)

public class BasicTypedXover implements Operator<PolyTree> {

    private double operatorProbability;
    private final Random rand;
    private final int maxTreeSize;

    public BasicTypedXover(double operatorProbability, int maxTreeSize, Random rand) {
        this.operatorProbability = operatorProbability;
        this.maxTreeSize = maxTreeSize;
        this.rand = rand;
    }

    // TODO : nesmyslně předávat celej konfig, lepší předat jen ten opts JsonObject, aby se nerozbil starej kód tak dočasně vyřešíme
    // todo : prohozenim parametrů

    @Deprecated
    public BasicTypedXover(JSONObject config, Random rand) {
        this(config.getJSONObject("basicTypedXover").getDouble("probability"),config.getJSONObject("basicTypedXover").getInt("maxTreeSize"),rand);
    }

    private JSONObject opts;
    @Override public JSONObject getOperatorInfo() {return opts;}

    // Preferovaná verze, todo odebrat tu starou deprekovanou !
    public BasicTypedXover(Random rand, JSONObject opts) {
        this(opts.getDouble("probability"),opts.getInt("maxTreeSize"),rand);
        this.opts = opts;
    }

    public AA<PolyTree> xover(PolyTree mum, PolyTree dad) {

        TMap<SubtreePos> mumPoses = mum.getAllSubtreePoses_byTypes();
        TMap<SubtreePos> dadPoses = dad.getAllSubtreePoses_byTypes();

        Map<Type,AA<List<SubtreePos>>> intersection = TMap.intersection(mumPoses, dadPoses);

        int numPossiblePairs = getNumPossiblePairs(intersection);
        int ball = rand.nextInt(numPossiblePairs);
        AA<SubtreePos> selectedPoses = selectPoses(ball, intersection);
        SubtreePos mumPos = selectedPoses._1();
        SubtreePos dadPos = selectedPoses._2();

        AA<PolyTree> children = PolyTree.xover(mum, dad, mumPos, dadPos);

        return new AA<>(
                children._1().getSize() <= maxTreeSize ? children._1() : mum ,
                children._2().getSize() <= maxTreeSize ? children._2() : dad
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
            List<SubtreePos> mumList = e.getValue()._1();
            List<SubtreePos> dadList = e.getValue()._2();
            sum += mumList.size() * dadList.size();

            if (sum > ball) {
                SubtreePos mumPos = F.randomElement(mumList,rand);
                SubtreePos dadPos = F.randomElement(dadList,rand);
                return new AA<>(mumPos, dadPos);
            }
        }

        throw new Error("Unreachable!");
    }




    @Override
    public List<PolyTree> operate(List<PolyTree> parents) {
        AA<PolyTree> children = xover(parents.get(0),parents.get(1));
        return Arrays.asList(children._1(),children._2());
    }

    @Override public int getNumInputs() {return 2;}
    @Override public double getWeight() {return operatorProbability;}

    public static void main(String[] args) {
        Checker ch = new Checker(424242L);

        QuerySolver qs = new QuerySolver(DataScientistLibs.DATA_SCIENTIST_01, ch.getRandom());

        List<PolyTree> trees = qs.simpleUniformGenerate("D => LD", 20, 100);

        Log.list(trees);
        Log.it();

        PolyTree mum = trees.get(94);
        PolyTree dad = trees.get(92);

        ch.it(mum, "(dia kBest (split kMeans (cons DT (cons (dia0 (split copy (cons logR (cons DT nil))) vote) (cons SVC nil)))) vote)");
        ch.it(dad, "(dia PCA (split kMeans (cons logR (cons SVC (cons (dia0 (split copy (cons DT (cons logR nil))) vote) nil)))) vote)");
        Log.it();

        ch.it(mum.showWithTypes());
        ch.it(dad.showWithTypes());

        TMap<SubtreePos> mumPoses = mum.getAllSubtreePoses_byTypes();
        TMap<SubtreePos> dadPoses = dad.getAllSubtreePoses_byTypes();
        Map<Type,AA<List<SubtreePos>>> intersection = TMap.intersection(mumPoses, dadPoses);
        int numPossiblePairs = getNumPossiblePairs(intersection);

        for (Map.Entry<Type,AA<List<SubtreePos>>> e : intersection.entrySet()) {

            Type type = e.getKey();

            Log.it(type);

        }

        Log.it("numPossiblePairs: "+numPossiblePairs);

        int numTries = 100000;

        BasicTypedXover xOver = new BasicTypedXover(1.0, 50, ch.getRandom());

        Map<String,Integer> combos = new TreeMap<>(QuerySolver.compareStrs);

        for (int i = 0; i < numTries; i++) {
            AA<PolyTree> children = xOver.xover(mum, dad);
            combos.merge( children.toString() , 1 , F::plus );
        }

        int sum = 0;

        for (Map.Entry<String,Integer> combo : combos.entrySet()) {
            int num = (int) Math.round(combo.getValue() / 1785.0);
            Log.it(num +" \t "+ combo.getKey());
            sum += num;
        }

        ch.it(sum, numPossiblePairs);


        ch.results();
    }

}
