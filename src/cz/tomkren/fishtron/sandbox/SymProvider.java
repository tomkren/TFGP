package cz.tomkren.fishtron.sandbox;

import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.fishtron.terms.SmartSymbol;
import net.fishtron.utils.F;

import java.util.List;

/** Created by tom on 19.11.2015.*/

public interface SymProvider {

    SmartSymbol getSym();

    static SmartLibrary mkLib(List<SymProvider> xs) {
        return new SmartLibrary(F.map(xs, SymProvider::getSym));
    }

    static SmartLibrary mkLib(SymProvider... xs) {
        return new SmartLibrary(F.map(xs, SymProvider::getSym));
    }
}
