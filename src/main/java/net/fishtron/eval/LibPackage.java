package net.fishtron.eval;

import net.fishtron.trees.Gamma;
import net.fishtron.types.Type;
import org.json.JSONObject;

/**
 * Created by tom on 23.11.2017.
 */
public class LibPackage {

    private final Type goal;
    private final Gamma gamma;
    private final EvalLib evalLib;
    private final JSONObject allParamsInfo;

    public LibPackage(Type goal, Gamma gamma, EvalLib evalLib, JSONObject allParamsInfo) {
        this.goal = goal;
        this.gamma = gamma;
        this.evalLib = evalLib;
        this.allParamsInfo = allParamsInfo;
    }

    public Type getGoal() { return goal; }
    public Gamma getGamma() { return gamma; }
    public EvalLib getEvalLib() { return evalLib; }

    public JSONObject getAllParamsInfo() {
        return allParamsInfo;
    }

    @Override
    public String toString() {
        return "LibPackage{" +
                "goal=" + goal +
                ", gamma=" + gamma +
                ", evalLib=" + evalLib +
                ", allParamsInfo=" + allParamsInfo +
                '}';
    }


}
