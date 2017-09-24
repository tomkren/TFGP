package cz.tomkren.fishtron.eva;

import net.fishtron.utils.Weighted;

import java.util.List;

// TODO BasicEvaledPop, IndivEvaledPop a TogetherEvaledPop maj spoustu kódu stejně, opravit
// todo: tzn asi místo interfacu požít třídu od který budou dědit, to podstatný teď dělaj v konstruktoru

public interface EvaledPop<Indiv extends Weighted> {
    boolean isTerminating();
    Indiv getBestIndividual();
    Distribution<Indiv> getIndividuals();
    List<Indiv> getTerminators();

    default List<Indiv> getIndivList() {
        return getIndividuals().getList();
    }
}
