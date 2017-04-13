package cz.tomkren.fishtron.ugen.trees;

import cz.tomkren.fishtron.terms.SubtreePos;
import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Fresh;
import cz.tomkren.fishtron.ugen.params.Params;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/** Created by tom on 18.03.2017. */

public class Leaf implements AppTree {

    private String sym;
    private Type type;
    private Params params;

    private Type originalType;
    private JSONObject debugInfo;

    Leaf(String sym, Type type) {
        this(sym, type, type, null, null);
    }

    private Leaf(String sym, Type type, Type originalType, JSONObject debugInfo, Params params) {
        this.sym = sym;
        this.type = type;
        this.originalType = originalType;
        this.debugInfo = debugInfo;
        this.params = params;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        cz.tomkren.fishtron.ugen.trees.Leaf leaf = (cz.tomkren.fishtron.ugen.trees.Leaf) o;
        return sym.equals(leaf.sym) && (params != null ? params.equals(leaf.params) : leaf.params == null);
    }

    @Override
    public int hashCode() {
        int result = sym.hashCode();
        result = 31 * result + (params != null ? params.hashCode() : 0);
        return result;
    }

    @Override
    public boolean hasParams() {
        return params != null;
    }

    public Params getParams() {
        return params;
    }

    @Override
    public AppTree randomizeParams(JSONObject allParamsInfo, Random rand) {
        JSONObject paramsInfo = allParamsInfo.has(sym) ? allParamsInfo.getJSONObject(sym) : null;
        if (paramsInfo == null) {
            return this;
        } else {
            return new Leaf(sym, type, originalType, debugInfo, new Params(paramsInfo, rand));
        }
    }

    Leaf randomlyShiftOneParam(Random rand, List<AB<Integer, Double>> shiftsWithProbabilities) {
        Params newParams = params.randomlyShiftOneParam(rand, shiftsWithProbabilities);
        return new Leaf(sym, type, originalType, debugInfo, newParams);
    }

    @Override
    public AppTree getSubtree(SubtreePos pos) {
        return pos.isRoot() ? this : null;
    }

    @Override
    public AppTree changeSubtree(SubtreePos pos, AppTree newSubtree) {
        return pos.isRoot() ? newSubtree : null;
    }


    @Override
    public List<SubtreePos> getAllSubtreePosesWhere(Predicate<AppTree> isTrue) {
        if (isTrue.test(this)) {
            return Collections.singletonList(SubtreePos.root(type));
        } else {
            return Collections.emptyList();
        }
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
        return 1;
    }

    @Override
    public void deskolemize(Set<Integer> ids) {
        type = type.deskolemize(ids);
    }

    @Override
    public void applySub(Sub sub) {
        type = sub.apply(type);
    }

    @Override
    public AppTree applySub_new(Sub sub) {
        return new Leaf(sym, sub.apply(type), originalType, debugInfo, params);
    }

    @Override
    public void applyTypeTransform(Function<Type, Type> tt) {
        type = tt.apply(type);
    }

    @Override
    public String toString() {
        if (params == null) {
            return sym;
        } else {
            return sym + params.toJson().toString();
        }
    }

    @Override
    public String toRawString() {
        return sym;
    }

    public String toShortString() {
        return sym;
    }

    public String getSym() {
        return sym;
    }

    @Override
    public String toStringWithTypes() {
        return "<" + sym + ":" + type + ">";
    }

    @Override
    public AB<Boolean, Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId) {
        Type t_s = gammaMap.get(sym);

        if (t_s == null) {
            return AB.mk(false, null);
        }

        Fresh freshRes = new Fresh(t_s, type, nextVarId);
        Type t_s_fresh = freshRes.getFreshType();
        int t_s_nextVarId = freshRes.getNextVarId();

        Sub mgu = Sub.mgu(type, t_s_fresh); // todo: Není úplně podle definice, ale pokud ex mgu, tak určitě i substituce ex (tzn silnější). Do-potvrdit si, že je to ok.

        return AB.mk(!mgu.isFail(), t_s_nextVarId);
    }

    @Override
    public JSONObject getTypeTrace() {
        JSONObject typeTrace = F.obj("node", sym, "type", type.toJson());
        if (debugInfo != null) {
            typeTrace.put("debugInfo", debugInfo);
        }
        return typeTrace;
    }

    @Override
    public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
        debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
    }
}
