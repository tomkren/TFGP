package net.fishtron.trees;

import org.json.JSONArray;

import net.fishtron.types.Type;
import net.fishtron.types.Types;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;


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
        return F.arr(sym, type.toString(), isVar);
    }

    public static GammaSym fromJson(Object json) {
        JSONArray arr = (JSONArray) json;
        return new GammaSym(
                arr.getString(0),
                Types.parse(arr.getString(1)),
                arr.getBoolean(2)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GammaSym gammaSym = (GammaSym) o;

        if (isVar != gammaSym.isVar) return false;
        if (!sym.equals(gammaSym.sym)) return false;
        return type.equals(gammaSym.type);
    }

    @Override
    public int hashCode() {
        int result = sym.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (isVar ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return sym +" : "+ Types.prettyPrint2(type);
    }
}
