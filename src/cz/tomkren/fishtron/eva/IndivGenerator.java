package cz.tomkren.fishtron.eva;

import java.util.List;

public interface IndivGenerator<Indiv> {
    List<Indiv> generate(int numIndivs);
}
