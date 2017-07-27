package cz.tomkren.fishtron.ugen.trees;

import java.util.List;

/**
 * Created by tom on 27.07.2017.
 */
public class Sexpr {

    private final String sym;
    private final List<Sexpr> args;

    public Sexpr(String sym, List<Sexpr> args) {
        this.sym = sym;
        this.args = args;
    }

    public String getSym() { return sym; }
    public List<Sexpr> getArgs() { return args; }

    public boolean isLeaf() {
        return args.isEmpty();
    }
}
