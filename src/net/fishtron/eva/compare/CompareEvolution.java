package net.fishtron.eva.compare;

import net.fishtron.eva.Operator;
import net.fishtron.eva.multi.MultiIndiv;
import net.fishtron.eva.multi.MultiLogger;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**Created by tom on 22.03.2017.
 *
 * Evaluation is done only in selection when comparing two randomly selected individuals from population.
 *
 * */

public class CompareEvolution<Indiv extends MultiIndiv> {

    private CompareOpts<Indiv> opts;
    private MultiLogger<Indiv> logger;

    private ComparePopulation<Indiv> population;
    //private int numSentIndividuals;
    private int numEvaluations;

    private long startTime;
    private int run;

    public CompareEvolution(CompareOpts<Indiv> opts, MultiLogger<Indiv> logger) {
        this.opts = opts;
        this.logger = logger;
    }

    public void start() {
        start(1);
    }

    private void start(int run) {
        this.run = run;
        startTime = System.nanoTime();

        makeEmptyPopulation();

        List<AB<Indiv,JSONObject>> initIndivs = generateIndividuals();
        updatePopulation(initIndivs);

        while (isEvaluationUnfinished()) {

            Operator<Indiv> operator = opts.getOperators().get(opts.getRandom());
            List<Indiv> parents = selectParents(operator.getNumInputs());

            List<AB<Indiv,JSONObject>> children = makeChildren(operator,  parents);
            updatePopulation(children);

            //if (isSendingNeeded()) { ... to nahore
            //} else {// todo just ask for results zatim bych rozbehal že nehrotí do zásoby pač pak je celá populace v tom porovnávači a ja nerozhoduju kdo přide na řadu asi}
        }
    }


    private void updatePopulation(List<AB<Indiv,JSONObject>> indivsWithParentInfo) {
        logger.log(run, numEvaluations, () -> indivsWithParentInfo);
        population.addIndividuals(F.map(indivsWithParentInfo, AB::_1));
    }


    private void makeEmptyPopulation() {
        population = new ComparePopulation<>(opts.getMaxPopulationSize());
        //numSentIndividuals = 0;
        numEvaluations = 0;
    }

    private boolean isEvaluationUnfinished() {
        double runTimeInSeconds = (System.nanoTime()-startTime)/1E9;
        boolean stillSomeTime = opts.getTimeLimit() - runTimeInSeconds > 0.0;
        return stillSomeTime && numEvaluations < opts.getNumEvaluations();
    }


    private List<AB<Indiv,JSONObject>> generateIndividuals() {
        opts.getChecker().log("Generating initial population....");
        List<Indiv> genIndivs = opts.getGenerator().generate(opts.getNumIndividualsToGenerate());
        return F.map(genIndivs, indiv -> new AB<>(indiv, mkIndivJson_forGenerated()));
    }

    /*private boolean isSendingNeeded() {
        return numSentIndividuals < opts.getNumEvaluations();
    }*/

    private List<Indiv> selectParents(int numParents) {

        CompareSelection<Indiv> parentSelection = opts.getParentSelection();
        List<Indiv> parents = new ArrayList<>(numParents);

        for (int i = 0; i < numParents; i++) {
            Indiv selectedParent = parentSelection.select(population.getPopulation(), opts::compareIndividuals);
            numEvaluations++; // 1 selection <==> 1 evaluation
            parents.add(selectedParent);
        }
        return parents;
    }

    private List<AB<Indiv,JSONObject>> makeChildren(Operator<Indiv> operator,  List<Indiv> parents) {
        List<Indiv> chs = operator.operate(parents);
        List<AB<Indiv,JSONObject>> chsWithParentInfo = F.map(chs, ch -> new AB<>(ch, mkIndivJson(operator, parents)));
        int maxNumChildren = opts.getNumEvaluations() - numEvaluations;  // - numSentIndividuals;
        return F.take(maxNumChildren, chsWithParentInfo);
    }






    private static JSONObject mkIndivJson_forGenerated() {
        return F.obj(
                "operator", F.obj("name","generator", "generated",true),
                "parents",  F.arr()
        );
    }

    private JSONObject mkIndivJson(Operator<Indiv> operator, List<Indiv> parents) {
        return F.obj(
                "operator", operator.getOperatorInfo(),
                "parents", F.jsonMap(parents, this::mkParentInfo)
        );
    }

    private JSONObject mkParentInfo(Indiv parent) {
        return F.obj(
                //"fitness", F.jsonMap(parent.getFitness()), // TODO tady se to roseklo protože nemaj fitness :)
                //"id", parent.getId() // TODO dočasně zakomentováno, aby se zjistilo jestli se to vyřeší, je to podobná věc jako u GP-ML... id se přidává zevlacky nebo vůbec (vgpml se to dela explicitne behem evaluace fitness) !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        );
    }

}
