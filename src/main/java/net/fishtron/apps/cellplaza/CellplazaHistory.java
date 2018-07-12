package net.fishtron.apps.cellplaza;

import net.fishtron.apps.cellplaza.shared.PlazaImg;
import net.fishtron.apps.cellplaza.v2.CellPlaza;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**Created by tom on 23.03.2017.*/

class CellplazaHistory {

    private static String tileDir = CellPlaza.BASE_DIR + "/mini_10/cores";

    private final List<JSONObject> clickWinners;
    private final int numStates;
    private int version;

    CellplazaHistory(int numStates) {
        clickWinners = new ArrayList<>();
        this.numStates = numStates;
        version = 1;
    }

    void addClickWinner(String clickWinner, String clickWinner1px, int winnerId, int loserId) {
        clickWinners.add(F.obj(
                "winnerId", winnerId,
                "loserId", loserId,
                "frame",clickWinner,
                "frame1px",clickWinner1px
        ));
        version++;
    }

    private JSONObject getTiles() {

        File path = new File(tileDir);
        String[] fs = path.list();
        if (fs == null) {throw new Error("Probably wrong tile dir path: "+tileDir);}

        List<String> names = new ArrayList<>(Arrays.asList(fs));

        Comparator<JSONObject> comp = Comparator.comparingInt(o -> o.getInt("cSum"));
        List<JSONObject> tiles = names.stream().map(this::mkTileInfo).sorted(comp).collect(Collectors.toList());

        JSONArray packs = new JSONArray();

        int numTilesInOnePack = tiles.size() / numStates;
        for (int i = 0; i<numStates; i++) {

            int from = i * numTilesInOnePack;
            int to = i < numStates-1 ? (i+1) * numTilesInOnePack : tiles.size();

            List<JSONObject> pack = tiles.subList(from, to);
            packs.put(F.jsonMap(pack));
        }



        return F.obj(
                "dir", tileDir,
                "tiles", packs //F.jsonMap(tiles)
        );
    }

    private JSONObject mkTileInfo(String filename) {
        return F.obj(
                "src", filename,
                "cSum", sumColor(filename)
        );
    }

    private int sumColor(String tileName) {
        File tileFile = new File(tileDir+"/"+tileName);
        PlazaImg plazaImg = new PlazaImg(tileFile);
        return plazaImg.sumColor();
    }


    JSONObject toJson() {
        return F.obj(
                "clickWinners", F.jsonMap(clickWinners),
                "tiles", getTiles(),
                "version", version
        );
    }

    public int getVersion() {
        return version;
    }
}
