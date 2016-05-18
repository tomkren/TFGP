package cz.tomkren.fishtron.sandbox;

import cz.tomkren.fishtron.terms.SmartSymbol;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.Comb0;

import java.util.Collections;
import java.util.List;

/** Created by tom on 19.11.2015.*/

public class TComb0<A> implements Comb0, SymProvider {

    private A c;
    private SmartSymbol sym;

    public TComb0(String name, Type type, A c) {
        this.c = c;
        sym = new SmartSymbol(name, type, Collections.emptyList(), this);
    }

    public TComb0(String name, String type, A c) {
        this(name, Types.parse(type), c);
    }

    @Override
    public SmartSymbol getSym() {
        return sym;
    }

    @Override
    public Object compute(List<Object> inputs) {
        return c;
    }


}