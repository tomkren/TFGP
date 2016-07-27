package cz.tomkren.fishtron.reusegen2;


import com.google.common.collect.Lists;
import cz.tomkren.fishtron.terms.SmartSymbol;
import cz.tomkren.fishtron.types.*;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.ABC;
import cz.tomkren.utils.F;

import java.math.BigInteger;
import java.util.*;

/** Created by tom on 20. 6. 2016. */

class TypeGadget {

    private Type type;
    private Map<Integer,AB<List<SubTypeInfo>,BigInteger>> infos; // TreeSize -> SubtypeInfo

    TypeGadget(Type t) {
        type = t;
        infos = new HashMap<>();
    }

    List<SubTypeInfo> getInfos(int treeSize, QSolver qs) {return query(treeSize,qs)._1();}
    BigInteger        getNum  (int treeSize, QSolver qs) {return query(treeSize,qs)._2();}

    private AB<List<SubTypeInfo>,BigInteger> query(int treeSize, QSolver qs) {
        return F.getOrMkAndPut(infos, treeSize, ()-> mkQueryInfosAndNum(treeSize,qs));
    }

    List<ApplicationTree> generateAll(int treeSize, QSolver qs) {

        List<ApplicationTree> ret = new ArrayList<>();

        for (SubTypeInfo info : getInfos(treeSize, qs)) {
            ret.addAll(info.generateAll(qs));
        }

        return ret;

    }

    ApplicationTree generateOne(int treeSize, QSolver qs) {

        AB<List<SubTypeInfo>,BigInteger> infoPair = query(treeSize, qs);

        if (infoPair == null) {
            throw new Error("TypedGadget.generateOne: TypeGadget("+Types.prettyPrint(type)+") for treeSize="+treeSize+"\n"+
                            "is supposed to have same infos, but it has none.");
        }

        List<SubTypeInfo> infos = infoPair._1();
        BigInteger num = infoPair._2();

        BigInteger index = F.nextBigInteger(num, qs.getRand());

        if (index == null) {throw new Error("Should be unreachable.");}

        for (SubTypeInfo info : infos) {
            BigInteger numTrees = info.getNum();

            if (index.compareTo(numTrees) < 0) {
                return info.generateOne(qs);
            }

            index = index.subtract(numTrees);
        }

        throw new Error("TypeGadget.generateOne : This place should be unreachable!");
    }

    private AB<List<SubTypeInfo>,BigInteger> mkQueryInfosAndNum(int treeSize, QSolver qs) {
        List<SubTypeInfo> infos = mkQueryInfos(treeSize, qs);
        BigInteger num = computeNum(infos);
        return new AB<>(infos, num);
    }

    private List<SubTypeInfo> mkQueryInfos(int treeSize, QSolver qs) {
        if (treeSize <= 0) {throw new Error("treeSize must be > 0, but it is: "+treeSize);}
        return treeSize == 1 ? mkLeafQueryInfos(qs) : mkAppQueryInfos(treeSize, qs);
    }

    private BigInteger computeNum(List<SubTypeInfo> infos) {
        return F.sumBigInteger(F.map(infos, SubTypeInfo::getNum));
    }

    private List<SubTypeInfo> mkLeafQueryInfos(QSolver qs) {

        List<SubTypeInfo> ret = new ArrayList<>();

        for (SmartSymbol sym : qs.getLib().getSyms()) {


            Type symType = mkSymType(sym, type.getNextVarId());
            Sub sub = Sub.mgu(symType, type);

            //log("  Trying to match: "+symType+" with "+type+" ... "+sub);

            if (!sub.isFail()) {
                Type newType = QSolver.normalizeType(sub.apply(symType)); // TODO pořádně promyslet
                ret.add(new SubTypeInfo.Leaf(newType, BigInteger.ONE, Collections.singletonList(sym)));
            }

        }

        ret = mergeSameTypes(ret);

        log("Making infos for: ("+Types.prettyPrint(type)+", "+1+")");
        logList(F.map(ret,s->"   "+s));

        return ret;
    }


    private List<SubTypeInfo> mkAppQueryInfos(int treeSize, QSolver qs) {

        List<SubTypeInfo> ret = new ArrayList<>();

        Type argType = new TypeVar(type.getNextVarId());
        Type funType = Types.mkFunType(argType, type);


        for (int funTreeSize = 1 ; funTreeSize < treeSize; funTreeSize++) {
            int argTreeSize = treeSize - funTreeSize;

            List<SubTypeInfo> funSubInfos = qs.getInfos(funType, funTreeSize);

            for (SubTypeInfo funSubInfo : funSubInfos) {
                Type funSubtype = funSubInfo.getReturnType();

                // todo promyslet...
                //Sub sub = Sub.mgu(funSubtype, funType);
                //Type argSubtype = sub.apply(argType);
                Type argSubtype = Types.splitFunType(funSubtype)._1();


                List<SubTypeInfo> argSubInfos = qs.getInfos(argSubtype, argTreeSize);

                BigInteger numFuns = funSubInfo.getNum();

                for (SubTypeInfo argSubInfo : argSubInfos) {
                    BigInteger newNum = numFuns.multiply(argSubInfo.getNum());
                    //Type newType = sub.apply(funSubtype); // TODO pořádně promyslet
                    ret.add(new SubTypeInfo.App(/*newType*/funSubtype, funTreeSize, argTreeSize, newNum));
                }
            }
        }

        ret = mergeSameTypes(ret);

        log("Making infos for: ("+Types.prettyPrint(type)+", "+treeSize+")");
        logList(F.map(ret,s->"   "+s));

        return ret;
    }


    private static List<SubTypeInfo> mergeSameTypes(List<SubTypeInfo> infos) {
        Map<String,SubTypeInfo> auxMap = new HashMap<>();
        for (SubTypeInfo info : infos) {
            auxMap.merge(info.getWholeType().toString(), info, TypeGadget::mergeInfos);
        }

        return new ArrayList<>(auxMap.values());
    }

    private static SubTypeInfo mergeInfos(SubTypeInfo info1, SubTypeInfo info2) {
        info1.merge(info2);
        return info1;
    }


    private static Type mkSymType(SmartSymbol sym, int nextVarId) {
        ABC<Type,List<Type>,Integer> ps = sym.freshenTypeVars(nextVarId);

        Type acc = ps._1();
        List<Type> argTypes = ps._2();

        for (Type argType : Lists.reverse(argTypes)) {
            acc = Types.mkFunType(argType,acc);
        }

        return acc;
    }

    private static void log(Object x) {
        //Log.it(x);
    }

    private static void logList(List<?> xs) {
        //Log.list(xs);
    }

}
