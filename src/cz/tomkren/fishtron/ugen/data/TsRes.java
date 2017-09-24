package cz.tomkren.fishtron.ugen.data;

import net.fishtron.types.Sub;
import cz.tomkren.fishtron.ugen.trees.AppTree;
import net.fishtron.utils.AB;

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

    public TsRes(AppTree tree, AB<Sub,Integer> moveRes) {
        this(tree, moveRes._1(), moveRes._2());
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
