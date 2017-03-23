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

    public int getNumStates() {return numStates;}
    public String getPlazaDir() {return plazaDir;}
    public JSONArray getPixelSizes() {return pixelSizes;}
    public boolean isSilent() {return silent;}
}
