package cz.tomkren.fishtron.ugen.cache;


import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.cache.data.EncodedTs1Res;
import cz.tomkren.fishtron.ugen.data.PreTs1Res;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.utils.F;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 18. 7. 2016. */

class TypeData {

    private Map<Integer,SizeData> sizeDataMap;
    private List<EncodedTs1Res> ts1Data;

    TypeData() {
        sizeDataMap = new HashMap<>();
        ts1Data = null;
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


    JSONObject toJson() {

        Object ts1Data_json = JSONObject.NULL;
        if (ts1Data != null) {
            ts1Data_json = F.jsonMap(ts1Data, EncodedTs1Res::toJson);
        }

        return F.obj(
                "sizeData", F.jsonMap(sizeDataMap, SizeData::toJson),
                "ts1Data",  ts1Data_json
        );
    }


}
