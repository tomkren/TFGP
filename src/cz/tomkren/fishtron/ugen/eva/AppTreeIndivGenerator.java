package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.eva.IndivGenerator;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import cz.tomkren.utils.Stopwatch;
import org.json.JSONObject;
import java.math.BigInteger;
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



    @Override
    public List<AppTreeIndiv> generate(int numToGenerate) {
        //vs: generate_randomSize(numToGenerate);
        Set<AppTree> treeSet = generate_cleverSizes(numToGenerate, goalType, maxTreeSize,gen, allParamsInfo, rand);
        return F.map(treeSet, tree -> new AppTreeIndiv(tree, lib));
    }

    // TODO zpřehlednit
    public static Set<AppTree> generate_cleverSizes(int numToGenerate, Type goalType, int maxTreeSize, Gen gen, JSONObject allParamsInfo, Random rand) {
        SortedSet<AppTree> treeSet = new TreeSet<>(AppTree.compareTrees);

        Stopwatch sw = new Stopwatch();

        int treeSize = 1;
        int numTriesForThisSize_init = maxTreeSize / 2;
        int numTriesForThisSize_remaining = numTriesForThisSize_init;
        int numSizes_remaining = 0;
        int numSizes_init = 1;

        int[] tab = new int[maxTreeSize+1];
        boolean restarted = false;
        boolean logFreshSizeGenerating = true;

        Stopwatch sw3 = new Stopwatch();

        while (treeSet.size() < numToGenerate) {


            if (numTriesForThisSize_remaining == 0) {

                Log.it(".. done generating treeSize "+treeSize+
                        ", now with "+ tab[treeSize]+" trees."+sw3.restart());
                logFreshSizeGenerating = true;
                treeSize ++;

                if (numSizes_remaining == 0) {
                    numTriesForThisSize_init = Math.max(1, numTriesForThisSize_init/2);
                    numSizes_init *= 2;
                    numSizes_remaining = numSizes_init;
                }

                numTriesForThisSize_remaining = numTriesForThisSize_init;
                numSizes_remaining --;
            }

            if (treeSize > maxTreeSize) {
                treeSize = 1;
                numTriesForThisSize_init = maxTreeSize / 2;
                numTriesForThisSize_remaining = numTriesForThisSize_init;
                numSizes_remaining = 0;
                numSizes_init = 1;
                restarted = true;
            }


            BigInteger num = null;

            if (tab[treeSize] == 0) {
                if (!restarted) {
                    Log.it_noln("  Building generating data for treeSize " + treeSize + " ..");
                }

                Stopwatch sw2 = new Stopwatch();
                num = gen.getNum(treeSize, goalType);

                if (!restarted) {
                    Log.it(".. done (num = "+num+")" + sw2.restart());
                }
            }

            if (num != null && F.isZero(num)) {
                Log.it("    Skipping empty treeSize "+treeSize);
                treeSize ++;
            } else {

                if (logFreshSizeGenerating) {
                    sw3 = new Stopwatch();
                    Log.it_noln("    Generating indiv trees for treeSize "+treeSize+" ..");
                    logFreshSizeGenerating = false;
                }

                numTriesForThisSize_remaining --;
                tab[treeSize]++;

                AppTree tree = gen.genOne(treeSize, goalType);

                if (tree == null) {throw new Error("null tree, should be unreachable");}

                AppTree randomizedTree = tree.randomizeParams(allParamsInfo, rand);
                treeSet.add(randomizedTree);
            }


        }

        if (!logFreshSizeGenerating) {
            Log.it(".. done generating treeSize "+treeSize+
                    ", now with "+ tab[treeSize]+" trees."+sw3.restart());
        }

        Log.it();
        Log.it("treeSize -> num individuals generated");

        for (int i = 1; i < tab.length; i++) {
            Log.it(i+" -> "+tab[i]);
        }

        Log.it("Generating "+treeSet.size()+" individuals took "+sw.getTime(3)+" sec.");

        return treeSet;
        //return F.map(treeSet, tree -> new AppTreeIndiv(tree, lib));
    }

    private List<AppTreeIndiv> generate_randomSize(int numToGenerate) {

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


    public static void main(String[] args) {

        int maxTreeSize = 64;

        int trees = 0;

        int treeSize = 1;
        int numTriesForThisSize_init = maxTreeSize / 2;
        int numTriesForThisSize_remaining = numTriesForThisSize_init;
        int numSizes_init = 1;
        int numSizes_remaining = 0;

        int[] tab = new int[65];

        while (trees < 256) {


            if (numTriesForThisSize_remaining == 0) {
                treeSize ++;
                if (numSizes_remaining == 0) {
                    numTriesForThisSize_init = Math.max(1, numTriesForThisSize_init/2);
                    numSizes_init *= 2;
                    numSizes_remaining = numSizes_init;
                }

                numSizes_remaining --;
                numTriesForThisSize_remaining = numTriesForThisSize_init;
            }

            if (treeSize > maxTreeSize) {
                treeSize = 1;
                numTriesForThisSize_init = maxTreeSize / 2;
                numTriesForThisSize_remaining = numTriesForThisSize_init;
                numSizes_init = 1;
                numSizes_remaining = 0;
            }

            //BigInteger num = gen.getNum(treeSize, goalType);


            if (treeSize > 1 && treeSize < 9) {
                treeSize ++;
            } else {
                tab[treeSize]++;
                numTriesForThisSize_remaining --;

                trees ++;
            }

        }


        for (int i = 0; i < tab.length; i++) {
            Log.it(i+" -> "+tab[i]);
        }

    }



}
