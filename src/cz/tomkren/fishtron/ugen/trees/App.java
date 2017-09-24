package cz.tomkren.fishtron.ugen.trees;

import com.google.common.base.Joiner;
import cz.tomkren.fishtron.terms.SubtreePos;
import net.fishtron.types.Sub;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import net.fishtron.utils.AA;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/** Created by tom on 18.03.2017. */

public class App implements AppTree {

    private AppTree funTree;
    private AppTree argTree;
    private Type type;

    private Type originalType;
    private JSONObject debugInfo;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        cz.tomkren.fishtron.ugen.trees.App app = (cz.tomkren.fishtron.ugen.trees.App) o;
        return funTree.equals(app.funTree) && argTree.equals(app.argTree);
    }

    @Override
    public int hashCode() {
        int result = funTree.hashCode();
        result = 31 * result + argTree.hashCode();
        return result;
    }

    App(AppTree funTree, AppTree argTree, Type type) {
        this(funTree, argTree, type, type, null);
    }

    private App(AppTree funTree, AppTree argTree, Type type, Type originalType, JSONObject debugInfo) {
        this.funTree = funTree;
        this.argTree = argTree;
        this.type = type;
        this.originalType = originalType;
        this.debugInfo = debugInfo;
    }

    @Override
    public Sexpr toSexpr() {
        AB<Leaf, List<AppTree>> funWithArgs = getFunLeafWithArgs();
        Leaf funLeaf = funWithArgs._1();
        List<AppTree> args = funWithArgs._2();
        return new Sexpr(funLeaf.getSym(), F.map(args, AppTree::toSexpr));
    }

    @Override
    public boolean hasParams() {
        return false;
    }

    @Override
    public AppTree randomizeParams(JSONObject allParamsInfo, Random rand) {
        AppTree newFunTree = funTree.randomizeParams(allParamsInfo, rand);
        AppTree newArgTree = argTree.randomizeParams(allParamsInfo, rand);
        return new App(newFunTree, newArgTree, type, originalType, debugInfo);
    }

    @Override
    public AppTree getSubtree(SubtreePos pos) {
        if (pos.isRoot()) {
            return this;
        } else {
            AppTree subtree = (pos.getSonIndex() == 0 ? funTree : argTree);
            return subtree.getSubtree(pos.getTail());
        }
    }

    @Override
    public AppTree changeSubtree(SubtreePos pos, AppTree newSubtree) {
        if (pos.isRoot()) {
            return newSubtree;
        } else {
            SubtreePos tailPos = pos.getTail();
            if (pos.getSonIndex() == 0) {
                AppTree newFunTree = funTree.changeSubtree(tailPos, newSubtree);
                return new App(newFunTree, argTree, type);
            } else {
                AppTree newArgTree = argTree.changeSubtree(tailPos, newSubtree);
                return new App(funTree, newArgTree, type);
            }
        }
    }

    @Override
    public List<SubtreePos> getAllSubtreePosesWhere(Predicate<AppTree> isTrue) {
        List<SubtreePos> ret = new ArrayList<>();

        if (isTrue.test(this)) {
            ret.add(SubtreePos.root(type));
        }

        List<SubtreePos> funPoses = funTree.getAllSubtreePosesWhere(isTrue);
        ret.addAll(F.map(funPoses, pos -> SubtreePos.reverseStep(0, pos)));

        List<SubtreePos> argPoses = argTree.getAllSubtreePosesWhere(isTrue);
        ret.addAll(F.map(argPoses, pos -> SubtreePos.reverseStep(1, pos)));

        return ret;
    }

    public AppTree getFunTree() {
        return funTree;
    }

    public AppTree getArgTree() {
        return argTree;
    }

    @Override
    public Type getOriginalType() {
        return originalType;
    }


    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int size() {
        return funTree.size() + argTree.size();
    }

    @Override
    public AB<Boolean, Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId) {

        if (isRootStrictlyWellTyped()) {
            AB<Boolean, Integer> funTreeRes = funTree.isStrictlyWellTyped(gammaMap, nextVarId);
            if (funTreeRes._1()) {
                return argTree.isStrictlyWellTyped(gammaMap, funTreeRes._2());
            }
        }

        return AB.mk(false, null);

        /*boolean isRootSWT = isRootStrictlyWellTyped();
        AB<Boolean,Integer> funTreeRes = funTree.isStrictlyWellTyped(gammaMap, nextVarId);
        AB<Boolean,Integer> argTreeRes = argTree.isStrictlyWellTyped(gammaMap, funTreeRes._2());

        boolean isSWT = isRootSWT && funTreeRes._1() && argTreeRes._1();
        return AB.mk(isSWT, argTreeRes._2());*/
    }

    private boolean isRootStrictlyWellTyped() {
        Type funType = funTree.getType();
        Type argType = argTree.getType();
        AA<Type> fun = Types.splitFunType(funType);
        return Types.isSameType(fun._1(), argType) && Types.isSameType(fun._2(), type);
    }

    @Override
    public JSONObject getTypeTrace() {
        JSONObject typeTrace = F.obj(
                "node", "@",
                "type", type.toJson(),
                "fun", funTree.getTypeTrace(),
                "arg", argTree.getTypeTrace()
        );

        if (!isRootStrictlyWellTyped()) {
            typeTrace.put("error", true);
        }

        if (debugInfo != null) {
            typeTrace.put("debugInfo", debugInfo);
        }

        return typeTrace;
    }

    /*private boolean isSameType(Type t1, Type t2) {
        return t1.toString().equals(t2.toString());
    }*/

    @Override
    public String toRawString() {
        return "(" + funTree.toRawString() + " " + argTree.toRawString() + ")";
    }

    @Override
    public String toString() {
        AB<Leaf, List<AppTree>> p = getFunLeafWithArgs();
        return "(" + p._1() + " " + Joiner.on(' ').join(p._2()) + ")";
    }

    public String toShortString() {
        AB<Leaf, List<AppTree>> p = getFunLeafWithArgs();
        String head = p._1().toShortString();
        List<String> tail = F.map(p._2(), AppTree::toShortString);
        return "(" + head + " " + Joiner.on(' ').join(tail) + ")";
    }

    private AB<Leaf, List<AppTree>> getFunLeafWithArgs() {
        List<AppTree> argTrees = new ArrayList<>();

        AppTree acc = this;
        while (acc instanceof App) {
            App app = (App) acc;
            argTrees.add(app.argTree);
            acc = app.funTree;
        }

        Collections.reverse(argTrees);
        return new AB<>((Leaf) acc, argTrees);
    }

    @Override
    public String toStringWithTypes() {
        return "(<" + type + "> " +
                funTree.toStringWithTypes() + " " +
                argTree.toStringWithTypes() + ")";
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
    public AppTree applySub_new(Sub sub) {
        Type newType = sub.apply(type);
        AppTree newFunTree = funTree.applySub_new(sub);
        AppTree newArgTree = argTree.applySub_new(sub);
        return new App(newFunTree, newArgTree, newType, originalType, debugInfo);
    }

    @Override
    public void applyTypeTransform(Function<Type, Type> tt) {
        type = tt.apply(type);
        funTree.applyTypeTransform(tt);
        argTree.applyTypeTransform(tt);
    }

    @Override
    public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
        debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
    }
}
