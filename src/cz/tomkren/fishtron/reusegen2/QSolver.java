package cz.tomkren.fishtron.reusegen2;

import cz.tomkren.fishtron.terms.SmartLibrary;
import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.utils.F;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Created by tom on 20. 6. 2016. */

public class QSolver {

    private SmartLibrary lib;
    private Random rand;
    private Map<String, TypeGadget> gadgets;

    public QSolver(SmartLibrary lib, Random rand) {
        this.lib = lib;
        this.rand = rand;
        gadgets = new HashMap<>();
    }

    public AppTree generateOne(Type t, int treeSize) {
        return getTypeGadget(t).generateOne(treeSize, this);
    }

    public List<AppTree> generateAll(Type t, int treeSize) {
        return getTypeGadget(t).generateAll(treeSize, this);
    }

    public BigInteger getNum(Type t, int treeSize) {
        return getTypeGadget(t).getNum(treeSize, this);
    }

    List<SubTypeInfo> getInfos(Type t, int treeSize) {
        return getTypeGadget(t).getInfos(treeSize, this);
    }

    SmartLibrary getLib() {
        return lib;
    }

    Random getRand() {
        return rand;
    }

    // todo type normalization .. (e.g. (List x1) and (List x23) are essentially the same types)
    private TypeGadget getTypeGadget(Type t) {
        Type tNormalized = normalizeType(t);
        return F.getOrMkAndPut(gadgets, tNormalized.toString(), ()-> new TypeGadget(tNormalized));
    }

    static Type normalizeType(Type t) {
        return t.freshenVars(0, new Sub())._1();
    }


}
