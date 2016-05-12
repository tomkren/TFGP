package cz.tomkren.fishtron.workflows;

import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.fishtron.eva.TogetherFitFun;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;

import com.martinpilat.DagEvalInterface;

import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class DataScientistFitness implements TogetherFitFun {

    private final DagEvalInterface evaluator;
    private final String datasetFile;
    private final boolean onlyNonNegative;


    public DataScientistFitness(String datasetFile) {this("http://127.0.0.1:8080", datasetFile,true);}

    public DataScientistFitness(String url, String datasetFile, boolean onlyNonNegative) {
        this.datasetFile = datasetFile;
        this.onlyNonNegative = onlyNonNegative;

        try {

            evaluator = new DagEvalInterface(url);

        } catch (MalformedURLException e) {
            throw new Error(e);
        }

    }

    public JSONObject getAllParamsInfo() {
        try {
            String json = evaluator.getMethodParams(datasetFile);
            Log.it(json);
            return new JSONObject(json);
        } catch (XmlRpcException e) {
            throw new Error(e);
        }
    }

    public JSONObject getAllParamsInfo_mayThrowUp() throws XmlRpcException {

        String json = evaluator.getMethodParams(datasetFile);
        Log.itln("allParamsInfo = "+ json);
        return new JSONObject(json);

    }

    public void killServer() {
        evaluator.killServer();
    }

    @Override
    public List<FitVal> getFitVals(List<Object> os) {

        String populationInJson = F.list(os).map(o->(TypedDag)o).reduce(TypedDag::toJson);

        //Log.it(populationInJson); // TODO logovat do souboru zde asi

        try {

            Log.it_noln("Evaluating ...");
            List<double[]> scores = evaluator.eval(populationInJson, datasetFile);


            if (scores.size() != os.size()) {
                throw new Error("There must be same number of individuals and fitness values! "+ scores.size() +" != "+ os.size()  );
            }

            Log.itln("... [OK]");


            List<FitVal> fitVals = new ArrayList<>(scores.size());
            int i = 0;
            for (double[] scoreArr : scores) {

                double score = 0.0;
                if (scoreArr.length == 0) {
                    System.err.println("Evaluation error !!!");
                    System.err.println(((TypedDag) os.get(i)).toJson());

                } else {
                    score = scoreArr[0];
                }

                if (onlyNonNegative && score < 0.0) {
                    System.err.println("Warning: Score < 0 ... "+score);
                    //System.err.println(((TypedDag) os.get(i)).toJson());
                    score = 0.0;
                }

                fitVals.add(new FitVal.Basic(score));

                i++;
            }

            return fitVals;

            /*return F.map(scores, scoreArr -> {

                double score = 0.0;
                if (scoreArr.length == 0) {
                    System.err.println("Evaluation error !!!");
                } else {
                    score = scoreArr[0];
                }

                return new FitVal.Basic(score);
            });*/


        } catch (XmlRpcException e) {
            throw new Error(e);
        }

    }
}
