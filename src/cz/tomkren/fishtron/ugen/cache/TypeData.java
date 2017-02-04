package cz.tomkren.fishtron.ugen.cache;


import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.utils.F;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 18. 7. 2016. */

class TypeData {

    private Map<Integer,SizeTypeData> sizeDataMap;
    private List<Ts1Res> ts1results;

    TypeData() {
        sizeDataMap = new HashMap<>();
        ts1results = null;
    }

    List<Ts1Res> getTs1(Type t, Gen gen) {
        if (ts1results == null) {
            ts1results = gen.ts_1_compute(t, 0);
        }
        return ts1results;
    }

    SizeTypeData getSizeTypeData(int k) {
        return sizeDataMap.computeIfAbsent(k,_k -> new SizeTypeData());
    }


    // TODO přidat i ts1results
    JSONObject toJson() {
        return F.jsonMap(sizeDataMap, SizeTypeData::toJson);
    }


}
