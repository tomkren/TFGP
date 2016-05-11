package cz.tomkren.fishtron.terms;

import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 7.11.2015.*/

// TODO sem dát očíštěnou a refaktorovanou verzi SmartLib ... jestli se to vyplatí



public class SmartLibrary {

    private final List<SmartSymbol> symLib;
    Map<String,SmartSymbol> codeMap;


    public SmartLibrary(List<SmartSymbol> symList) {

        this.symLib  = F.sort(symList, sym -> (double) sym.getArity() );
        this.codeMap = new HashMap<>();

        symList.forEach(sym -> codeMap.put(sym.getName(), sym));

        for (SmartSymbol smartSym : symLib) {
            smartSym.initApplicableSons(symLib);
        }
    }

    public static SmartLibrary mk(String classPrefix, JSONObject allParamsInfo, JSONArray codeNodeLines) {
        String[] strLines = new String[codeNodeLines.length()];
        for (int i = 0; i < codeNodeLines.length(); i++) {
            strLines[i] = codeNodeLines.getString(i);
        }
        return mk(classPrefix, allParamsInfo, strLines);
    }

    public static SmartLibrary mk(String classPrefix, String... codeNodeLines) {
        return mk(classPrefix, new JSONObject(),codeNodeLines);
    }

    public static SmartLibrary mk(String classPrefix, JSONObject allParamsInfo, String... codeNodeLines) {
        List<SmartSymbol> libList = new ArrayList<>(codeNodeLines.length);
        for (String line : codeNodeLines) {
            String trimLine = line.trim();
            if (!"".equals(trimLine)) {
                libList.add(SmartSymbol.mk(classPrefix, trimLine, allParamsInfo));
            }
        }
        return new SmartLibrary(libList);
    }

    public List<SmartSymbol> getSyms(){
        return symLib;
    }

    public SmartSymbol getSymByName(String symbolName) {
        return codeMap.get(symbolName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("sym \t -> \t applicableSons\n");
        sb.append("-------------------------------------------------------------------------------\n");


        for (SmartSymbol smartSym : symLib) {

            String asStr = F.list(smartSym.getApplicableSons()).foldr("",(symList,str)-> F.list(symList).foldr(" ",(sym,s)-> sym.getName() +" "+s ) +"| "+ str );

            sb.append( smartSym.getName() ).append(" \t -> \t ").append(asStr).append("\n");
        }
        return sb.toString();
    }

}
