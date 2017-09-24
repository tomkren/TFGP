package cz.tomkren.fishtron.ugen.cache;


import net.fishtron.types.Sub;
import net.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.cache.data.EncodedTs1Res;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 18. 7. 2016. */

class TypeData {

    private Map<Integer,SizeData> sizeDataMap;
    private List<EncodedTs1Res> ts1Data;

    TypeData() {
        this(new HashMap<>(), null);
    }

    private TypeData(Map<Integer,SizeData> sizeDataMap, List<EncodedTs1Res> ts1Data) {
        this.sizeDataMap = sizeDataMap;
        this.ts1Data = ts1Data;
    }

    List<Ts1Res> ts1(Type t, Gen gen, Cache cache) {
        if (ts1Data == null) {
            List<Ts1Res> ts1results = gen.ts1_compute(t, 0);
            ts1Data = F.map(ts1results, r -> encode(cache, r));
        }
        return F.map(ts1Data, r -> decode(cache, r));
    }

    private static EncodedTs1Res encode(Cache cache, Ts1Res ts1Res) {
        int sub_id = cache.addSub(ts1Res.getSigma());
        return new EncodedTs1Res(ts1Res.getSym(), sub_id, ts1Res.getNextVarId());
    }

    private static Ts1Res decode(Cache cache, EncodedTs1Res r) {
        Sub sub = cache.getSub(r.getSub_id());
        return new Ts1Res(r.getSym(), sub, r.getNextVarId());
    }


    SizeData getSizeData(int k) {
        return sizeDataMap.computeIfAbsent(k,_k -> new SizeData());
    }


    private static final String SIZE_DATA_KEY = "sizeData";
    private static final String TS1_DATA_KEY  = "ts1Data";


    JSONObject toJson() {

        Object ts1Data_json = JSONObject.NULL;
        if (ts1Data != null) {
            ts1Data_json = F.jsonMap(ts1Data, EncodedTs1Res::toJson);
        }

        return F.obj(
                SIZE_DATA_KEY, F.jsonMap(sizeDataMap, SizeData::toJson),
                TS1_DATA_KEY,  ts1Data_json
        );
    }

    public static TypeData fromJson(Object data) {
        JSONObject d = (JSONObject) data;

        Object ts1data_json = d.get(TS1_DATA_KEY);
        List<EncodedTs1Res> newTs1Data = null;
        if (ts1data_json != JSONObject.NULL) {
            newTs1Data = F.map((JSONArray) ts1data_json, EncodedTs1Res::fromJson);
        }

        JSONObject sizeData_json = d.getJSONObject(SIZE_DATA_KEY);
        Map<Integer,SizeData> newSizeData = F.map(sizeData_json, Integer::parseInt, SizeData::fromJson);

        return new TypeData(newSizeData, newTs1Data);
    }


}
