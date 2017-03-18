package cz.tomkren.fishtron.ugen.multi;

import cz.tomkren.fishtron.ugen.trees.AppTree;
import cz.tomkren.fishtron.ugen.eval.EvalLib;

import java.util.List;

/**Created by tom on 07.03.2017.*/

public class AppTreeMI implements MultiIndiv {

    private final AppTree tree;
    //private final EvalLib lib; // todo pokud to pude elegantně, tak odebrat a radši mít jen jednou v evaluatoru
    private Object value;

    private int id;
    private List<Double> fitness;
    private int front;
    private double crowdingDistance;


    public AppTreeMI(AppTree tree/*, EvalLib lib*/) {
        this.tree = tree;
        //this.lib = lib;
    }

    public AppTree getTree() {return tree;}
    //public EvalLib getLib() {return lib;}

    @Override
    public Object computeValue(EvalLib lib) {
        value = lib.eval(tree);
        return value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return tree.toString();
    }

    public String toShortString() {
        return tree.toShortString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppTreeMI appTreeMI = (AppTreeMI) o;
        return tree.equals(appTreeMI.tree);
    }

    @Override
    public int hashCode() {
        return tree.hashCode();
    }

    @Override public List<Double> getFitness() {return fitness;}
    @Override public void setFitness(List<Double> fitness) {this.fitness = fitness;}

    @Override public int getId() {return id;}
    @Override public void setId(int id) {this.id =id;}

    @Override public int getFront() {return front;}
    @Override public void setFront(int front) {this.front = front;}

    @Override public double getCrowdingDistance() {return crowdingDistance;}
    @Override public void setCrowdingDistance(double crowdingDistance) {this.crowdingDistance = crowdingDistance;}

}
