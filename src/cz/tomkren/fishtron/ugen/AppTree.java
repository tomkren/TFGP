package cz.tomkren.fishtron.ugen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import com.google.common.base.Joiner;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;

/** Created by user on 27. 7. 2016.*/

// TODO opakujou se tu kody, předelat na abstract dědičnost asi...

public interface AppTree {

    static AppTree mk(String sym, Type type) {
        return new AppTree.Leaf(sym, type);
    }

    static AppTree mk(AppTree funTree, AppTree argTree, Type type) {
        return new AppTree.App(funTree, argTree, type);
    }

    static void writeErrorTreeToFile(JSONObject typeTrace) {
        F.writeJsonAsJsFile("www/data/lastErrTree.js", "mkLastErrTree", typeTrace);
    }

    Type getType();
    void deskolemize(Set<Integer> ids);
    void applySub(Sub sub);
    void applyTypeTransform(Function<Type,Type> tt);
    String toRawString();

    AB<Boolean,Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId);
    JSONObject getTypeTrace();

    default boolean isStrictlyWellTyped(Gamma gamma) {
        Map<String,Type> gammaMap = new HashMap<>();
        for(AB<String,Type> p : gamma.getSymbols()) {
            gammaMap.put(p._1(),p._2());
        }
        return isStrictlyWellTyped(gammaMap, 0)._1();
    }

    void updateDebugInfo(Function<JSONObject,JSONObject> updateFun);

    class Leaf implements AppTree {
        private String sym;
        private Type type;
        private JSONObject debugInfo;

        private Leaf(String sym, Type type) {
            this.sym = sym;
            this.type = type;
            this.debugInfo = null;
        }

        @Override public Type getType() {return type;}
        @Override public void deskolemize(Set<Integer> ids) {type = type.deskolemize(ids);}
        @Override public void applySub(Sub sub) {type = sub.apply(type);}
        @Override public void applyTypeTransform(Function<Type, Type> tt) {type = tt.apply(type);}
        @Override public String toString() {return sym;}
        @Override public String toRawString() {return sym;}

        @Override public AB<Boolean,Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId) {
            Type t_s = gammaMap.get(sym);

            if (t_s == null) {
                return AB.mk(false, type.getNextVarId(nextVarId));
            }

            Fresh freshRes = new Fresh(t_s,type,nextVarId);
            Type t_s_fresh     = freshRes.getFreshType();
            int  t_s_nextVarId = freshRes.getNextVarId();

            Sub mgu = Sub.mgu(type, t_s_fresh); // todo: Není úplně podle definice, ale pokud ex mgu, tak určitě i substituce ex (tzn silnější). Do-potvrdit si, že je to ok.

            return AB.mk(!mgu.isFail(), t_s_nextVarId);
        }

        @Override public JSONObject getTypeTrace() {
            JSONObject typeTrace = F.obj("node",sym, "type",type.toJson());
            if (debugInfo != null) {typeTrace.put("debugInfo", debugInfo);}
            return typeTrace;
        }

        @Override
        public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
            debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
        }
    }

    class App implements AppTree {

        private AppTree funTree;
        private AppTree argTree;
        private Type type;
        private JSONObject debugInfo;


        private App(AppTree funTree, AppTree argTree, Type type) {
            this.funTree = funTree;
            this.argTree = argTree;
            this.type = type;
            this.debugInfo = null;
        }

        @Override public Type getType() {return type;}

        @Override
        public AB<Boolean,Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId) {

            boolean isRootSWT = isRootStrictlyWellTyped();
            AB<Boolean,Integer> funTreeRes = funTree.isStrictlyWellTyped(gammaMap, nextVarId);
            AB<Boolean,Integer> argTreeRes = argTree.isStrictlyWellTyped(gammaMap, funTreeRes._2());

            boolean isSWT = isRootSWT && funTreeRes._1() && argTreeRes._1();
            return AB.mk(isSWT, argTreeRes._2());
        }

        private boolean isRootStrictlyWellTyped() {
            Type funType = funTree.getType();
            Type argType = argTree.getType();
            AA<Type> fun = Types.splitFunType(funType);
            return isSameType(fun._1(), argType) && isSameType(fun._2(), type);
        }

        @Override
        public JSONObject getTypeTrace() {
            JSONObject typeTrace = F.obj(
                    "node","@",
                    "type",type.toJson(),
                    "fun",funTree.getTypeTrace(),
                    "arg",argTree.getTypeTrace()
            );

            if (!isRootStrictlyWellTyped()) {
                typeTrace.put("error",true);
            }

            if (debugInfo != null) {
                typeTrace.put("debugInfo", debugInfo);
            }

            return typeTrace;
        }

        private boolean isSameType(Type t1, Type t2) {
            return t1.toString().equals(t2.toString());
        }

        @Override
        public String toRawString() {
            return "("+funTree.toRawString()+" "+argTree.toRawString()+")";
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
        public void deskolemize(Set<Integer> ids) {
            type = type.deskolemize(ids);
            funTree.deskolemize(ids);
            argTree.deskolemize(ids);
        }

        @Override
        public void applySub(Sub sub) {
            type = sub.apply(type);
            funTree.applySub(sub);
            argTree.applySub(sub);
        }

        @Override
        public void applyTypeTransform(Function<Type,Type> tt) {
            type = tt.apply(type);
            funTree.applyTypeTransform(tt);
            argTree.applyTypeTransform(tt);
        }

        @Override
        public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
            debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
        }
    }

}
