package cz.tomkren.fishtron.ugen.cache;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.*;

/** Created by user on 13. 9. 2016.*/

public class Cache {

    private Gen gen;

    private Map<String, TypeData> typeDataMap;
    private List<Sub> subsList;
    private Map<String, Integer> sub2id;

    public Cache(Gen gen) {
        this.gen = gen;
        typeDataMap = new HashMap<>();
        subsList = new ArrayList<>();
        sub2id = new HashMap<>();
    }

    // -- main public interface ----------------------------------------------------------------------

    public List<SubsRes> subs_caching(int k, Type t, int nextVarId) {
        SizeTypeData sizeTypeData = getSizeTypeData(k, t);
        return decodeSubs(sizeTypeData, t, nextVarId);
    }

    public BigInteger computeNum(int k, Type t) {
        SizeTypeData sizeTypeData = getSizeTypeData(k, t);
        return sizeTypeData.computeNum();
    }

    // -- private methods ---------------------------------------------------------------------------

    private SizeTypeData getSizeTypeData(int k, Type t) {
        TypeData typeData = typeDataMap.computeIfAbsent(t.toString(), key->new TypeData());
        SizeTypeData sizeTypeData = typeData.getSizeTypeData(k);

        if (!sizeTypeData.isComputed()) {
            List<SubsRes> subs = gen.subs_compute(k, t,/*bylo nvi*/ 0);
            //int t_nvi = t.getNextVarId(nextVarId);
            List<EncodedSubsRes> encodedSubs = F.map(subs, this::encodeSub);
            sizeTypeData.set(encodedSubs/*,t_nvi*/);
        }

        return sizeTypeData;
    }

    private EncodedSubsRes encodeSub(SubsRes subData) {
        Sub sub = subData.getSigma();
        String sub_str = sub.toString();
        Integer sub_id = sub2id.get(sub_str);

        if (sub_id == null) {
            sub_id = subsList.size();
            subsList.add(sub);
            sub2id.put(sub_str, sub_id);
        }

        BigInteger num = subData.getNum();
        int nextVarId  = subData.getNextVarId();
        return new EncodedSubsRes(num,sub_id,nextVarId);
    }

    private List<SubsRes> decodeSubs(SizeTypeData sizeTypeData, Type t, int nextVarId) {

        List<EncodedSubsRes> encodedSubs = sizeTypeData.getSubsData();

        List<SubsRes> decoded_unIncremented = F.map(encodedSubs,
                p -> new SubsRes(p.getNum(), subsList.get(p.getEncodedSub()), p.getNextVarId()));

        int t_nvi = t.getNextVarId(0); //sizeTypeData.t_nvi();
        int new_t_nvi =  Math.max(t_nvi, nextVarId);// což se == t.getNextVarId(nextVarId);

        if (new_t_nvi != t.getNextVarId(nextVarId)) {
            throw new Error("Cache.decodeSubs assert fail: new_t_nvi != t.getNextVarId(nextVarId)");
        }

        if (new_t_nvi < t_nvi) {
            throw new Error("Cache.decodeSubs error: new_t_nvi < t_nvi, should never be true.");
        } else if (new_t_nvi == t_nvi) {
            return decoded_unIncremented;
        } else {
            return F.map(decoded_unIncremented, subsRes -> incrementDecodedSub(subsRes, t_nvi, new_t_nvi));
        }

    }

    private static SubsRes incrementDecodedSub(SubsRes subsRes, int t_nvi, int new_t_nvi) {

        int delta = new_t_nvi - t_nvi;
        Sub sub = subsRes.getSigma();

        Set<Integer> codomainVarIds = sub.getCodomainVarIds();

        Sub incrementSub = new Sub();

        for (Integer codomVarId : codomainVarIds) {
            if (codomVarId >= t_nvi) {
                incrementSub.add(codomVarId, new TypeVar(codomVarId + delta));
            }
        }

        Sub newSub = Sub.dot(incrementSub,sub);

        return new SubsRes(subsRes.getNum(), newSub, subsRes.getNextVarId() + delta);
    }


    // -- toJson method and utils ---------------------------------------------------------------

    public JSONObject toJson() {
        return F.obj(
                "types", typesToJson(typeDataMap),
                "subs",  subsToJson(subsList)
        );
    }

    private static JSONObject typesToJson(Map<String,TypeData> typeDataMap) {
        return F.jsonMap(typeDataMap, Cache::typeDataToJson);
    }

    private static JSONArray subsToJson(List<Sub> subsList) {
        return F.jsonMap(subsList, Sub::toJson);
    }

    private static JSONObject subsToJson_debugVersion(List<Sub> subsList) {
        JSONObject ret = new JSONObject();
        for (int i = 0; i < subsList.size(); i++) {
            ret.put(Integer.toString(i), subsList.get(i).toJson());
        }
        return ret;
    }

    private static JSONObject typeDataToJson(TypeData td) {
        return F.jsonMap(td.getSizeDataMap(), x -> sizeTypeDataToJson(x.getSubsData()) );
    }

    private static JSONArray sizeTypeDataToJson(List<EncodedSubsRes> subs) {
        return F.jsonMap(subs, p -> F.arr(p.getNum().toString(),p.getEncodedSub()));
    }


}
