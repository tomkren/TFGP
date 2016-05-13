package cz.tomkren.fishtron.workflows;

/** Created by tom on 7.11.2015.*/

import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

    private final int id;
    private final String name;
    private final JSONObject params;
    private final List<AB<Vertex,Integer>> successors;
    private int nextFreeSlot;
    private double x, y;

    private TypedDag innerDag;

    private static int nextId = 1; // todo pak udělat míň haxově


    public Vertex(String boxName, JSONObject params, TypedDag innerDag) {
        id = nextId++;
        name = boxName;

        this.params = params;
        this.innerDag = innerDag;

        successors = new ArrayList<>();
        nextFreeSlot = 0;

        x = 0;
        y = 0;
    }

    public Vertex(String boxName) {
        this(boxName, new JSONObject(),null);
    }

    public Vertex pseudoCopy() {return new Vertex(this);}

    private Vertex(Vertex v) {
        id = nextId++;
        name = v.name;

        params = v.params;  // TODO může bejt nebezpečný, kdyby někdo měnil ty parametry!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        innerDag = v.innerDag == null ? null : v.innerDag.copy();

        int n = v.successors.size();
        successors = new ArrayList<>(n); // ty nekopírujeme, pač tam chceme hluboký kopie
        nextFreeSlot = v.nextFreeSlot;

        x = v.x;
        y = v.y;
    }

    public double getX() {return x;}
    public double getY() {return y;}

    public void moveX(double dx) {x += dx;}
    public void moveY(double dy) {y += dy;}

    public String getName() {return name;}
    public int getId() {return id;}
    public String getKutilId() {return "$v_"+id;}

    public List<Vertex> getSuccessors() {return F.map(successors,AB::_1);}

    public List<AB<Vertex,Integer>> getSuccessorsWithPorts() {return successors;}

    public void addSuccessor(Vertex v, int port) {
        successors.add(new AB<>(v,port));
    }

    public void addSuccessor(Vertex v) {
        successors.add(new AB<>(v,v.nextFreeSlot));
        v.nextFreeSlot++;
    }

    @Override
    public String toString() {return name+"("+id+")";}


    public static final int X_1SIZE = 64;
    public static final int Y_1SIZE = 64;

    public static void toJson_input(StringBuilder sb, List<Vertex> ins) {
        String id   = "input";
        String name = "input";

        int numOutputs = ins.size();

        sb.append("\"").append(id).append("\" : [ [], \"").append(name).append("\", [");

        int i = 0;
        for (Vertex s : ins) {
            sb.append('\"').append(s.id).append(':').append(i).append("\"");
            if (i < numOutputs-1) {
                sb.append(", ");
            }
            i++;
        }

        sb.append("] ]");
    }

    public static String indent(int i, String str) {
        StringBuilder sb = new StringBuilder();
        String[] parts = str.split("\\n");
        for (String part : parts) {
            for (int j = 0; j < i; j++) {sb.append(' ');}
            sb.append(part);
            sb.append('\n');
        }
        return sb.toString();
    }

    public void toJson(StringBuilder sb) {
        int numInputs  = Math.max(1, nextFreeSlot);
        int numOutputs = Math.max(1, successors.size());

        sb.append("\"").append(id).append("\" : [ [");

        for (int i = 0; i < numInputs; i++) {
            sb.append('\"').append(id).append(':').append(i).append("\"");
            if (i < numInputs-1) {
                sb.append(", ");
            }
        }

        String paramsStr = params.toString(); //"{}"; // TODO ??

        String innerDagJson = innerDag == null ? "" : ",\n" + indent(6,innerDag.toJson()) ;


        sb.append("], [\"").append(name).append("\", ").append(paramsStr).append(innerDagJson).append("], [");

        int i = 0;
        for (AB<Vertex,Integer> p : successors) {
            Vertex s = p._1();
            int port = p._2();
            sb.append('\"').append(s.id).append(':').append(port).append("\"");
            if (i < numOutputs-1) {
                sb.append(", ");
            }
            i++;
        }

        sb.append("] ]");
    }

    public JSONObject toKutilJson(int xx, int yy) {

        JSONObject ret = new JSONObject();

        int xPos = (int)((0.6+x) * X_1SIZE ) + xx;
        int yPos = (int)((0.5+y) * Y_1SIZE ) + yy;

        int numInputs  = Math.max(1, nextFreeSlot);
        int numOutputs = Math.max(1, successors.size());

        StringBuilder shape = new StringBuilder();

        shape.append("f").append(" ").append(numInputs).append(' ').append(numOutputs).append(' ').append(name);

        if (innerDag != null) {
            shape.append(":#999999");
        }

        for (AB<Vertex,Integer> p : successors) {
            Vertex s = p._1();
            int port = p._2();
            shape.append(' ').append(s.getKutilId()).append(':').append(port);
        }

        ret.put("id", getKutilId());
        ret.put("shape", shape.toString());
        ret.put("pos", xPos+" "+yPos);

        if (innerDag != null) {
            ret.put("inside", innerDag.toKutilJson(100,100));
        }

        return ret;
    }

    public void toKutilXML(StringBuilder sb, int xx, int yy) {

        int xPos = (int)((0.6+x) * X_1SIZE ) + xx;
        int yPos = (int)((0.5+y) * Y_1SIZE ) + yy;

        int numInputs  = Math.max(1, nextFreeSlot);
        int numOutputs = Math.max(1, successors.size());

        sb.append("<o id=\"").append(getKutilId()).append("\"")
                .append(" shape=\"f").append(innerDag == null ? "" : "_b").append(" ").append(numInputs).append(' ').append(numOutputs).append(' ').append(name);

        for (AB<Vertex,Integer> p : successors) {
            Vertex s = p._1();
            int port = p._2();
            sb.append(' ').append(s.getKutilId()).append(':').append(port);
        }
        sb.append("\"").append("\t pos=\"").append(xPos).append(' ').append(yPos).append("\"").append(innerDag == null ? "/" : "").append(">");

        if (innerDag != null) {

            sb.append( indent(2,innerDag.toKutilXML(100,100)) );

            sb.append("</o>");
        }


    }


    public String successorsStr() {return name +"("+id+")->"+ successors.toString();}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return id == vertex.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}
