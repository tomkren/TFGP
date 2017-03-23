package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.utils.F;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**Created by tom on 23.03.2017.*/

class History {

    private final List<JSONObject> clickWinners;

    History() {
        clickWinners = new ArrayList<>();
    }

    void addClickWinner(String clickWinner, String clickWinner1px, int winnerId, int loserId) {
        clickWinners.add(F.obj(
                "winnerId", winnerId,
                "loserId", loserId,
                "frame",clickWinner,
                "frame1px",clickWinner1px
        ));
    }


    JSONObject toJson() {
        return F.obj(
                "clickWinners", F.jsonMap(clickWinners)
        );
    }

}
