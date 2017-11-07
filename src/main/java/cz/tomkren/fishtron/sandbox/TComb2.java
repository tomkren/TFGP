package cz.tomkren.fishtron.sandbox;

import cz.tomkren.fishtron.terms.SmartSymbol;
import net.fishtron.types.Type;
import net.fishtron.types.TypeParser;
import cz.tomkren.utils.Comb0;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/** Created by tom on 19.11.2015.*/

public class TComb2<A,B,C> implements Comb0, SymProvider {

    private BiFunction<A,B,C> f;
    private SmartSymbol sym;

    public TComb2(String name, Type in1, Type in2, Type out, BiFunction<A, B, C> f) {
        mk(name, in1, in2, out, f, null);
    }

    public TComb2(String name, String in1, String in2, String out, BiFunction<A, B, C> f) {
        TypeParser typeParser = new TypeParser();
        mk(name, typeParser.parse(in1), typeParser.parse(in2), typeParser.parse(out), f, null);
    }

    public TComb2(String name, String in1, String in2, String out, BiFunction<A, B, C> f, SonRestriction sr) {
        TypeParser typeParser = new TypeParser();
        mk(name, typeParser.parse(in1), typeParser.parse(in2), typeParser.parse(out), f, Collections.singletonList(sr));
    }

    private void mk(String name, Type in1, Type in2, Type out, BiFunction<A, B, C> f, List<SonRestriction> srs) {
        this.f = f;
        sym = new SmartSymbol(name, out, Arrays.asList(in1,in2), this, srs);
    }

    @Override
    public SmartSymbol getSym() {
        return sym;
    }

    @Override
    public Object compute(List<Object> inputs) {
        return f.apply((A)inputs.get(0), (B)inputs.get(1));
    }


}
