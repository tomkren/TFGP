package cz.tomkren.fishtron.ugen.apps.gpml;


import cz.tomkren.fishtron.ugen.multi.MultiEvalManager;
import cz.tomkren.fishtron.ugen.multi.MultiEvalResult;
import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import cz.tomkren.utils.AB;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONObject;

import java.util.List;

/**Created by tom on 07.03.2017.*/

public interface XmlRpcServer_MultiEvalManager<Indiv extends MultiIndiv> extends MultiEvalManager<Indiv> {

    JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException;
    int getCoreCount();
    Object submit(List<AB<Indiv,JSONObject>> indivs);
    MultiEvalResult<Indiv> getEvaluated();
    String quitServer();

    @Override
    default MultiEvalResult<Indiv> evalIndividuals(List<AB<Indiv, JSONObject>> indivs) {
        submit(indivs);
        return getEvaluated();
    }

    @Override
    default MultiEvalResult<Indiv> justAskForResults() {
        return getEvaluated();
    }

    @Override
    default int getEvalPoolSize(int suggestedPoolSize) {
        return getCoreCount();
    }
}
