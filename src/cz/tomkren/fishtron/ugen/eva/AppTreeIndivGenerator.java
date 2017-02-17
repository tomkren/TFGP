package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.utils.F;
import cz.tomkren.utils.TODO;
import org.json.JSONObject;

import java.util.*;

/**  Created by tom on 17. 2. 2017.*/

public class AppTreeIndivGenerator implements IndivGenerator<AppTreeIndiv> {

    private final Type goalType;
    private final int maxTreeSize;
    private final Gen gen;
    private final EvalLib lib;
    private final JSONObject allParamsInfo;
    private final Random rand;

    public AppTreeIndivGenerator(Type goalType, int maxTreeSize, Gen gen, EvalLib lib, JSONObject allParamsInfo) {
        this.goalType = goalType;
        this.maxTreeSize = maxTreeSize;
        this.gen = gen;
        this.lib = lib;
        this.allParamsInfo = allParamsInfo;
        this.rand = gen.getRand();
    }

    /* todo | Procházet velikosti stromů asi systematicky
       todo | a předtim čeknout num a ani se nesnažit přiště ho generovat tu velikost
       todo | a nečekovat tree ale i s parametrama na unikatnost!
       todo |
       todo |
    * */

    @Override
    public List<AppTreeIndiv> generate(int numToGenerate) {

        SortedSet<AppTree> treeSet = new TreeSet<>(AppTree.compareTrees);

        while (treeSet.size() < numToGenerate) {

            int treeSize = rand.nextInt(maxTreeSize + 1);

            AppTree tree = gen.genOne(treeSize, goalType);

            if (tree != null) {

                AppTree randomizedTree = tree.randomizeParams(allParamsInfo, rand);

                treeSet.add(randomizedTree);
            }



        }

        return F.map(treeSet, tree -> new AppTreeIndiv(tree, lib));
    }





}
