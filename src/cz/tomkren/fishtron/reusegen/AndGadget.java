package cz.tomkren.fishtron.reusegen;


import cz.tomkren.fishtron.skeletons.SkeletonTree;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartSymbol;
import net.fishtron.types.Sub;
import net.fishtron.types.TMap;
import net.fishtron.types.Type;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Listek;
import cz.tomkren.utils.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** Created by user on 19. 6. 2015. */

public class AndGadget {

    private Query dadQuery;
    private SmartSymbol sym;
    private Listek<Query> sonQueries;
    private Sub sub;

    private Map<Type,BigInteger> nums;
    private BigInteger num;

    private TMap<PolyTree> allTrees;

    public SmartSymbol getSym() {return sym;}


    public AndGadget(Query dadQuery, SmartSymbol sym, Listek<Query> sonQueries, Sub sub) {
        this.dadQuery = dadQuery;
        this.sym = sym;
        this.sonQueries = sonQueries;
        this.sub = sub;

        nums = computeNums(sonQueries, sub, BigInteger.ONE);
        num = sum(nums);

        allTrees = null;
    }

    private Map<Type,BigInteger> computeNums(Listek<Query> queries, Sub sub, BigInteger acc) {

        if (F.isZero(acc)) {throw new Error("acc should never be zero!");}

        if (queries == null) {
            Type originalType = dadQuery.getType();
            Type rootType = sub.apply(originalType);

            Map<Type,BigInteger> ret = new HashMap<>();
            ret.put(rootType, acc);
            return ret;
        }

        Query sonQuery = new Query(sub, queries.getHead());
        QueryResult sonQueryResult = getSolver().query(sonQuery);

        Map<Type,BigInteger> ret = new HashMap<>();

        if (F.isZero(sonQueryResult.getNum())) {return ret;}

        for (Map.Entry<Type,BigInteger> e : sonQueryResult.getNums().entrySet()) {

            Type moreSpecificType = e.getKey();
            BigInteger numSonTrees = e.getValue();

            Sub sonSpecificSub = Sub.mgu( moreSpecificType, sonQuery.getType() );

            Sub newSub = Sub.dot(sonSpecificSub,sub);
            BigInteger newAcc = numSonTrees.multiply(acc);

            Map<Type,BigInteger> subRet = computeNums(queries.getTail(), newSub, newAcc);
            mergeAllByAdd(ret,subRet);
        }

        return ret;
    }

    public PolyTree generateOne() {

        List<Query> sonQs = Listek.toList(sonQueries);
        List<PolyTree> sons = new ArrayList<>(sonQs.size());

        Sub locSub = sub;

        //Log.it("and-gadget sym: "+ getSym() );
        //int i = 0;

        for (Query sq : sonQs) {
            Query sonQuery = new Query(locSub, sq);

            //Log.it("  sonQuery("+i+"): "+sonQuery+" for "+getSym() );

            PolyTree sonTree = getSolver().query(sonQuery).generateOne();

            if (sonTree == null) {
                Log.err("Warning! (#dyrtySolve) sonQuery returning null sonTree: "+ sonQuery);
                //todo #dyrtySolve
                return null;

                //throw new Error("Should be unreachable!");
            }

            Type moreSpecificType = sonTree.getType();
            Sub sonSpecificSub = Sub.mgu(moreSpecificType, sonQuery.getType());
            locSub = Sub.dot(sonSpecificSub, locSub);

            sons.add(sonTree);

            //i++;
        }

        PolyTree newTree = sym.mkTree(dadQuery.getType(), sons);
        newTree.applySub(locSub);
        return newTree;
    }

    public PolyTree generateOne(SkeletonTree skeletonTree) {

        List<Query> sonQs = Listek.toList(sonQueries);
        List<PolyTree> sons = new ArrayList<>(sonQs.size());

        Sub locSub = sub;

        int i = 0;
        for (Query sq : sonQs) {
            Query sonQuery = new Query(locSub, sq);

            PolyTree sonTree;
            List<SkeletonTree> skeletonSons = skeletonTree.getSons();
            if (skeletonSons.isEmpty()) {
                sonTree = getSolver().query(sonQuery).generateOne();
            } else {
                sonTree = getSolver().query(sonQuery).generateOne(skeletonSons.get(i));
            }

            if (sonTree == null) {return null;}

            Type moreSpecificType = sonTree.getType();
            Sub sonSpecificSub = Sub.mgu(moreSpecificType, sonQuery.getType());
            locSub = Sub.dot(sonSpecificSub, locSub);

            sons.add(sonTree);

            i++;
        }

        PolyTree newTree = sym.mkTree(dadQuery.getType(), sons);
        newTree.applySub(locSub);
        return newTree;
    }


    public TMap<PolyTree> generateAll() {
        if (allTrees == null) {
            allTrees = generateAll(sonQueries, sub, Listek.mkSingleton(null));
        }
        return allTrees;
    }

    private TMap<PolyTree> generateAll(Listek<Query> locSonQueries, Sub locSub, Listek<Listek<PolyTree>> acc) {

        if (locSonQueries == null) {

            TMap<PolyTree> ret = new TMap<>();

            Type originalType = dadQuery.getType();
            Type rootType = locSub.apply(originalType);

            for (Listek<PolyTree> sons : Listek.toList(acc)) {
                PolyTree newTree = sym.mkTree(rootType, Listek.toReverseList(sons) );
                ret.add(rootType, newTree);
            }

            return ret;
        }

        Query sonQuery = new Query(locSub, locSonQueries.getHead());
        TMap<PolyTree> sonResult = getSolver().query(sonQuery).generateAll();

        TMap<PolyTree> ret = new TMap<>();

        if (sonResult.isEmpty()) {return ret;}

        for (Map.Entry<Type,List<PolyTree>> e : sonResult.entrySet()) {

            Type moreSpecificType = e.getKey();
            List<PolyTree> sonTrees = e.getValue();

            Sub sonSpecificSub = Sub.mgu( moreSpecificType, sonQuery.getType() );
            Sub newSub = Sub.dot(sonSpecificSub, locSub);

            // newAcc vznikne obohacením acc o sonTrees
            Listek<Listek<PolyTree>> newAcc = null;
            for (Listek<PolyTree> preArgs : acc.toList()) {
                for (PolyTree sonTree : sonTrees) {
                    newAcc = Listek.mk( Listek.mk(sonTree,preArgs) , newAcc);
                }
            }

            TMap<PolyTree> subRet = generateAll(locSonQueries.getTail(), newSub, newAcc);

            ret.add(subRet);
        }

        return ret;
    }

    public Map<Type, BigInteger> getNums() {return nums;}
    public BigInteger getNum() {return num;}


    public static <K> BigInteger sum(Map<K,BigInteger> mapa) {
        BigInteger sum = BigInteger.ZERO;
        for (Map.Entry<K,BigInteger> e : mapa.entrySet()) {
            sum = sum.add( e.getValue() );
        }
        return sum;
    }

    public static <K> void mergeAllByAdd(Map<K,BigInteger> target, Map<K,BigInteger> source) {
        mergeAll(target, source, BigInteger::add);
    }

    public static <K,V> void mergeAll(Map<K,V> target, Map<K,V> source, BiFunction<? super V, ? super V, ? extends V> f) {
        for (Map.Entry<K,V> e : source.entrySet()) {
            target.merge(e.getKey(), e.getValue(), f);
        }
    }


    public QuerySolver getSolver() {
        return dadQuery.getSolver();
    }


    public String toString_short() {
        return "AndGadget{\n" +
                " dadQuery=" + dadQuery +
                ",\n sym=" + sym +
                ",\n sonQueries=" + sonQueries +
                ",\n sub=" + sub +
                ",\n nums=" + nums +
                ",\n num=" + num +
                ",\n allTrees= (...)" +
                "}\n";
    }

    @Override
    public String toString() {
        return "AndGadget{\n" +
                " dadQuery=" + dadQuery +
                ",\n sym=" + sym +
                ",\n sonQueries=" + sonQueries +
                ",\n sub=" + sub +
                ",\n nums=" + nums +
                ",\n num=" + num +
                ",\n allTrees=" + allTrees +
                "}\n";
    }
}