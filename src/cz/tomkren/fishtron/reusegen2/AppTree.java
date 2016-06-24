package cz.tomkren.fishtron.reusegen2;

import com.google.common.base.Joiner;
import cz.tomkren.fishtron.terms.SmartSymbol;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.utils.AB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Created by tom on 21. 6. 2016.*/

public interface AppTree {

    class Leaf implements AppTree {
        private SmartSymbol sym;
        private Type type;

        private Leaf(SmartSymbol sym, Type type) {
            this.sym = sym;
            this.type = type;
        }

        @Override
        public String toString() {
            return sym.getName();
        }
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

        @Override
        public String toString() {
            AB<Leaf,List<AppTree>> p = getFunLeafWithArgs();
            return "("+p._1()+" "+Joiner.on(' ').join(p._2())+")";
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
    }

    static AppTree mk(SmartSymbol sym, Type type) {
        return new Leaf(sym, type);
    }

    static AppTree mk(AppTree funTree, AppTree argTree, Type type) {
        return new App(funTree, argTree, type);
    }



}
