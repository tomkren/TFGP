package cz.tomkren.fishtron.ugen.apps.gpml;


import net.fishtron.eva.multi.MultiEvalManager;
import net.fishtron.eva.multi.MultiEvalResult;
import net.fishtron.eva.multi.MultiIndiv;
import net.fishtron.utils.AB;
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
