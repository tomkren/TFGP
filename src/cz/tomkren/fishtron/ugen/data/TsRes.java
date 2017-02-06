package cz.tomkren.fishtron.ugen.data;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.ugen.AppTree;

/** Created by Tomáš Křen on 6.2.2017. */

public class TsRes {

    private final AppTree tree;
    private final Sub sigma;
    private final int nextVarId;

    public TsRes(AppTree tree, Sub sigma, int nextVarId) {
        this.tree = tree;
        this.sigma = sigma;
        this.nextVarId = nextVarId;
    }

    public AppTree getTree() {return tree;}
    public Sub getSigma() {return sigma;}
    public int getNextVarId() {return nextVarId;}

    @Override
    public String toString() {
        return "TsRes{" +
                "tree=" + tree +
                ", sigma=" + sigma +
                ", nextVarId=" + nextVarId +
                '}';
    }
}
