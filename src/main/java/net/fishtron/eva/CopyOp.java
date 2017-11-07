package net.fishtron.eva;

import org.json.JSONObject;

import java.util.List;

public class CopyOp<Ind> implements Operator<Ind> {
    private double p;
    public CopyOp(double p) {this.p = p;}

    // todo odpreparovat a pak mk nahradit opts constructorem...
    @Deprecated
    public CopyOp(JSONObject config) {
        this(config.getJSONObject("copyOp").getDouble("probability"));
    }


    private JSONObject opts;
    @Override public JSONObject getOperatorInfo() {return opts;}

    private CopyOp(JSONObject opts, boolean fake_arg) {
        this(opts.getDouble("probability"));
        this.opts = opts;
    }

    public static <Indiv> CopyOp<Indiv> mk(JSONObject opts) {
        return new CopyOp<>(opts, true);
    }





    @Override public int getNumInputs() {return 1;}
    @Override public List<Ind> operate(List<Ind> parents) {return parents;}
    @Override public double getWeight() {return p;}
}
