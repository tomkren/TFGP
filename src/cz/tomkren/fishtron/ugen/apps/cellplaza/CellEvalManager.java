package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.eval.EvalLib;
import cz.tomkren.fishtron.ugen.multi.MultiEvalManager;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.TODO;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 20.03.2017. */

class CellEvalManager<Indiv extends MultiIndiv> implements MultiEvalManager<Indiv> {

    private final EvalLib lib;
    private final boolean dummyMode;
    private final int poolSize;

    CellEvalManager(EvalLib lib, int poolSize, boolean dummyMode) {
        this.lib = lib;
        this.dummyMode = dummyMode;
        this.poolSize =poolSize;
    }

    @Override
    public MultiEvalResult<Indiv> evalIndividuals(List<AB<Indiv, JSONObject>> indivs) {
        throw new TODO();
    }

    @Override
    public MultiEvalResult<Indiv> justAskForResults() {
        throw new TODO();
    }

    @Override
    public int getEvalPoolSize(int suggestedPoolSize) {
        return poolSize;
    }
}
