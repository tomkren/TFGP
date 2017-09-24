package cz.tomkren.fishtron.reusegen2;

import com.google.common.base.Joiner;
import cz.tomkren.fishtron.terms.SmartSymbol;
import net.fishtron.types.Type;
import net.fishtron.utils.AB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Created by tom on 21. 6. 2016.*/

public interface ApplicationTree {

    class Leaf implements ApplicationTree {
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

    class App implements ApplicationTree {

        private ApplicationTree funTree;
        private ApplicationTree argTree;
        private Type type;

        private App(ApplicationTree funTree, ApplicationTree argTree, Type type) {
            this.funTree = funTree;
            this.argTree = argTree;
            this.type = type;
        }

        @Override
        public String toString() {
            AB<Leaf,List<ApplicationTree>> p = getFunLeafWithArgs();
            return "("+p._1()+" "+Joiner.on(' ').join(p._2())+")";
        }


        private AB<Leaf,List<ApplicationTree>> getFunLeafWithArgs() {
            ApplicationTree acc = this;
            List<ApplicationTree> argTrees = new ArrayList<>();

            while (acc instanceof App) {
                App app = (App) acc;
                argTrees.add(app.argTree);
                acc = app.funTree;
            }

            Collections.reverse(argTrees);
            return new AB<>((Leaf)acc, argTrees);
        }
    }

    static ApplicationTree mk(SmartSymbol sym, Type type) {
        return new Leaf(sym, type);
    }

    static ApplicationTree mk(ApplicationTree funTree, ApplicationTree argTree, Type type) {
        return new App(funTree, argTree, type);
    }



}
