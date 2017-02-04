package cz.tomkren.fishtron.ugen.cache;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.cache.data.PreSubsRes;
import cz.tomkren.fishtron.ugen.cache.data.PreTs1Res;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.utils.AB;
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

    public List<Ts1Res> ts_1_caching(Type t, int nextVarId) {
        TypeData typeData = getTypeData(t);
        List<PreTs1Res> ts1results_unmoved = typeData.getTs1(t, gen, this);
        return moveTs1Results(ts1results_unmoved, t, nextVarId);
    }

    public List<SubsRes> subs_caching(int k, Type t, int nextVarId) {
        SizeData sizeData = getSizeTypeData(k, t);
        List<PreSubsRes> decoded_unIncremented = sizeData.getSubsData(gen, this, k, t);
        return moveSubsResults(decoded_unIncremented, t, nextVarId);
    }

    public BigInteger computeNum_caching(int k, Type t) {
        SizeData sizeData = getSizeTypeData(k, t);
        return sizeData.computeNum(gen, this, k, t);
    }

    // -- private methods ---------------------------------------------------------------------------

    private TypeData getTypeData(Type t) {
        return typeDataMap.computeIfAbsent(t.toString(), key->new TypeData());
    }

    private SizeData getSizeTypeData(int k, Type t) {
        return getTypeData(t).getSizeTypeData(k);
    }

    int addSub(Sub sub) {
        String sub_str = sub.toString();
        Integer sub_id = sub2id.get(sub_str);
        if (sub_id == null) {
            sub_id = subsList.size();
            subsList.add(sub);
            sub2id.put(sub_str, sub_id);
        }
        return sub_id;
    }

    Sub getSub(int sub_id) {
        return subsList.get(sub_id);
    }


    private static List<Ts1Res> moveTs1Results(List<PreTs1Res> ts1results_unmoved, Type t, int nextVarId) {

        int tnvi_0 = t.getNextVarId(0);
        int tnvi_n = Math.max(tnvi_0, nextVarId);

        return F.map(ts1results_unmoved, pre_ts1Res -> moveTs1Res(pre_ts1Res, tnvi_0, tnvi_n));
    }

    private static List<SubsRes> moveSubsResults(List<PreSubsRes> decoded_unIncremented, Type t, int nextVarId) {

        int tnvi_0 = t.getNextVarId(0); //sizeTypeData.t_nvi();
        int tnvi_n = Math.max(tnvi_0, nextVarId);// což se == t.getNextVarId(nextVarId);

        if (tnvi_n != t.getNextVarId(nextVarId)) {
            throw new Error("Cache.moveSubsResults assert fail: tnvi_n != t.getNextVarId(nextVarId)");
        }

        if (tnvi_n < tnvi_0) {
            throw new Error("Cache.moveSubsResults error: tnvi_n < tnvi_0, should never be true.");
        } /*else if (tnvi_n == tnvi_0) {return decoded_unIncremented;}*/
        else {
            return F.map(decoded_unIncremented, pre_subsRes -> moveSubsRes(pre_subsRes, tnvi_0, tnvi_n));
        }
    }

    private static Ts1Res moveTs1Res(PreTs1Res preRes, int tnvi_0, int tnvi_n) {
        Sub sub = preRes.getSigma();
        AB<Sub,Integer> moveSubRes = moveSub(sub, tnvi_0, tnvi_n);
        return new Ts1Res(preRes.getSym(), moveSubRes._1(), moveSubRes._2());
    }

    private static SubsRes moveSubsRes(PreSubsRes preRes, int tnvi_0, int tnvi_n) {
        Sub sub = preRes.getSigma();
        AB<Sub,Integer> moveSubRes = moveSub(sub, tnvi_0, tnvi_n);
        return new SubsRes(preRes.getNum(), moveSubRes._1(), moveSubRes._2());
    }

    private static AB<Sub,Integer> moveSub(Sub sub, int tnvi_0, int tnvi_n) {
        TreeSet<Integer> codomainVarIds = sub.getCodomainVarIds();

        Sub incSub = new Sub();
        //int delta  = tnvi_n - tnvi_0;

        int nextVarId = tnvi_n;

        for (Integer varId : codomainVarIds) {
            if (varId >= tnvi_0) {
                //int newVarId = varId + delta;
                incSub.add(varId, new TypeVar(nextVarId));
                nextVarId ++;
            }
        }

        Sub movedSub = Sub.dot(incSub, sub);

        /*int nextVarId = tnvi_n;
        if (!codomainVarIds.isEmpty()) {
            nextVarId = codomainVarIds.last() + delta + 1;
        }*/

        return AB.mk(movedSub, nextVarId);
    }



    // -- toJson method and its utils ---------------------------------------------------------------

    public JSONObject toJson() {
        return F.obj(
                "types", typesToJson(typeDataMap),
                "subs",  subsToJson(subsList)
        );
    }

    private static JSONObject typesToJson(Map<String,TypeData> typeDataMap) {
        return F.jsonMap(typeDataMap, TypeData::toJson);
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


}
