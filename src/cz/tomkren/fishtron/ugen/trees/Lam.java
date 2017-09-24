package cz.tomkren.fishtron.ugen.trees;

import cz.tomkren.fishtron.terms.SubtreePos;
import net.fishtron.types.Sub;
import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.utils.AA;
import net.fishtron.utils.AB;
import cz.tomkren.utils.TODO;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**Created by tom on 18.03.2017.*/

public class Lam implements AppTree {

    private String varName;
    private AppTree bodyTree;
    private Type type;

    private Type originalType;
    private JSONObject debugInfo;


    public Lam(String varName, AppTree bodyTree, Type type, Type originalType, JSONObject debugInfo) {
        this.varName = varName;
        this.bodyTree = bodyTree;
        this.type = type;
        this.originalType = originalType;
        this.debugInfo = debugInfo;

        if (!Types.isFunType(type)) {
            throw new Error("Lambda function must have function type!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lam lam = (Lam) o;
        return varName.equals(lam.varName) && bodyTree.equals(lam.bodyTree);
    }

    @Override
    public int hashCode() {
        int result = varName.hashCode();
        result = 31 * result + bodyTree.hashCode();
        return result;
    }

    @Override
    public Sexpr toSexpr() {
        throw new TODO("Lambda Sexprs are not yet supported.");
    }

    @Override
    public AppTree randomizeParams(JSONObject allParamsInfo, Random rand) {
        AppTree newBodyTree = bodyTree.randomizeParams(allParamsInfo, rand);
        return new Lam(varName, newBodyTree, type, originalType, debugInfo);
    }

    @Override
    public boolean hasParams() {
        return false;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Type getOriginalType() {
        return originalType;
    }

    @Override
    public int size() {
        return 1 + bodyTree.size();
    }

    @Override
    public void deskolemize(Set<Integer> ids) {
        type = type.deskolemize(ids);
        bodyTree.deskolemize(ids);
        // TODO pořádně se zamyslet že fakt ok
    }

    @Override
    public void applySub(Sub sub) {
        type = sub.apply(type);
        bodyTree.applySub(sub);
        // TODO pořádně se zamyslet že fakt ok
    }

    @Override
    public AppTree applySub_new(Sub sub) {
        Type newType = sub.apply(type);
        AppTree newBodyTree = bodyTree.applySub_new(sub);
        return new Lam(varName, newBodyTree, newType, originalType, debugInfo);
        // TODO pořádně se zamyslet že fakt ok
    }

    @Override
    public void applyTypeTransform(Function<Type, Type> tt) {
        type = tt.apply(type);
        bodyTree.applyTypeTransform(tt);
        // TODO pořádně se zamyslet že fakt ok
    }


    private Type getArgType() {
        AA<Type> typeParts = Types.splitFunType(type);
        return typeParts._1();
    }

    private static String showLambdaFun(String head, String body) {
        return "(λ "+head+" . "+body+")";
    }

    @Override
    public String toRawString() {
        return showLambdaFun(varName, bodyTree.toRawString());
    }

    @Override
    public String toShortString() {
        return showLambdaFun(varName, bodyTree.toShortString());
    }

    @Override
    public String toStringWithTypes() {
        return showLambdaFun(varName+" : "+getArgType(), bodyTree.toStringWithTypes());
    }

    @Override
    public AB<Boolean, Integer> isStrictlyWellTyped(Map<String, Type> gammaMap, int nextVarId) {
        throw new TODO("Define isStrictlyWellTyped(...) of a lambda function!"); // TODO !!!
    }

    @Override
    public JSONObject getTypeTrace() {
        throw new TODO("Define getTypeTrace() of a lambda function!"); // TODO !!!
    }

    @Override
    public List<SubtreePos> getAllSubtreePosesWhere(Predicate<AppTree> isTrue) {
        throw new TODO("Define getAllSubtreePosesWhere(...) of a lambda function!"); // TODO !!!
    }

    @Override
    public AppTree getSubtree(SubtreePos pos) {
        throw new TODO("Define getSubtree(...) of a lambda function!"); // TODO !!!
    }

    @Override
    public AppTree changeSubtree(SubtreePos pos, AppTree newSubtree) {
        throw new TODO("Define changeSubtree(...) of a lambda function!"); // TODO !!!
    }

    @Override
    public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
        debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
    }
}
