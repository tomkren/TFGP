package cz.tomkren.fishtron.reusegen;


import cz.tomkren.fishtron.skeletons.SkeletonTree;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartLibrary;
import net.fishtron.types.TMap;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.utils.Log;

import java.util.*;
import java.util.function.BiFunction;

public class QuerySolver {

    private SmartLibrary lib;
    private Random rand;

    private Map<String,QueryResult> queries;

    private int nextVarId;

    private int numQueryCalls;
    private int numQueryResults;

    public QuerySolver(SmartLibrary lib, Random rand) {
        this.lib = lib;
        this.rand = rand;

        nextVarId = 0;
        queries = new HashMap<>();

        numQueryCalls = 0;
        numQueryResults = 0;
    }

    @Override
    public String toString() {
        return "QuerySolver{" +
                "lib=" + lib +
                ", rand=" + rand +
                ", queries=" + queries +
                ", nextVarId=" + nextVarId +
                ", numQueryCalls=" + numQueryCalls +
                ", numQueryResults=" + numQueryResults +
                '}';
    }

    public int getNextVarId() {return nextVarId;}
    public void setNextVarId(int nextVarId) {this.nextVarId = nextVarId;}
    public Random getRand() {return rand;}

    public QueryResult query(String type, int treeSize) {
        return query(new Query(Types.parse(type),treeSize));
    }

    public QueryResult query(Type type, int treeSize) {
        return query(new Query(type,treeSize));
    }

    public QueryResult query(Query q) {
        q.setSolver(this);
        String qKey = q.toString();
        QueryResult qResult = queries.get(qKey);
        if (qResult == null) {
            qResult = new QueryResult(q);
            queries.put(qKey, qResult);
            numQueryResults ++;
        }
        numQueryCalls ++;
        return qResult;
    }


    public static final Comparator<String> compareStrs = (s1, s2) -> {
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == len2) {return s1.compareTo(s2);}
        return Integer.compare(len1, len2);
    };

    public static final Comparator<PolyTree> compareTrees = (t1, t2) -> {
        int size1 = t1.getSize();
        int size2 = t2.getSize();
        if (size1 == size2) {return compareStrs.compare(t1.toString(), t2.toString());}
        return Integer.compare(size1, size2);
    };

    public List<PolyTree> simpleUniformGenerate(Type goalType, int maxTreeSize, int numToGenerate) {
        return uniformGenerate(this::generateOne, goalType, maxTreeSize, numToGenerate);
    }

    public List<PolyTree> simpleUniformGenerate(String goalType, int maxTreeSize, int numToGenerate) {
        return simpleUniformGenerate(Types.parse(goalType), maxTreeSize, numToGenerate);
    }

    public List<PolyTree> uniformGenerateWithRandomizedParams(String goalType, int maxTreeSize, int numToGenerate) {
        return uniformGenerateWithRandomizedParams(Types.parse(goalType), maxTreeSize, numToGenerate);
    }

    public List<PolyTree> uniformGenerateWithRandomizedParams(Type goalType, int maxTreeSize, int numToGenerate) {
        return uniformGenerate(this::generateOneWithRandomizedParams/*_notNull*/, goalType, maxTreeSize, numToGenerate);
    }

    public List<PolyTree> uniformGenerateWithParams(SkeletonTree skeletonTree, Type goalType, int maxTreeSize, int numToGenerate) {
        return uniformGenerate( (type,treeSize) -> this.generateOneWithParams(skeletonTree, type, treeSize) , goalType, maxTreeSize, numToGenerate);
    }

    // TODO přidat limit po kterym to nebude čekovat unikátnost !!!!!!!!!!!!!

    public List<PolyTree> uniformGenerate(BiFunction<Type,Integer,PolyTree> genOneFun, Type goalType, int maxTreeSize, int numToGenerate) {
        Set<PolyTree> treeSet = new TreeSet<>(compareTrees);

        while (treeSet.size() < numToGenerate) {

            int treeSize = rand.nextInt(maxTreeSize + 1);


            //Log.it("--------------------------------------------");
            //Log.it("treeSize: "+treeSize+" type: "+goalType);

            PolyTree tree = genOneFun.apply(goalType, treeSize);

            if (tree != null) {
                if (!treeSet.contains(tree)) {
                    treeSet.add(tree);
                }
            }
        }

        return new ArrayList<>(treeSet);
    }



    public PolyTree generateOneWithParams(SkeletonTree skeletonTree, Type goalType, int treeSize) {
        PolyTree treeWithDefaultParams = query(goalType, treeSize).generateOne(skeletonTree);
        return treeWithDefaultParams == null ? null : treeWithDefaultParams.randomizeAllParams(rand);
    }

    // todo #dirtySolve
    public PolyTree generateOneWithRandomizedParams_notNull(Type goalType, int treeSize) {
        PolyTree ret = null;
        while (ret == null) {
            ret = generateOneWithRandomizedParams(goalType, treeSize);
        }
        return ret;
    }

    public PolyTree generateOneWithRandomizedParams(Type goalType, int treeSize) {
        PolyTree treeWithDefaultParams = query(goalType, treeSize).generateOne();
        return treeWithDefaultParams == null ? null : treeWithDefaultParams.randomizeAllParams(rand);
    }



    public PolyTree generateOne(Type goalType, int treeSize) {
        return query(goalType, treeSize).generateOne();
    }

    public PolyTree generateOne(String goalType, int treeSize) {
        return generateOne(Types.parse(goalType), treeSize);
    }

    public TMap<PolyTree> generateAll(String goalType, int treeSize) {
        return query(goalType, treeSize).generateAll();
    }

    public TMap<PolyTree> generateAllUpTo(String goalType, int upToTreeSize) {
        Log.it("generateAllUpTo("+goalType+", "+upToTreeSize+")");
        TMap<PolyTree> ret = new TMap<>();
        for (int treeSize = 1; treeSize <= upToTreeSize; treeSize++) {
            QueryResult qRes = query(goalType, treeSize);
            Log.it("  treeSize = " + treeSize + ", num = " + qRes.getNum() + "\t\t" + "reuse-ratio: " + getReuseRatio());
            ret.add(qRes.generateAll());
        }
        return ret;
    }


    public SmartLibrary getLib() {return lib;}

    public String getReuseRatio() {
        return numQueryResults+"/"+numQueryCalls;
    }

}


