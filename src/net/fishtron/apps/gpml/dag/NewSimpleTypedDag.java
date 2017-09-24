package net.fishtron.apps.gpml.dag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Joiner;
import org.json.JSONArray;
import org.json.JSONObject;


import net.fishtron.apps.workflows.MyList;
import net.fishtron.types.Type;
import net.fishtron.types.TypeTerm;
import net.fishtron.types.Types;
import net.fishtron.utils.AA;
import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import net.fishtron.utils.TriFun;

import cz.tomkren.fishtron.terms.SmartSymbol;
import cz.tomkren.fishtron.terms.SmartSymbolWithParams;
import cz.tomkren.utils.Comb0;



public class NewSimpleTypedDag {

    private List<NewVertex> ins, outs;
    private Type inType, outType;
    private int width, height;

    public NewSimpleTypedDag(String name, Type inType, Type outType, JSONObject params, NewSimpleTypedDag innerDag) {

        this.inType = inType;
        this.outType = outType;

        NewVertex v = new NewVertex(name, params, innerDag);

        ins  = makeInterfaceList(inType, v);
        outs = makeInterfaceList(outType, v);

        width  = 1;
        height = 1;
    }

    public NewSimpleTypedDag(String name, Type inType, Type outType) {
        this(name, inType, outType, new JSONObject(), null);
    }


    public NewSimpleTypedDag copy() {
        return new NewSimpleTypedDag(this);
    }

    public NewSimpleTypedDag(NewSimpleTypedDag oldDag) {
        Map<Integer, NewVertex> oldToOld = new HashMap<>();
        Map<Integer, NewVertex> oldToNew = new HashMap<>();

        oldDag.forEachVertex(v -> {
            oldToOld.put(v.getId(), v);
            oldToNew.put(v.getId(), v.pseudoCopy());
        });

        for (Map.Entry<Integer,NewVertex> e : oldToNew.entrySet()) {
            int   oldId = e.getKey();
            NewVertex vNew = e.getValue();
            NewVertex vOld = oldToOld.get(oldId);

            for (AB<NewVertex,Integer> oldSuccWithPort : vOld.getSuccessorsWithPorts()) {
                NewVertex v = oldSuccWithPort._1();
                int port = oldSuccWithPort._2();
                vNew.addSuccessor( oldToNew.get(v.getId()) , port );
            }
        }

        inType  = oldDag.inType;
        outType = oldDag.outType;

        width  = oldDag.width;
        height = oldDag.height;

        ins = new ArrayList<>();
        outs = new ArrayList<>();

        oldDag. ins.forEach(vOld ->  ins.add(oldToNew.get(vOld.getId())));
        oldDag.outs.forEach(vOld -> outs.add(oldToNew.get(vOld.getId())));

    }

    private List<NewVertex> makeInterfaceList(Type type, NewVertex v) {
        List<NewVertex> ret = new ArrayList<>();
        int arity = getArity(type);
        for (int i = 0; i < arity; i++) {
            ret.add(v);
        }
        return ret;
    }

    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public int getPxWidth()  {return width  * NewVertex.X_1SIZE;}
    public int getPxHeight() {return height * NewVertex.Y_1SIZE;}

    private int getArity(Type type) {

        if (type instanceof TypeTerm) {
            List<Type> args = ((TypeTerm)type).getArgs();

            if (args.size() < 3) {
                throw new Error("Type must have at least 3 parts: "+type);
            }

            Type op   = args.get(0);
            Type arg1 = args.get(1);
            Type arg2 = args.get(2);
            if (Types.PAIR.equals(op)) {
                return getArity(arg1) + getArity(arg2);
            } else if (Types.VECTOR.equals(op)) {
                return getArity(arg1) * Types.fromNat(arg2);
            } else {
                throw new Error("Unsupported type constructor: "+op);
            }
        } else {
            return 1;
        }
    }


    public static NewSimpleTypedDag para(NewSimpleTypedDag dag1, NewSimpleTypedDag dag2) {
        return dag1.copy().para(dag2.copy());
    }

    public static NewSimpleTypedDag seri(NewSimpleTypedDag dag1, NewSimpleTypedDag dag2) {
        return dag1.copy().seri(dag2.copy());
    }

    public static NewSimpleTypedDag dia(NewSimpleTypedDag dag1, NewSimpleTypedDag dag2, NewSimpleTypedDag dag3) {
        return dag1.copy().seri(dag2.copy()).seri(dag3.copy());
    }

    public static NewSimpleTypedDag dia0(NewSimpleTypedDag dag1, NewSimpleTypedDag dag2) {
        return dag1.copy().seri(dag2.copy());
    }


    public static NewSimpleTypedDag split(NewSimpleTypedDag dag, MyList dagList) {
        return dag.copy().seri(fromMyList(dagList));
    }


    // -- new in 0.5 --

    public static NewSimpleTypedDag stacking(NewSimpleTypedDag stacker, NewSimpleTypedDag method) {
        return stacker.copy().seri(method.copy());
    }


    /*

    TypedDag.boosting( TypedDag: D => Boo , MyList: V (Boo => Boo) n , TypedDag : Boo => LD ) : D => LD",
    booBegin : D => Boo
    booEnd   : Boo => LD
    booster  : Boo => Boo
    */

    public static NewSimpleTypedDag boosting(NewSimpleTypedDag booBegin, MyList boosterList, NewSimpleTypedDag booEnd) {
        NewSimpleTypedDag boosterChain = fromBoosterList(boosterList);
        return booBegin.copy().seri(boosterChain).seri(booEnd.copy());
    }

    public static final Type BooType = Types.parse("Boo");

    public static NewSimpleTypedDag booster(NewSimpleTypedDag innerMethod) {
        return new NewSimpleTypedDag("booster", BooType, BooType, new JSONObject(), innerMethod);
    }

    private static NewSimpleTypedDag fromBoosterList(MyList boosterList) {
        List<NewSimpleTypedDag> boosters = boosterList.toList(NewSimpleTypedDag.class);
        return serialList(boosters);
    }

    private static NewSimpleTypedDag serialList(List<NewSimpleTypedDag> dags) {
        if (dags.isEmpty()) {return null;}

        Iterator<NewSimpleTypedDag> it = dags.iterator();
        NewSimpleTypedDag acc = it.next().copy();

        while (it.hasNext()) {
            acc = acc.seri(it.next().copy());
        }
        return acc;
    }


    // -- (end) new in 0.5


    public static NewSimpleTypedDag fromMyList(MyList dagList) {
        List<NewSimpleTypedDag> dags = dagList.toList(NewSimpleTypedDag.class);
        return paraList(dags);
    }

    public static NewSimpleTypedDag paraList(List<NewSimpleTypedDag> dags) {
        if (dags.isEmpty()) {return null;}
        Iterator<NewSimpleTypedDag> it = dags.iterator();
        NewSimpleTypedDag acc = it.next().copy();
        while (it.hasNext()) {
            acc = acc.para(it.next().copy());
        }
        return acc;
    }


    public NewSimpleTypedDag split_someOld(MyList dagList) {
        seri(fromMyList_noCopy(dagList) );
        return this;
    }

    public static NewSimpleTypedDag fromMyList_noCopy(MyList dagList) {
        List<NewSimpleTypedDag> dags = F.map(dagList.toList(), o -> (NewSimpleTypedDag) o);
        return paraList_noCopy(dags);
    }

    public static NewSimpleTypedDag paraList_noCopy(List<NewSimpleTypedDag> dags) {
        return F.reduce(dags, (x, y) -> x.para(y));
    }

    public NewSimpleTypedDag para(NewSimpleTypedDag dag2) {

        parallelMove(dag2);

        ins .addAll(dag2.ins);
        outs.addAll(dag2.outs);

        inType = new TypeTerm(Types.PAIR, inType, dag2.inType);
        outType = new TypeTerm(Types.PAIR, outType, dag2.outType);
        return this;
    }


    public NewSimpleTypedDag dia_someOld(NewSimpleTypedDag dag2, NewSimpleTypedDag dag3) {
        seri(dag2);
        seri(dag3);
        return this;
    }

    public NewSimpleTypedDag seri(NewSimpleTypedDag dag2) {


        if (getArity(outType) != getArity(dag2.inType)) { // moc silný, chcem napojovat ty se stejnou aritou (!outType.equals(dag2.inType)) {

            throw new Error("TypedDag.seri : incompatible types " + outType + " & " + dag2.inType);
        }

        List<NewVertex> ins2 = dag2.ins;
        int n = outs.size();

        if (n != ins2.size()) {
            throw new MergeException("Serial merge needs outs.size() == dag.ins.size(),"+
                    " but it was outs.size(): "+outs.size()+" dag.ins.size(): "+ins2.size());
        }

        serialMove(dag2);

        for (int i = 0; i < n; i++) {
            outs.get(i).addSuccessor( ins2.get(i) );
        }

        outs = dag2.outs;


        outType = dag2.outType;
        return this;
    }




    private void serialMove(NewSimpleTypedDag dag2) {
        dag2.move(0, height);
        height += dag2.height;

        double xMove = 0.5 * Math.abs(width - dag2.width);

        if (width < dag2.width) {
            move(xMove, 0);
        } else if (width > dag2.width) {
            dag2.move(xMove,0);
        }

        width = Math.max(width, dag2.width);
    }

    private void parallelMove(NewSimpleTypedDag dag2) {
        dag2.move(width, 0);
        width += dag2.width;

        double yMove = 0.5 * Math.abs(height - dag2.height);

        if (height < dag2.height) {
            move(0,yMove);
        } else if (height > dag2.height) {
            dag2.move(0,yMove);
        }

        height = Math.max(height, dag2.height);
    }

    public void move(double dx, double dy) {
        forEachVertex(v -> {
            v.moveX(dx);
            v.moveY(dy);
        });
    }


    public void forEachVertex(Consumer<NewVertex> f) {
        Set<NewVertex> vSet = new HashSet<>();
        Set<NewVertex> processed = new HashSet<>();
        vSet.addAll(ins);
        while (!vSet.isEmpty()) {
            Set<NewVertex> vSet_new = new HashSet<>();
            for (NewVertex v1 : vSet) {

                f.accept(v1);
                processed.add(v1);

                for (NewVertex v2 : v1.getSuccessors()) {
                    if (!processed.contains(v2) && !vSet.contains(v2)) {
                        vSet_new.add(v2);
                    }
                }
            }
            vSet = vSet_new;
        }
    }



    public static SmartSymbol mkAtomicDagNode(String name, String inType, String outType, JSONObject paramsInfo) {
        return mkAtomicDagNode(name, Types.parse(inType), Types.parse(outType), paramsInfo);
    }

    public static SmartSymbol mkAtomicDagNode(String name, Type inType, Type outType, JSONObject paramsInfo) {


        /*Comb0 comb = haxTypeInput -> {
            Type t = (Type) haxTypeInput.get(0);
            AA<Type> p = getBoxInOutTypes(t);
            return new TypedDag(name, p._1(), p._2());
        };*/

        Function<JSONObject,Comb0> params2comb = params -> (haxTypeInput -> {
            Type t = (Type) haxTypeInput.get(0);
            AA<Type> p = NewSimpleTypedDag.getBoxInOutTypes(t);
            return new NewSimpleTypedDag(name, p._1(), p._2(), params, null);
        });


        // ... původní před #newFish
        //ProtoNode protoNode = new ProtoNode(name, inType + " => " + outType);
        //return new CodeNodeWithParams(protoNode, params2comb, paramsInfo, null);

        SmartSymbol proto = new SmartSymbol(name, Types.parse(inType + " => " + outType), SmartSymbol.EMPTY_INS, null); // TODO #newFish: nevim esli to null je ok
        return new SmartSymbolWithParams(proto, params2comb, paramsInfo, null);
    }

    public static AA<Type> getBoxInOutTypes(Type type) {
        if (type instanceof TypeTerm) {
            TypeTerm tt = (TypeTerm) type;
            List<Type> args = tt.getArgs();
            if (args.size() == 3 && Types.BOX_ARROW.equals(args.get(1))) {
                return new AA<>(args.get(0),args.get(2));
            }
        }
        throw new Error("Type "+type+" was expected to be box type!");
    }

    public static SmartSymbol mkDagOperationNode(String name, Comb0 comb, String outType, String... inTypes) {

        // ... původní před #newFish
        //ProtoNode protoNode = new ProtoNode(name, outType, inTypes);
        //return new CodeNode(protoNode, comb);

        return new SmartSymbol(name, Types.parse(outType), F.map(inTypes , Types::parse), comb);
    }


    /*public static CodeNode mkCodeNode(String... args) {
        return mkCodeNode(args);
    }*/



    public static SmartSymbol mkSplit(String name, String outType, String inType1, String inType2) {
        Comb0 comb = xs -> ((NewSimpleTypedDag)xs.get(0)).split_someOld((MyList)xs.get(1));
        return mkDagOperationNode(name, comb, outType, inType1, inType2);
    }

    public static SmartSymbol mkDia(String name, String outType, String inType1, String inType2, String inType3) {
        Comb0 comb = xs -> ((NewSimpleTypedDag)xs.get(0)).dia_someOld((NewSimpleTypedDag) xs.get(1), (NewSimpleTypedDag) xs.get(2));
        return mkDagOperationNode(name, comb, outType, inType1, inType2, inType3);
    }


    public static SmartSymbol mkCodeNode(JSONObject allParamsInfo, String... args) {
        if (args.length < 2) {throw new Error("Too few arguments.");}

        String name    = args[0].trim();
        String outType = args[1].trim();

        if (args.length == 2) {
            String[] ps = outType.split("=>");
            if (ps.length != 2) {throw new Error("Atom type must have 2 parts (split by =>).");}

            //Log.it(allParamsInfo);

            JSONObject paramsInfo = allParamsInfo.has(name) ? allParamsInfo.getJSONObject(name) : new JSONObject();

            return mkAtomicDagNode(name, ps[0].trim(), ps[1].trim(), paramsInfo);
        } else {
            return mkDagOperationNode(name, outType, Arrays.copyOfRange(args,2,args.length) );
        }
    }

    /* Snad nepotřebný po #newFish
    public static CodeNode mkCodeNode(String... args) {
        return mkCodeNode(new JSONObject(), args);
    }*/


    public static SmartSymbol mkDagOperationNode(String name, String outType, String... inTypes) {
        int n = inTypes.length;
        if (n == 2) {
            return mkDagOperationNode(name, mkTypedDagFun2(mkBiFun(name)), outType, inTypes);
        } else if (n == 3) {
            return mkDagOperationNode(name, mkTypedDagFun3(mkTriFun(name)), outType, inTypes);
        }

        throw new Error("Unsupported arity "+n+".");
    }

    public static Comb0 mkTypedDagFun2(BiFunction<NewSimpleTypedDag,NewSimpleTypedDag,NewSimpleTypedDag> f) {
        return inputs -> f.apply((NewSimpleTypedDag)inputs.get(0),(NewSimpleTypedDag)inputs.get(1));
    }

    public static Comb0 mkTypedDagFun3(TriFun<NewSimpleTypedDag,NewSimpleTypedDag,NewSimpleTypedDag,NewSimpleTypedDag> f) {
        return inputs -> f.apply((NewSimpleTypedDag)inputs.get(0),(NewSimpleTypedDag)inputs.get(1),(NewSimpleTypedDag)inputs.get(2));
    }





    public static BiFunction<NewSimpleTypedDag,NewSimpleTypedDag,NewSimpleTypedDag> mkBiFun(String name) {
        try {
            Method method = NewSimpleTypedDag.class.getMethod(name, NewSimpleTypedDag.class);
            return (x,y)-> {
                try {
                    Object ret = method.invoke(x, y);
                    return (NewSimpleTypedDag) ret;
                }
                catch (IllegalArgumentException e)  {throw new Error("IllegalArgumentException !");}
                catch (IllegalAccessException e)    {throw new Error("IllegalAccessException !");}
                catch (InvocationTargetException e) {
                    throw new Error("InvocationTargetException! : "+e.getCause().getMessage());}
            };
        }
        catch (SecurityException e)     {throw new Error("SecurityException !");}
        catch (NoSuchMethodException e) {throw new Error("NoSuchMethodException !");}
    }

    public static TriFun<NewSimpleTypedDag,NewSimpleTypedDag,NewSimpleTypedDag,NewSimpleTypedDag> mkTriFun(String name) {
        try {
            Method method = NewSimpleTypedDag.class.getMethod(name, NewSimpleTypedDag.class, NewSimpleTypedDag.class);
            return (x,y,z)-> {
                try {
                    Object ret = method.invoke(x, y, z);
                    return (NewSimpleTypedDag) ret;
                }
                catch (IllegalArgumentException e)  {throw new Error("IllegalArgumentException !");}
                catch (IllegalAccessException e)    {throw new Error("IllegalAccessException !");}
                catch (InvocationTargetException e) {
                    throw new Error("InvocationTargetException! : " +e.getCause().getMessage());}
            };
        }
        catch (SecurityException e)     {throw new Error("SecurityException !");}
        catch (NoSuchMethodException e) {throw new Error("NoSuchMethodException !");}
    }


    /*
    public List<SimpleVertex> toSimpleGraph() {

        // TODO retardně implementováno, ale mělo by stačit, dyštak pak odretardnit

        List<SimpleVertex> begin  = new ArrayList<>();
        List<SimpleVertex> middle = new ArrayList<>();
        List<SimpleVertex> end    = new ArrayList<>();

        forEachVertex(v -> {
            AB<SimpleVertex, Vertex.Info> p = v.toSimpleVertex();
            SimpleVertex sv = p._1();
            Vertex.Info vInfo = p._2();

            switch (vInfo) {
                case BEGIN:
                    begin.add(sv);
                    break;
                case MIDDLE:
                    middle.add(sv);
                    break;
                case END:
                    end.add(sv);
                    break;
            }

        });

        if (begin.size() != 1) {throw new Error("There should be exactly one begin.");}
        if (end.size() > 1) {throw new Error("There should be at most one end.");}

        begin.addAll(middle);
        begin.addAll(end);

        return begin;
    }
    */

    public String toKutilXML(int xx, int yy) {
        StringBuilder sb = new StringBuilder();

        forEachVertex(v -> {
            v.toKutilXML(sb, xx, yy);
            sb.append('\n');
        });

        return sb.toString();
    }

    public JSONArray toKutilJson(int xx, int yy) {
        JSONArray ret = new JSONArray();
        forEachVertex(v -> ret.put(v.toKutilJson(xx, yy)));
        return ret;
    }

    public static String toJson(List<NewSimpleTypedDag> dags) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        dags.forEach(dag -> sb.append(dag.toJson()).append(",\n"));
        F.deleteLast(sb,2);
        sb.append("]");
        return sb.toString();
    }

    private boolean isMalformed = false;

    public boolean isMalformed() {

        Set<Integer> ids = new HashSet<>();

        forEachVertex(v -> {
            int vId = v.getId();
            if (ids.contains(vId)) {
                isMalformed = true;
            }
            ids.add(vId);
        });

        return isMalformed;
    }



    public boolean isMalformed_fake1() {
        int[] n = new int[1];
        n[0] = 0;
        forEachVertex(v -> {
            n[0]++;
        });
        return n[0]%3 == 0;
    }

    public boolean isMalformed_fake2() {
        return Math.random() < 0.2;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  ");
        NewVertex.toJson_input(sb, ins);
        sb.append(",\n");

        Set<Integer> ids = new HashSet<>();

        forEachVertex(v -> {

            int vId = v.getId();
            if (ids.contains(vId)) {
                isMalformed = true;
            }
            ids.add(vId);

            sb.append("  ");
            v.toJson(sb);
            sb.append(",\n");

        });
        F.deleteLast(sb,2);

        sb.append("\n}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toKutilXML(0,0);
    }

    public String toOldSchoolString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        List<NewVertex> vsList = new ArrayList<>();
        Set<NewVertex> vsSet   = new HashSet<>();


        vsList.addAll(ins);

        while (!vsList.isEmpty()) {

            sb.append(Joiner.on(' ').join(vsList)).append('\n');

            sb2.append(Joiner.on(' ').join(F.map(vsList, NewVertex::successorsStr))).append('\n');


            List<NewVertex> temp = new ArrayList<>();

            for (NewVertex v1 : vsList) {
                for (NewVertex v2 : v1.getSuccessors()) {
                    if (!vsSet.contains(v2)) {
                        temp.add(v2);
                        vsSet.add(v2);
                    }
                }
            }

            vsList = temp;
            vsSet  = new HashSet<>();

        }

        return sb.toString() +"\n"+ sb2.toString() ; // +"\n"+toKutilXML(new Int2D(0,0));
    }




    public static class MergeException extends RuntimeException {
        public MergeException(String message) {
            super(message);
        }
    }

}
