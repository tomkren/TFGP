package net.fishtron.eva;

import java.util.List;

public interface IndivGenerator<Indiv> {
    List<Indiv> generate(int numIndivs);
}
