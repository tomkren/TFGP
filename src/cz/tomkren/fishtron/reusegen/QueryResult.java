package cz.tomkren.fishtron.reusegen;


import com.google.common.base.Joiner;
import cz.tomkren.fishtron.skeletons.SkeletonTree;
import cz.tomkren.fishtron.terms.PolyTree;
import cz.tomkren.fishtron.terms.SmartSymbol;
import net.fishtron.types.Sub;
import net.fishtron.types.TMap;
import net.fishtron.types.Type;
import net.fishtron.utils.ABC;
import net.fishtron.utils.F;
import net.fishtron.utils.Listek;

import java.math.BigInteger;
import java.util.*;

/** Created by tom on 19. 6. 2015. */

public class QueryResult {

    private Query query;

    private List<AndGadget> andGadgets;

    private Map<Type,BigInteger> nums;
    private BigInteger num;


    public QueryResult(Query query) {

        /*if (query.toString().equals("((V LD (S (S 0)) Disj) => LD) ; 3")) {
            Log.it("BONDOVKA!");
        }*/

        this.query = query;

        andGadgets = new ArrayList<>();
        nums = new HashMap<>();

        List<SmartSymbol> symbols = query.getDadSym() == null ? query.getAllSyms() : query.getDadSym().getApplicableSons().get( query.getSonIndexInDad() );

        for (SmartSymbol sym : symbols) { // zde bývalo neefektivní: query.getAllSyms()

            int nextVarId = query.getSolver().getNextVarId();
            ABC<Type,List<Type>,Integer> freshResult = sym.freshenTypeVars(nextVarId);
            Type symOutType        = freshResult._1();
            List<Type> symArgTypes = freshResult._2();
            query.getSolver().setNextVarId(freshResult._3());

            Type goalType = query.getType();
            Sub sub = Sub.mgu(goalType, symOutType);

            if (!sub.isFail()) {
                List<List<Integer>> allSimpleProfiles = possibleSimpleProfiles(query.getTreeSize(), sym.getArity());

                for (List<Integer> simpleProfile : allSimpleProfiles) {

                    int numArgs = symArgTypes.size();
                    if (simpleProfile.size() != numArgs) {throw new Error("simpleProfile must have numArgs elements.");}

                    List<Query> sonQueries = new ArrayList<>(numArgs);
                    for (int i = 0; i < numArgs; i++) {
                        sonQueries.add(new Query(symArgTypes.get(i), simpleProfile.get(i), query, sym, i));
                    }


                    //Listek<Query> sonQueries = Listek.fromList( F.zipWith(symArgTypes, simpleProfile, (t,n)->new Query(t,n,query)) );

                    AndGadget ag = new AndGadget(query, sym, Listek.fromList(sonQueries), sub);

                    if (!F.isZero(ag.getNum())) {
                        andGadgets.add(ag);
                        AndGadget.mergeAllByAdd(nums,ag.getNums());
                    }
                }
            }
        }

        num = AndGadget.sum(nums);


    }

    public PolyTree generateOne() {

        if (F.isZero(num)) {
            return null;
        }

        BigInteger index = F.nextBigInteger(num, query.getRand());

        if (index == null) {throw new Error("Should be unreachable.");}

        for (AndGadget andGadget : andGadgets) {
            BigInteger numTrees = andGadget.getNum();

            if (index.compareTo(numTrees) < 0) {
                return andGadget.generateOne();
            }

            index = index.subtract(numTrees);
        }

        throw new Error("QueryResult.generateOne : This place should be unreachable!");
    }


    public PolyTree generateOne(SkeletonTree skeletonTree) {


        // TODO udělat ještě SymGadget at nemusí tolik sčítat
        BigInteger smallSum = BigInteger.ZERO;
        for (AndGadget andGadget : andGadgets)  {
            if (skeletonTree.isInRoot(andGadget.getSym().getName())) {
                smallSum = smallSum.add(andGadget.getNum());
            }
        }

        if (F.isZero(smallSum)) {return null;}

        BigInteger index = F.nextBigInteger(smallSum, query.getRand());
        if (index == null) {throw new Error("Should be unreachable.");}

        for (AndGadget andGadget : andGadgets) {
            BigInteger numTrees = andGadget.getNum();

            if (skeletonTree.isInRoot(andGadget.getSym().getName())) {
                if(index.compareTo(numTrees) < 0) {
                    return andGadget.generateOne(skeletonTree);
                }

                index = index.subtract(numTrees); // jen při OK symbolu
            }
        }


        throw new Error("QueryResult.generateOne("+skeletonTree+") : This place should be unreachable!");
    }

    public TMap<PolyTree> generateAll() {

        TMap<PolyTree> ret = new TMap<>();

        for (AndGadget andGadget : andGadgets) {
            ret.add(andGadget.generateAll());
        }

        return ret;
    }

    public BigInteger getNum() {return num;}
    public Map<Type,BigInteger> getNums() {return nums;}


    public static List<List<Integer>> possibleSimpleProfiles(int fatherSize, int numArgs) {

        int size = fatherSize - 1;
        List<List<Integer>> ret = new ArrayList<>();

        if (size < numArgs) {
            return ret;
        }

        // todo tady sem to dal nově
        if (numArgs == 0) {
            if (size == 0) {
                ret.add(Collections.emptyList());
            }
            return ret;
        }

        if (numArgs == 1) {
            ret.add(F.singleton(size));
        } else {

            int n = size - (numArgs - 1);
            for (int i = 1; i <= n; i++) {

                List<List<Integer>> subResults = possibleSimpleProfiles(size - i + 1, numArgs - 1);

                for (List<Integer> subResult : subResults) {
                    List<Integer> newResult = new ArrayList<>();
                    newResult.add(i);
                    newResult.addAll(subResult);
                    ret.add(newResult);
                }
            }

        }

        return ret;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "query=" + query +
                ", andGadgets=" + Joiner.on("\n").join(F.map(andGadgets, AndGadget::toString_short)) +
                ", nums=" + nums +
                ", num=" + num +
                '}';
    }
}