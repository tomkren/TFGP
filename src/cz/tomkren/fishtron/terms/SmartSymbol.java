package cz.tomkren.fishtron.terms;

import cz.tomkren.fishtron.sandbox.SonRestriction;
import net.fishtron.types.*;
import cz.tomkren.fishtron.workflows.TypedDag; // TODO !!!!!!!!!!!!!!  Potřeba odstranit reference na TypedDag -důležité !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!!   !!!!!!!!!!!!!!!!!!
import cz.tomkren.utils.*;
import net.fishtron.utils.AB;
import net.fishtron.utils.ABC;
import net.fishtron.utils.F;
import net.fishtron.utils.Log;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Created by tom on 7.11.2015.*/

// TODO sem dát očíštěnou a refaktorovanou verzi SmartSym ... jestli se to vyplatí


public class SmartSymbol {

    // TODO #newFish pak dát  pryč, přecejenom por jistotu ještě chvilku nechávám zde
    //public static final String CLASS_PREFIX = "cz.tomkren.fishtron.workflows.";

    public static final List<Type> EMPTY_INS = Collections.emptyList();



    // todo byli final, zatim jsem dal pryč, pak až tu něco bude tak vrátit

    // Z ProtoNode
    private String name;
    private Type out;
    private List<Type> ins;

    // Z CodeNode
    private Comb0 code;

    // Ze SmartSym
    private List<List<SmartSymbol>> applicableSons;

    private List<SonRestriction> sonRestrictions;

    // TODO : nevim esli by nemělo hluboce kopčit, zatim neni známi kontext přesně
    public SmartSymbol(SmartSymbol proto) {
        this.name = proto.name;
        this.out = proto.out;
        this.ins = proto.ins;
        this.code = proto.code;
        this.applicableSons = proto.applicableSons;
    }

    public SmartSymbol(String name, Type out, List<Type> ins, Comb0 code) {
        this(name, out, ins, code, null);
    }

    public SmartSymbol(String name, Type out, List<Type> ins, Comb0 code, List<SonRestriction> sonRestrictions) {
        this.name = name;
        this.out = out;
        this.ins = ins;
        this.code = code;
        this.applicableSons = null;
        this.sonRestrictions = sonRestrictions;
    }

    public Comb0 getCode() {return code;}

    public boolean isTerminal() {return ins.size() == 0;}

    protected void initApplicableSons(List<SmartSymbol> allLibSymbols) {

        applicableSons = new ArrayList<>(getArgTypes().size());

        ABC<Type,List<Type>,Integer> freshResult = freshenTypeVars(0);

        //Type freshOutType = freshResult._1();
        List<Type> freshInTypes = freshResult._2();
        int nextVarId = freshResult._3();


        for (Type freshArgType : freshInTypes) {

            List<SmartSymbol> applicableSonsForThisArg = new ArrayList<>();

            for (SmartSymbol smartSym : allLibSymbols) {

                AB<Type,Integer> innerFreshResult = smartSym.getOutputType().freshenVars(nextVarId, new Sub());
                Type freshOutType = innerFreshResult._1();
                nextVarId = innerFreshResult._2();

                // TODO šlo by dělat fikanějc, např pro cons první arg se chytne všchno, ale nemá to pak podporu v druhym a vystupnim typu

                Sub maybeMgu = Sub.mgu(freshArgType, freshOutType);  // todo.. možná se vyplatí tuto substituci nezahodit, ale pak jí používat aby se nepočítala furt nanovo

                if (!maybeMgu.isFail()) {
                    applicableSonsForThisArg.add(smartSym);
                }

            }

            applicableSons.add(applicableSonsForThisArg);
        }

        if (sonRestrictions != null) {

            for (SonRestriction sr : sonRestrictions) {

                int sonIndex = sr.getSonIndex();
                String forbiddenName = sr.getForbiddenName();

                F.Partition<SmartSymbol> partition = new F.Partition<>(applicableSons.get(sonIndex), sym -> !forbiddenName.equals(sym.getName()) );

                if (partition.getKO().size() > 0) {
                    Log.it("SonRestrictions removed from applicableSons of SmartSymbol '"+getName()+"' on sonIndex "+sonIndex+" following symbols:");
                    Log.itln(F.map(partition.getKO(), SmartSymbol::getName));

                    applicableSons.set(sonIndex, partition.getOK());
                }
            }
        }

    }

    public List<List<SmartSymbol>> getApplicableSons() {return applicableSons;}

    public PolyTree mkTree(Type rootType, List<PolyTree> sons) {
        return new PolyTree(this, rootType, sons);
    }


    public ABC<Type,List<Type>,Integer> freshenTypeVars(int startVarId) {

        List<Type> typeList = new ArrayList<>(1+getArity());
        typeList.add(getOutputType());
        typeList.addAll(getArgTypes());

        TypeTerm helperTerm = new TypeTerm(typeList);
        AB<Type,Integer> p = helperTerm.freshenVars(startVarId, new Sub());

        TypeTerm freshHelperTerm = (TypeTerm) p._1();
        int nextVarId = p._2();

        List<Type> freshTypeList = freshHelperTerm.getArgs();

        Type       freshOutType = freshTypeList.get(0);
        List<Type> freshInTypes = freshTypeList.subList(1,freshTypeList.size());

        return new ABC<>(freshOutType, freshInTypes, nextVarId);
    }

    public String getName() {
        return name;
    }

    public String getNameWithParams() {
        return name;
    }

    public Type getOutputType() {return out;}

    public List<Type> getArgTypes() {
        return ins;
    }

    public int getArity() {
        return ins.size();
    }

    @Override
    public String toString() {
        return out +" <-< "+ name+ (ins.isEmpty() ? "" : " <-< "+ ins) ;
    }






    public static SmartSymbol mk(String classPrefix, String nodeLine, JSONObject allParamsInfo) {

        int colonPos = nodeLine.lastIndexOf(':');
        String rest = nodeLine.substring(0,colonPos).trim();
        String outTypeStr = nodeLine.substring(colonPos+1).trim();

        String[] ps = rest.split("\\(", 2);
        String classDotMethod = ps[0].trim();

        boolean isTerminal = ps.length < 2;

        if (!isTerminal) {
            rest = ps[1].trim();
            if (rest.charAt(rest.length()-1) != ')') {
                throw new Error("Missing ')' at the end of: "+rest);
            }
            rest = rest.substring(0, rest.length() - 1).trim();
        }

        ps = classDotMethod.split("\\.", 2);

        String homeClassStr, methodName;

        if (ps.length == 1) {
            homeClassStr = null;
            methodName   = ps[0].trim();
        } else if (ps.length == 2) {
            homeClassStr = ps[0].trim();
            methodName   = ps[1].trim();
        } else {
            throw new Error("Wrong format of the method name.");
        }

        if (isTerminal) {
            return fromStatic0(classPrefix, outTypeStr, homeClassStr, methodName, allParamsInfo);
        }

        ps = rest.split(",");
        int n = ps.length;

        String[] args = new String[2*n];

        for (int i = 0; i < n; i++) {
            String[] ps2 = ps[i].trim().split(":", 2);
            args[2*i  ] = ps2[0].trim();
            args[2*i+1] = ps2[1].trim();
        }

        //Log.it(buildingSymbolDescription);
        //Log.it(outTypeStr);
        //Log.it(homeClassStr);
        //Log.it(methodName);
        //Log.list(Arrays.asList(args));

        return fromStatic(classPrefix, outTypeStr, homeClassStr, methodName, args);
    }


    public static SmartSymbol fromStatic0(String classPrefix, String outTypeStr, String homeClassStr, String methodName,  JSONObject allParamsInfo) {

        if (homeClassStr == null) {return fromStatic0(outTypeStr, methodName, allParamsInfo);}

        try {
            Type outType = Types.parse(outTypeStr);
            Class<?> homeClass = getClass(classPrefix, homeClassStr);

            Comb0 comb = Comb0.fromStatic(methodName, homeClass);
            return new SmartSymbol(methodName, outType, EMPTY_INS, comb);

        } catch (ClassNotFoundException e) {
            throw new Error("ClassNotFoundException! : " + e.getMessage());
        }
    }


    private static SmartSymbol fromStatic0(String outTypeStr, String methodName, JSONObject allParamsInfo) {

        // TODO dočasný, zobecnit !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!               !!!                    HAX !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return TypedDag.mkCodeNode(allParamsInfo, methodName, outTypeStr);
    }

    /**
     * Reflection using approach to defining a building symbol with implementation.
     * @param outTypeStr Fishtron output type.
     * @param homeClassStr Path from CodeNode.CLASS_PREFIX with class name where the method resides.
     * @param methodName Method name.
     * @param args There must be even number of input-type args for fromStatic method. Each pair has the form ('Java class', 'Fishtron type').
     * @return Newly constructed SmartSymbol (building symbol with implementation) for the static method.
     */
    public static SmartSymbol fromStatic(String classPrefix, String outTypeStr, String homeClassStr, String methodName, String... args) {
        if (args.length % 2 != 0) {throw new Error(
                "There must be even number of input-type args for fromStatic method. "+
                        "Each pair has the form (\'Java class\', \'Fishtron type\').");
        }
        if (homeClassStr == null) {throw new Error("Function node must specify homeClass, but it was null.");}

        TypeParser typeParser = new TypeParser();

        Type outType = typeParser.parse(outTypeStr); // TODO Chytat i TypesParseErrory !

        int numIns = args.length / 2;
        Class<?>[] inClasses = new Class[numIns];
        List<Type> inTypes   = new ArrayList<>(numIns);

        try {
            Class<?> homeClass = getClass(classPrefix, homeClassStr);

            for (int i = 0; i < args.length; i+=2) {
                inClasses[i/2] = getClass(classPrefix, args[i]);
                inTypes.add(typeParser.parse(args[i + 1]));
            }

            Comb0 comb = Comb0.fromStatic(methodName, homeClass, inClasses);
            return new SmartSymbol(methodName, outType, inTypes, comb);

        } catch (ClassNotFoundException e) {
            throw new Error("ClassNotFoundException! : "+e.getMessage());
        }
    }


    public static Class<?> getClass(String classPrefix, String className) throws ClassNotFoundException {
        switch (className) {
            case "Object" : return Object.class;
            default : return Class.forName(/*#newFish CLASS_PREFIX*/ classPrefix + className);
        }
    }

}
