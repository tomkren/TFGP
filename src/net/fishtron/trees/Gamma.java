package net.fishtron.trees;

import net.fishtron.types.Type;
import net.fishtron.types.Types;
import cz.tomkren.fishtron.ugen.trees.Lam;
import net.fishtron.utils.AB;
import net.fishtron.utils.ABC;
import net.fishtron.utils.F;
import net.fishtron.utils.TODO;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**  Created by user on 31. 1. 2017. */

public class Gamma {

    private final List<GammaSym> gamma;

    public Gamma(List<GammaSym> gammaSymList) {
        this.gamma = gammaSymList;
    }

    /*public Gamma(List<AB<String, Type>> gammaPairList) {
        this.gamma = F.map(gammaPairList, p -> new GammaSym(p._1(), p._2(), false));
    }*/

    private Gamma(List<Gamma> gammas, boolean checkUniqueness /*todo !!!*/) {
        int size = F.sumInt(F.map(gammas, g -> g.gamma.size()));
        gamma = new ArrayList<>(size);
        for (Gamma g : gammas) {
            gamma.addAll(g.gamma);
        }
    }

    public static Gamma union(List<Gamma> gammas) {
        return new Gamma(gammas, true);
    }


    public ABC<Type,Gamma,Function<AppTree,AppTree>> mkGammaWithGoalTypeVars(Type goalType, List<String> varNames) {

        AB<List<Type>,Type> p = Types.fullSplitFunType(goalType);
        List<Type> argTypes = p._1();
        Type newGoalType = p._2();

        List<GammaSym> varList = new ArrayList<>(argTypes.size());

        int i = 0;
        for (Type argType : argTypes) {

            if (argType.hasTypeVars()) {
                throw new TODO("Type variables in goalType arguments are not yet supported, sorry!!!");
            }

            String varName = (i < varNames.size()) ? varNames.get(i) : "x"+i;

            if (has(varName)) {
                throw new Error("Var name '"+varName+"' already in use (has type "+getType(varName)+").");
            }

            varList.add(new GammaSym(varName, argType, true));

            i++;
        }

        int newGammaSize = argTypes.size() + size();
        List<GammaSym> newSymList = new ArrayList<>(newGammaSize);

        newSymList.addAll(varList);
        newSymList.addAll(gamma);

        if (newSymList.size() != newGammaSize) {throw new Error("New gamma size assert failed.");}

        Gamma newGamma = new Gamma(newSymList);
        Function<AppTree,AppTree> addLambdasFun = bodyTree -> addLambdas(varList, bodyTree);
        return ABC.mk(newGoalType, newGamma, addLambdasFun);
    }

    private static AppTree addLambdas(List<GammaSym> varList, AppTree bodyTree) {
        AppTree acc = bodyTree;
        for (int i = varList.size() - 1; i >= 0; i --) {

            GammaSym varSym = varList.get(i);

            if (!varSym.isVar()) {throw new Error("Sym "+varSym+" is not a var!");}

            String varName = varSym.getSym();
            Type   varType = varSym.getType();

            Type newType         = Types.mkFunType(varType, acc.getType());
            Type newOriginalType = Types.mkFunType(varType, acc.getOriginalType()); // TODO promyslet, taková nouzovka spolehající na dočasnou vlastnost že argTypes jsou bez type vars zatim

            acc = new Lam(varName, acc, newType, newOriginalType, null);
        }
        return acc;
    }


    private Type getType(String symName) {
        GammaSym gs = getGammaSym(symName);
        return gs == null ? null : gs.getType();
    }

    private boolean has(String symName) {
        return getGammaSym(symName) != null;
    }

    private GammaSym getGammaSym(String symName) {
        for (GammaSym gs : gamma) {
            if (gs.getSym().equals(symName)) {return gs;}
        }
        return null;
    }

    public int size() {
        return gamma.size();
    }

    public List<AB<String, Type>> getSymbols() {
        return F.map(gamma, GammaSym::toNameTypePair);
    }

    public JSONArray toJson() {
        return F.jsonMap(gamma, GammaSym::toJson);
    }

    public static Gamma fromJson(JSONArray json) {
        List<GammaSym> newSymList = F.map(json, GammaSym::fromJson);
        return new Gamma(newSymList);
    }

    /*public static Gamma mk(String... strs) {
        if (strs.length % 2 != 0) {throw new Error("There must be an even number of gamma strings.");}
        List<GammaSym> ret = new ArrayList<>(strs.length/2);
        for (int i = 0; i < strs.length; i+=2) {
            ret.add(new GammaSym(strs[i], Types.parse(strs[i+1]), false));
        }
        return new Gamma(ret);
    }*/

    public static Gamma mk(Object... objs) {

        List<GammaSym> ret = new ArrayList<>();

        int n = objs.length;
        int i = 0;

        while (i<n) {
            Object arg = objs[i];
            if (arg instanceof String) {

                String sym = (String) arg;

                if (i+1 >= n) {throw new Error("Arg on index "+(i+1)+" expected (because "+sym+" is a String), but missing.");}

                Object typeObj = objs[i+1];
                Type type;

                if (typeObj instanceof String) {
                    type = Types.parse((String) typeObj);
                } else if (typeObj instanceof Type) {
                    type = (Type) typeObj;
                } else {
                    throw new Error("Arg on index "+(i+1)+" must be either String or Type.");
                }

                ret.add(new GammaSym(sym, type, false));
                i += 2;

            } else if (arg instanceof GammaSym) {

                ret.add((GammaSym) arg);
                i += 1;

            } else {
                throw new Error("Arg on index "+i+" must be either String or GammaSym.");
            }
        }

        return new Gamma(ret);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (GammaSym gs : gamma) {
            sb.append(gs.toString()).append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gamma gamma1 = (Gamma) o;
        return gamma.equals(gamma1.gamma);
    }

    @Override
    public int hashCode() {
        return gamma.hashCode();
    }
}