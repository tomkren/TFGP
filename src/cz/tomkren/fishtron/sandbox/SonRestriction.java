package cz.tomkren.fishtron.sandbox;

/** Created by tom on 24.11.2015.*/

public class SonRestriction {

    private final int sonIndex;
    private final String forbiddenName;

    public SonRestriction(int sonIndex, String forbiddenName) {
        this.sonIndex = sonIndex;
        this.forbiddenName = forbiddenName;
    }

    public int getSonIndex() {
        return sonIndex;
    }

    public String getForbiddenName() {
        return forbiddenName;
    }
}
