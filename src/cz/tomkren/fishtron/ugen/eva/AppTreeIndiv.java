package cz.tomkren.fishtron.ugen.eva;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.eval.EvalLib;

/** Created by user on 15. 2. 2017. */

public class AppTreeIndiv implements FitIndiv {

    private final AppTree tree;
    private final EvalLib lib; // todo pokud to pude elegantně, tak odebrat a radši mít jen jednou v evaluatoru
    private FitVal fitVal;

    public AppTreeIndiv(AppTree tree, EvalLib lib) {
        this.tree = tree;
        this.lib = lib;
        this.fitVal = null;
    }

    public AppTree getTree() {
        return tree;
    }

    public EvalLib getLib() {
        return lib;
    }

    @Override
    public Object computeValue() {
        return lib.eval(tree);
    }

    @Override
    public FitVal getFitVal() {
        return fitVal;
    }

    @Override
    public void setFitVal(FitVal fitVal) {
        this.fitVal = fitVal;
    }


}
