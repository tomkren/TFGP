package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
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
    private final boolean silent;
    private final Random rand;

    public AppTreeIndivGenerator(Type goalType, int maxTreeSize, Gen gen, EvalLib lib,
                                 JSONObject allParamsInfo, boolean silent) {
        this.goalType = goalType;
        this.maxTreeSize = maxTreeSize;
        this.gen = gen;
        this.lib = lib;
        this.allParamsInfo = allParamsInfo;
        this.silent = silent;
        this.rand = gen.getRand();
    }

    public AppTreeIndivGenerator(Type goalType, int maxTreeSize, Gen gen, EvalLib lib, JSONObject allParamsInfo) {
        this(goalType, maxTreeSize, gen, lib, allParamsInfo, true);
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

        int numGenerated = 0;
        while (treeSet.size() < numToGenerate) {

            int treeSize = 1 + rand.nextInt(maxTreeSize);

            AppTree tree = gen.genOne(treeSize, goalType);

            if (tree != null) {

                AppTree randomizedTree = tree.randomizeParams(allParamsInfo, rand);


                if (!silent) {
                    if (!treeSet.contains(randomizedTree)) {
                        numGenerated ++;
                        Log.it(numGenerated);
                    }
                }

                treeSet.add(randomizedTree);
            }



        }

        return F.map(treeSet, tree -> new AppTreeIndiv(tree, lib));
    }





}
