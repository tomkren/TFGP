
package cz.tomkren.fishtron.latticegen;

import com.google.common.base.Joiner;
import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.AB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Created by user on 27. 7. 2016.*/

public interface AppTree {

    static AppTree mk(String sym, Type type) {
        return new AppTree.Leaf(sym, type);
    }

    static AppTree mk(AppTree funTree, AppTree argTree, Type type) {
        return new AppTree.App(funTree, argTree, type);
    }

    Type getType();
    void deskolemizeRootType();
    void specifyType(Sub sub);
    String toRawString();
    boolean isStrictlyWellTyped();

    class Leaf implements AppTree {
        private String sym;
        private Type type;

        private Leaf(String sym, Type type) {
            this.sym = sym;
            this.type = type;
        }

        @Override public Type getType() {return type;}
        @Override public void deskolemizeRootType() {type = type.deskolemize();}
        @Override public void specifyType(Sub sub) {type = sub.apply(type);}
        @Override public String toString() {return sym;}
        @Override public String toRawString() {return sym;}
        @Override public boolean isStrictlyWellTyped() {return true;}
    }

    class App implements AppTree {

        private AppTree funTree;
        private AppTree argTree;
        private Type type;

        private App(AppTree funTree, AppTree argTree, Type type) {
            this.funTree = funTree;
            this.argTree = argTree;
            this.type = type;
        }

        @Override public Type getType() {return type;}

        @Override
        public boolean isStrictlyWellTyped() {

            Type funType = funTree.getType();
            Type argType = argTree.getType();

            AA<Type> fun = Types.splitFunType(funType);

            return isSameType(fun._1(), argType) && isSameType(fun._2(), type);
        }

        private boolean isSameType(Type t1, Type t2) {
            return t1.toString().equals(t2.toString());
        }

        @Override
        public String toRawString() {
            return "("+funTree+" "+argTree+")";
        }

        @Override
        public String toString() {
            AB<AppTree.Leaf,List<AppTree>> p = getFunLeafWithArgs();
            return "("+p._1()+" "+ Joiner.on(' ').join(p._2())+")";
        }

        private AB<Leaf,List<AppTree>> getFunLeafWithArgs() {
            AppTree acc = this;
            List<AppTree> argTrees = new ArrayList<>();

            while (acc instanceof App) {
                App app = (App) acc;
                argTrees.add(app.argTree);
                acc = app.funTree;
            }

            Collections.reverse(argTrees);
            return new AB<>((Leaf)acc, argTrees);
        }

        @Override
        public void deskolemizeRootType() {
            type = type.deskolemize();
        }

        @Override
        public void specifyType(Sub sub) {
            type = sub.apply(type);
            funTree.specifyType(sub);
            argTree.specifyType(sub);
        }

    }

}
