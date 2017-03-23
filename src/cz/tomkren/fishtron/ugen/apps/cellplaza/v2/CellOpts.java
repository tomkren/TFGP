package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import org.json.JSONArray;

/**Created by tom on 22.03.2017.*/

public class CellOpts {
    private final int numStates;
    private final String plazaDir;
    private final JSONArray pixelSizes;
    private final boolean silent;

    public CellOpts(int numStates, String plazaDir, JSONArray pixelSizes, boolean silent) {
        this.numStates = numStates;
        this.plazaDir = plazaDir;
        this.pixelSizes = pixelSizes;
        this.silent = silent;
    }

    int getNumStates() {return numStates;}
    String getPlazaDir() {return plazaDir;}
    JSONArray getPixelSizes() {return pixelSizes;}
    public boolean isSilent() {return silent;}
}
