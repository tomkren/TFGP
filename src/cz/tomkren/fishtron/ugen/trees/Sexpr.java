package cz.tomkren.fishtron.ugen.trees;

import com.google.common.base.Joiner;
import net.fishtron.utils.F;

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

    @Override
    public String toString() {
        if (args.isEmpty()) {return sym;}
        return sym + "("+ Joiner.on(",").join(F.map(args, Sexpr::toString)) +")";
    }
}
