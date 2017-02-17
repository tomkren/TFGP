package cz.tomkren.fishtron.ugen.apps.workflows;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.mains.DagEvaTester;
import cz.tomkren.fishtron.sandbox2.Dag_IEvalManager;
import cz.tomkren.fishtron.sandbox2.EvalResult;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.TODO;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 17. 2. 2017.*/

public class DummyManager<Indiv extends FitIndiv> implements Dag_IEvalManager<Indiv> {

    List<AB<Indiv, JSONObject>> submitIndivs;
    private int nextId;

    public DummyManager() {
        submitIndivs = null;
        nextId = 0;
    }

    @Override
    public JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException {
        return DagEvaTester.testParamsInfo;
    }

    @Override
    public int getCoreCount() {
        return 16;
    }

    @Override
    public Object submit(List<AB<Indiv, JSONObject>> indivs) {
        submitIndivs = indivs;

        throw new TODO();

        //return null;
    }

    @Override
    public EvalResult<Indiv> getEvaluated() {
        throw new TODO();
    }

    @Override
    public String quitServer() {
        return "Timmmmmmmy!";
    }
}
