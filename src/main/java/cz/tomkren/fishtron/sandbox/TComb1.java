package cz.tomkren.fishtron.sandbox;

import cz.tomkren.fishtron.terms.SmartSymbol;
import net.fishtron.types.Type;
import net.fishtron.types.TypeParser;
import cz.tomkren.utils.Comb0;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** Created by tom on 19.11.2015.*/

public class TComb1<A,B> implements Comb0, SymProvider {

    private Function<A,B> f;
    private SmartSymbol sym;

    public TComb1(String name, Type in, Type out, Function<A, B> f) {
        mk(name, in, out, f);
    }

    public TComb1(String name, String in, String out, Function<A, B> f) {
        TypeParser typeParser = new TypeParser();
        mk(name, typeParser.parse(in), typeParser.parse(out), f);
    }

    private void mk(String name, Type in, Type out, Function<A, B> f) {
        this.f = f;
        sym = new SmartSymbol(name, out, Collections.singletonList(in), this);
    }

    @Override
    public SmartSymbol getSym() {
        return sym;
    }

    @Override
    public Object compute(List<Object> inputs) {
        return f.apply((A)inputs.get(0));
    }


}
