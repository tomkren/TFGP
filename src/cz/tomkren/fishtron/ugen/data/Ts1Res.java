package cz.tomkren.fishtron.ugen.data;

import net.fishtron.types.Sub;
import net.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.utils.AB;

import java.math.BigInteger;

/** Created by user on 2. 2. 2017.*/

public class Ts1Res {

    private final String s;
    private final Sub sigma;
    private final int nextVarId;

    public Ts1Res(String s, Sub sigma, int nextVarId) {
        this.s = s;
        this.sigma = sigma;
        this.nextVarId = nextVarId;
    }

    public Ts1Res(String s, AB<Sub,Integer> moveRes) {
        this(s, moveRes._1(), moveRes._2());
    }

    public String getSym() {return s;}
    public Sub getSigma() {return sigma;}
    public int getNextVarId() {return nextVarId;}

    public SubsRes toSubsRes() {
        return new SubsRes(BigInteger.ONE, sigma, nextVarId);
    }

    public PreSubsRes toPreSubsRes() {
        return new PreSubsRes(BigInteger.ONE, sigma);
    }


    public TsRes toTsRes(Type t) {
        AppTree symLeaf = AppTree.mk(s, sigma.apply(t));
        return new TsRes(symLeaf, sigma, nextVarId);
    }


    @Override
    public String toString() {
        return "Ts1Res{" +
                "s='" + s + '\'' +
                ", sigma=" + sigma +
                ", nextVarId=" + nextVarId +
                '}';
    }
}
