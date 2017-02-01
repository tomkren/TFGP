package cz.tomkren.fishtron.ugen.cache;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.utils.ABC;
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

    public List<Gen.SubsRes> subs_k_caching(int k, Type t, int nextVarId) {
        SizeTypeData sizeTypeData = getSizeTypeData(k, t, nextVarId);
        return decodeSubs(sizeTypeData,nextVarId,t);
    }

    public BigInteger computeNum(int k, Type t, int nextVarId) {
        SizeTypeData sizeTypeData = getSizeTypeData(k, t, nextVarId);
        return sizeTypeData.computeNum();
    }

    // -- private methods ---------------------------------------------------------------------------

    private SizeTypeData getSizeTypeData(int k, Type t, int nextVarId) {
        TypeData typeData = typeDataMap.computeIfAbsent(t.toString(), key->new TypeData());
        SizeTypeData sizeTypeData = typeData.getSizeTypeData(k);

        if (!sizeTypeData.isComputed()) {
            List<Gen.SubsRes> subs = gen.subs_k_compute(k, t, nextVarId);
            int t_nvi = t.getNextVarId(nextVarId);
            sizeTypeData.set(encodeSubs(subs),t_nvi);
        }

        return sizeTypeData;
    }

    private List<ABC<BigInteger,Integer,Integer>> encodeSubs(List<Gen.SubsRes> subs) {
        return F.map(subs, this::encodeSub);
    }

    private ABC<BigInteger,Integer,Integer> encodeSub(Gen.SubsRes subData) {
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
        return ABC.mk(num,sub_id,nextVarId);
    }

    private List<Gen.SubsRes> decodeSubs(SizeTypeData sizeTypeData, int nextVarId, Type t) {

        List<ABC<BigInteger,Integer,Integer>> encodedSubs = sizeTypeData.getSubsData();
        int t_nvi = sizeTypeData.t_nvi();

        List<Gen.SubsRes> decoded_unIncremented =
                F.map(encodedSubs, p -> new Gen.SubsRes(p._1(), subsList.get(p._2()), p._3()) );

        int new_t_nvi = t.getNextVarId(nextVarId);

        if (new_t_nvi <= t_nvi) {
            return decoded_unIncremented; // todo opravdu ???
        }

        return F.map(decoded_unIncremented, subData -> incrementDecodedSub(subData, t_nvi, new_t_nvi));
    }

    // todo | neni to nejaky celý blbě? rozmyslet co když to je poprvý volany že nextVarId_input = 0, že to je naky chlupatý
    // todo | ...

    private static Gen.SubsRes incrementDecodedSub(Gen.SubsRes subData, int t_nvi, int new_t_nvi) {

        int delta = new_t_nvi - t_nvi;
        Sub sub = subData.getSigma();

        Set<Integer> codomainVarIds = sub.getCodomainVarIds();

        Sub incrementSub = new Sub();

        for (Integer codomVarId : codomainVarIds) {
            if (codomVarId >= t_nvi) {
                incrementSub.add(codomVarId, new TypeVar(codomVarId + delta)); // TODO nekurvěj to nějak skolemizovaný ?
            }
        }

        Sub newSub = Sub.dot(incrementSub,sub);

        return new Gen.SubsRes(subData.getNum(), newSub, subData.getNextVarId() + delta);
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

    private static JSONArray sizeTypeDataToJson(List<ABC<BigInteger,Integer,Integer>> subs) {
        return F.jsonMap(subs, p -> F.arr(p._1().toString(),p._2()));
    }


}
