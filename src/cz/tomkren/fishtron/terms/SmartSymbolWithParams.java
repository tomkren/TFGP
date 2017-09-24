package cz.tomkren.fishtron.terms;

import cz.tomkren.fishtron.eva.Distribution;
import net.fishtron.utils.AB;
import cz.tomkren.utils.Comb0;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/** Created by tom on 7.11.2015.*/

public class SmartSymbolWithParams extends SmartSymbol {

    private final Function<JSONObject,Comb0> params2code;
    private final JSONObject paramsInfo;
    private final Map<String,Integer> selectedParamIndices;


    public SmartSymbolWithParams(SmartSymbol protoSym, Function<JSONObject,Comb0> params2code, JSONObject paramsInfo, Map<String,Integer> selectedParamIndices) {
        super(protoSym);
        this.params2code = params2code;
        this.paramsInfo = paramsInfo;
        this.selectedParamIndices = selectedParamIndices;
    }

    @Override
    public Comb0 getCode() {
        JSONObject params = getParams();
        return params2code.apply(params);
    }

    @Override
    public String getNameWithParams() {
        return getName() + getParams().toString();
    }

    public JSONObject getParams() {
        JSONObject params = new JSONObject();

        if (selectedParamIndices != null) {
            for (Map.Entry<String,Integer> e : selectedParamIndices.entrySet()) {
                String paramName = e.getKey();
                int selectedIndex = e.getValue();
                params.put(paramName, paramsInfo.getJSONArray(paramName).get(selectedIndex) );
            }
        }

        return params;
    }

    public int numParams() {
        return  selectedParamIndices == null ? 0 : selectedParamIndices.size();
    }


    public SmartSymbolWithParams randomizeAllParams(Random rand) {

        Map<String,Integer> newIndices = new HashMap<>();

        for (Object oKey : paramsInfo.keySet()) {
            String key = (String) oKey;

            JSONArray possibleValues = paramsInfo.getJSONArray(key);
            int newIndex = rand.nextInt(possibleValues.length());

            newIndices.put(key, newIndex);
        }

        return new SmartSymbolWithParams(this, params2code, paramsInfo, newIndices);
    }

    public SmartSymbolWithParams randomlyShiftOneParam(Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {

        if (selectedParamIndices.isEmpty()) {return this;}

        String paramName = F.list(selectedParamIndices.entrySet()).randomElement(rand).getKey();

        //Log.it(" >>>>>>>>>> "+getName()+" -> "+ paramName);

        Map<String,Integer> newIndices = new HashMap<>(selectedParamIndices);

        newIndices.compute(paramName, (k,currentIndex)-> {
            int numValues =  paramsInfo.getJSONArray(paramName).length();
            Distribution<AB<Integer,Double>> shiftDist = mkShiftDistribution(shiftsWithProbabilities, currentIndex, numValues);
            return currentIndex + (shiftDist.isEmpty() ? 0 : shiftDist.get(rand)._1());
        });

        return new SmartSymbolWithParams(this, params2code, paramsInfo, newIndices);
    }

    private static Distribution<AB<Integer,Double>> mkShiftDistribution(List<AB<Integer,Double>> shiftsWithProbabilities, int currentIndex, int numValues) {
        Distribution<AB<Integer,Double>> ret = new Distribution<>();

        for (AB<Integer,Double> shiftWithProbability: shiftsWithProbabilities) {
            int resultIndex = currentIndex + shiftWithProbability._1();
            if (resultIndex >= 0 && resultIndex < numValues) {
                ret.add(shiftWithProbability);
            }
        }

        return ret;
    }


    /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    public static void main(String[] args) {
        Checker ch = new Checker();
        Random r = ch.getRandom();

        String testAllParamsInfo = "{\"DT\":{\"max_features\":[0.05,0.1,0.25,0.5,0.75,1],\"criterion\":[\"gini\",\"entropy\"],\"min_samples_split\":[1,2,5,10,20],\"max_depth\":[1,2,5,10,15,25,50,100],\"min_samples_leaf\":[1,2,5,10,20]},\"SVC\":{\"tol\":[1.0E-4,0.001,0.01],\"C\":[0.1,0.5,1,2,5,10,15],\"gamma\":[0,1.0E-4,0.001,0.01,0.1,0.5]},\"kMeans\":{},\"union\":{},\"copy\":{},\"kBest\":{\"k\":[1,3,6,9,12]},\"gaussianNB\":{},\"logR\":{\"tol\":[1.0E-4,0.001,0.01],\"C\":[0.1,0.5,1,2,5,10,15],\"penalty\":[\"l1\",\"l2\"],\"solver\":[\"newton-cg\",\"lbfgs\",\"liblinear\"]},\"vote\":{},\"PCA\":{\"n_components\":[1,3,6,9,12],\"whiten\":[false,true]}}";

        JSONObject allParams = new JSONObject(testAllParamsInfo);
        JSONObject pcaParams = allParams.getJSONObject("PCA");

        Log.it(pcaParams.toString(2));

        String name = "PCA";
        Type type = Types.parse("D => D");

        Function<JSONObject,Comb0> params2comb = params -> (haxTypeInput -> {
            Type t = (Type) haxTypeInput.get(0);
            AA<Type> p = TypedDag.getBoxInOutTypes(t);
            //Log.it("BOND HERE! "+params);
            return new TypedDag(name, p._1(), p._2(), params);
        });


        CodeNodeWithParams node = new CodeNodeWithParams(new ProtoNode(name, type),params2comb,pcaParams, null);

        ch.it( node.getParams() , "{}" );

        ch.it( ((TypedDag)node.getCode().compute1(type)).toJson() );

        for (int i = 0; i < 10; i++) {
            Log.it("---------------------------------------------------------");
            CodeNodeWithParams node2 = node.randomizeAllParams(r);
            ch.it(node2.getParams() );
            ch.it( ((TypedDag)node2.getCode().compute1(type)).toJson() );
        }


        ch.results();
    }*/


}
