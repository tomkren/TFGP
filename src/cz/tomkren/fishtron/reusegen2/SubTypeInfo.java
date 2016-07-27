package cz.tomkren.fishtron.reusegen2;

import cz.tomkren.fishtron.terms.SmartSymbol;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.F;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Created by tom on 20. 6. 2016. */


interface SubTypeInfo {

    Type getReturnType();
    Type getWholeType();
    BigInteger getNum();
    void merge(SubTypeInfo info);

    ApplicationTree generateOne(QSolver qs);
    List<ApplicationTree> generateAll(QSolver qs);


    class Leaf implements SubTypeInfo {

        private Type type;
        private BigInteger num;
        private List<SmartSymbol> symbols;

        Leaf(Type type, BigInteger num, List<SmartSymbol> symbols) {
            this.type = type;
            this.num = num;
            this.symbols = symbols;
        }

        @Override
        public ApplicationTree generateOne(QSolver qs) {
            SmartSymbol sym = F.randomElement(symbols, qs.getRand());
            return mkTree(sym);
        }

        @Override
        public List<ApplicationTree> generateAll(QSolver qs) {
            return F.map(symbols, this::mkTree);
        }

        private ApplicationTree mkTree(SmartSymbol sym) {
            return ApplicationTree.mk(sym, type);
        }

        @Override public Type getReturnType() {return type;}
        @Override public Type getWholeType() {return type;}

        @Override public BigInteger getNum() {return num;}

        @Override
        public void merge(SubTypeInfo info) {
            if (info instanceof Leaf) {
                Leaf leaf = (Leaf) info;
                if (type.equals(leaf.type)) {

                    num = num.add(leaf.num);
                    symbols.addAll(leaf.symbols);

                } else {
                    throw new Error("merge error: types must be equal");
                }
            } else {
                throw new Error("merge error: both infos must be leafs");
            }
        }

        @Override
        public String toString() {
            return "Leaf{" +
                    "type=" + Types.prettyPrint(type) +
                    ", num=" + num +
                    ", symbols=" + F.map(symbols, SmartSymbol::getName) +
                    '}';
        }
    }

    class App implements SubTypeInfo {

        private Type argType;
        private Type returnType;

        private int funTreeSize;
        private int argTreeSize;

        private BigInteger num;

        App(Type funType, int funTreeSize, int argTreeSize, BigInteger num) {

            AA<Type> p = Types.splitFunType(funType);

            this.argType = p._1();
            this.returnType = p._2();

            this.funTreeSize = funTreeSize;
            this.argTreeSize = argTreeSize;

            this.num = num;
        }


        @Override
        public ApplicationTree generateOne(QSolver qs) {

            Type funType = getWholeType();
            ApplicationTree funTree = qs.generateOne(funType, funTreeSize);
            ApplicationTree argTree = qs.generateOne(argType, argTreeSize);

            return ApplicationTree.mk(funTree, argTree, returnType);
        }

        @Override
        public List<ApplicationTree> generateAll(QSolver qs) {

            Type funType = getWholeType();
            List<ApplicationTree> funTrees = qs.generateAll(funType, funTreeSize);
            List<ApplicationTree> argTrees = qs.generateAll(argType, argTreeSize);

            List<ApplicationTree> ret = new ArrayList<>();

            for (ApplicationTree funTree : funTrees) {
                for (ApplicationTree argTree : argTrees) {
                    ApplicationTree newTree = ApplicationTree.mk(funTree, argTree, returnType);
                    ret.add(newTree);
                }
            }

            return ret;
        }

        @Override public Type getReturnType() {return returnType;}
        @Override public Type getWholeType() {return Types.mkFunType(argType, returnType);}
        @Override public BigInteger getNum() {return num;}

        @Override
        public void merge(SubTypeInfo info) {
            if (info instanceof App) {
                App app = (App) info;

                if (getWholeType().equals(app.getWholeType())) {

                    num = num.add(app.num);


                } else {
                    throw new Error("merge error: types must be equal");
                }
            } else {
                throw new Error("merge error: both infos must be leafs");
            }
        }

        @Override
        public String toString() {
            return "App{" +
                    "argType=" + Types.prettyPrint(argType) +
                    ", returnType=" + Types.prettyPrint(returnType) +
                    ", funTreeSize=" + funTreeSize +
                    ", argTreeSize=" + argTreeSize +
                    ", num=" + num +
                    '}';
        }
    }


}
