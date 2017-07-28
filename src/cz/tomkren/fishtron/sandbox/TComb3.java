package cz.tomkren.fishtron.sandbox;

import cz.tomkren.fishtron.terms.SmartSymbol;
import net.fishtron.types.Type;
import net.fishtron.types.TypeParser;
import cz.tomkren.utils.Comb0;
import cz.tomkren.utils.TriFun;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Created by tom on 19.11.2015.*/

public class TComb3<A,B,C,D> implements Comb0, SymProvider {

    private TriFun<A,B,C,D> f;
    private SmartSymbol sym;

    public TComb3(String name, Type in1, Type in2, Type in3, Type out, TriFun<A, B, C, D> f) {
        mk(name, in1, in2, in3, out, f, null);
    }

    public TComb3(String name, String in1, String in2, String in3, String out, TriFun<A, B, C, D> f) {
        TypeParser typeParser = new TypeParser();
        mk(name, typeParser.parse(in1), typeParser.parse(in2), typeParser.parse(in3), typeParser.parse(out), f, null);
    }

    public TComb3(String name, String in1, String in2, String in3, String out, TriFun<A, B, C, D> f, SonRestriction sr) {
        TypeParser typeParser = new TypeParser();
        mk(name, typeParser.parse(in1), typeParser.parse(in2), typeParser.parse(in3), typeParser.parse(out), f, Collections.singletonList(sr));
    }

    private void mk(String name, Type in1, Type in2, Type in3, Type out, TriFun<A, B, C, D> f, List<SonRestriction> srs) {
        this.f = f;
        sym = new SmartSymbol(name, out, Arrays.asList(in1,in2,in3), this, srs);
    }

    @Override
    public SmartSymbol getSym() {
        return sym;
    }

    @Override
    public Object compute(List<Object> inputs) {
        return f.apply((A)inputs.get(0), (B)inputs.get(1), (C)inputs.get(2));
    }


}
