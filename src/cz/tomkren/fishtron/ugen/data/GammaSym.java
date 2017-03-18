package cz.tomkren.fishtron.ugen.data;

import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.json.JSONArray;


/**Created by tom on 18.03.2017.*/

public class GammaSym {

    private final String sym;
    private final Type type;
    private final boolean isVar;

    public GammaSym(String sym, Type type, boolean isVar) {
        this.sym = sym;
        this.type = type;
        this.isVar = isVar;
    }

    public String getSym() {return sym;}
    public Type getType()  {return type;}
    public boolean isVar() {return isVar;}

    public AB<String,Type> toNameTypePair() {
        return AB.mk(sym,type);
    }

    public JSONArray toJson() {
        return F.arr(sym, type.toString());
    }

    @Override
    public String toString() {
        return sym +" : "+ Types.prettyPrint2(type);
    }
}
