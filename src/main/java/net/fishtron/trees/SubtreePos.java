package net.fishtron.trees;

import java.util.ArrayList;
import java.util.List;

import net.fishtron.types.Type;
import net.fishtron.utils.Listek;
import net.fishtron.utils.Log;


public class SubtreePos {

    private final Listek<Integer> path;
    private final Type type;

    private SubtreePos(Type type) {
        path = null;
        this.type = type;
    }

    private SubtreePos(int sonIndex, SubtreePos subtreePosInSon) {
        path = new Listek<>(sonIndex, subtreePosInSon.path);
        type = subtreePosInSon.getType();
    }

    private SubtreePos(Listek<Integer> path, Type type) {
        this.path = path;
        this.type = type;
    }

    public boolean isRoot() {
        return path == null;
    }

    public Integer getSonIndex() {
        if (path == null) {throw new Error("Empty path has no don index!");}
        return path.getHead();
    }

    public SubtreePos getTail() {
        if (path == null) {throw new Error("Empty path has tail!");}
        return new SubtreePos(path.getTail(), type);
    }

    public List<Integer> getPath() {
        return Listek.toList(path);
    }

    public Type getType() {
        return type;
    }

    public static SubtreePos root(Type type) {
        return new SubtreePos(type);
    }

    public static SubtreePos reverseStep(int sonIndex, SubtreePos subtreePosInSon) {
        return new SubtreePos(sonIndex, subtreePosInSon);
    }


    // TODO neefektivní ale halt ve spěchu :)
    public static SubtreePos classicStep(SubtreePos subtreePosHere, int nextSonIndex, Type type) {
        List<Integer> path = new ArrayList<>(subtreePosHere.getPath());
        path.add(nextSonIndex);
        return new SubtreePos(Listek.fromList(path), type);
    }


    @Override
    public String toString() {
        return Listek.toList(path).toString();
    }


    public static void main(String[] args) {

        SubtreePos pos = reverseStep(1, reverseStep(2, reverseStep(3, root(null))));

        Log.it(pos);

        Log.it( pos = classicStep(pos, 4, null) );

        Log.it( pos = reverseStep(0, pos) );
    }
}
