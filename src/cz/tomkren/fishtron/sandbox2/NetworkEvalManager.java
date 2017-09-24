package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.fishtron.eva.FitVal;
import cz.tomkren.fishtron.sandbox.JsonEvalInterface;
import net.fishtron.utils.AB;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;

/** Created by user on 10. 6. 2016.*/

public class NetworkEvalManager<Indiv extends FitIndiv> implements EvalManager<Indiv> {

    private JsonEvalInterface evaluator;
    private String evalMethodName;
    private String poolSizeMethodName;
    private Function<Object,Object> toJsonObject;

    private Map<Integer, AB<Indiv,JSONObject>> id2indiv;
    private int nextId;


    public NetworkEvalManager(String poolSizeMethodName,String evalMethodName, String evaluatorURL, Function<Object,Object> toJsonObject) {
        this.poolSizeMethodName = poolSizeMethodName;
        this.evalMethodName = evalMethodName;
        this.toJsonObject = toJsonObject;
        evaluator = new JsonEvalInterface(evaluatorURL);

        id2indiv = new HashMap<>();
        nextId = 1;
    }

    @Override
    public int getEvalPoolSize(int suggestedPoolSize) {
        return evaluator.evalObject(poolSizeMethodName, suggestedPoolSize, x->(int)x);
    }

    @Override
    public EvalResult<Indiv> evalIndividuals(List<AB<Indiv,JSONObject>> indivs) {

        JSONArray jsonIndivs = new JSONArray();
        List<JSONObject> indivObjs = new ArrayList<>(indivs.size());

        for (AB<Indiv,JSONObject> indivData : indivs) {

            Indiv      indiv     = indivData._1();
            JSONObject indivJson = indivData._2();

            id2indiv.put(nextId, indivData);
            indivJson.put("id",nextId);

            Object indivValue = indiv.computeValue();
            Object jsonCode = toJsonObject.apply(indivValue);

            JSONObject indivDataToSubmit = F.obj(
                "id",   nextId,
                "code", jsonCode
            );

            indivObjs.add(indivJson);
            jsonIndivs.put(indivDataToSubmit);

            nextId++;
        }



        List<Indiv> someEvaluatedIndivs = evaluator.eval(evalMethodName, jsonIndivs, this::getIndivBack);

        return () -> F.zip(someEvaluatedIndivs, indivObjs);
    }

    private Indiv getIndivBack(Object evalRes) {
        Object[] evalResArr = (Object[]) evalRes;

        int    id    = (int)    evalResArr[0];
        double score = (double) evalResArr[1];

        FitVal fitVal = new FitVal.Basic(score, isPerfect(score));
        AB<Indiv,JSONObject> indivData = id2indiv.remove(id);

        if (indivData == null) {throw new Error("EvalResult for individual with non-existing id "+id+"!");}

        Indiv indiv = indivData._1();
        JSONObject indivJson = indivData._2();

        indivJson.put("fitness", score);
        indiv.setFitVal(fitVal);

        return indiv;
    }

    /* TODO score == perfectScore*/
    private boolean isPerfect(double score) {
        return false;
    }

    public static void main(String[] args) {

        // TESTING...
        Checker ch = new Checker();

        NetworkEvalManager<Test_FakeAnt> em = new NetworkEvalManager<>("getEvalPoolSize","evalAnts_2", "http://localhost:4242/", x->x);

        ch.it("evalPoolSize: "+em.getEvalPoolSize(2));

        String antStr1 = "[\"ifa\", \"m\", \"r\"]";
        String antStr2 = "[\"ifa\", \"m\", [\"pr3\", \"l\", [\"pr2\", [\"ifa\", \"m\", \"r\"], [\"pr2\", \"r\", [\"pr2\", \"l\", \"r\"]]], [\"pr2\", [\"ifa\", \"m\", \"l\"], \"m\"]]]";


        Test_FakeAnt ant1 = new Test_FakeAnt(antStr1);
        Test_FakeAnt ant2 = new Test_FakeAnt(antStr2);

        List<AB<Test_FakeAnt,JSONObject>> ants = Arrays.asList(new AB<>(ant1, F.obj()),new AB<>(ant2, F.obj()));

        EvalResult<Test_FakeAnt> res = em.evalIndividuals(ants);

        for(Test_FakeAnt ind : res.getIndividuals()) {
            Log.it("score:"+ ind.getWeight() + " indiv: "+ind.computeValue());
        }

        ch.it( F.map(res.getIndividuals(), Test_FakeAnt::getWeight), "[11.0, 89.0]");
        ch.it(em.id2indiv.size() , 0);
        ch.it(em.nextId, 3);

        ch.results();
    }

    private static class Test_FakeAnt implements FitIndiv {
        private FitVal fv;
        private JSONArray val;

        Test_FakeAnt(String json) {val = new JSONArray(json);}
        @Override public FitVal getFitVal() {return fv;}
        @Override public void setFitVal(FitVal fitVal) {fv = fitVal;}
        @Override public Object computeValue() {return val;}
        @Override public double getWeight() {return fv.getVal();}
    }
}
