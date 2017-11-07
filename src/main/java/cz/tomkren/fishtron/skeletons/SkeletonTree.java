package cz.tomkren.fishtron.skeletons;

import com.google.common.base.Joiner;

import java.util.Collections;
import java.util.List;

/** Created by tom on 7.7.2015. */

public class SkeletonTree {

    private final List<String> symNames;
    private final List<SkeletonTree> sons;

    public SkeletonTree(List<String> symNames) {
        this.symNames = symNames;
        this.sons = Collections.emptyList();
    }

    public SkeletonTree(String symName, List<SkeletonTree> sons) {
        this.symNames = Collections.singletonList(symName);
        this.sons = sons;
    }

    public List<String> getSymNames() {
        return symNames;
    }

    public List<SkeletonTree> getSons() {
        return sons;
    }

    public boolean isInRoot(String name) {
        return symNames.contains(name);
    }

    public String showSymNames() {
        return symNames.size() == 1 ? symNames.get(0) : "{"+ Joiner.on(",").join(symNames) + "}" ;
    }


    @Override
    public String toString() {
        return sons.isEmpty() ? showSymNames() : "("+ showSymNames() +" "+ Joiner.on(" ").join(sons) +")" ;
    }
}
