package net.fishtron.gen.cache;

import net.fishtron.types.Sub;
import net.fishtron.types.Type;
import net.fishtron.gen.Gen;
import net.fishtron.gen.cache.data.EncodedSubsRes;
import net.fishtron.gen.data.SubsRes;
import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.List;

/** Created by user on 18. 7. 2016. */

class SizeData {

    private List<EncodedSubsRes> subsData;

    SizeData() {
        subsData = null;
    }

    private SizeData(List<EncodedSubsRes> subsData) {
        this.subsData = subsData;
    }

    private void ensureSubsDataIsComputed(Gen gen, Cache cache, int k, Type t) {
        if (subsData == null) {
            List<SubsRes> subs = gen.subs_compute(k, t, 0);
            subsData = F.map(subs, subsRes -> encode(cache, subsRes));
        }
    }

    BigInteger computeNum(Gen gen, Cache cache, int k, Type t) {
        ensureSubsDataIsComputed(gen, cache, k ,t);
        BigInteger sum = BigInteger.ZERO;
        for (EncodedSubsRes sd : subsData) {sum = sum.add(sd.getNum());}
        return sum;
    }

    List<SubsRes> getSubsData(Gen gen, Cache cache, int k, Type t) {
        ensureSubsDataIsComputed(gen, cache, k ,t);
        return F.map(subsData, p -> decode(cache, p));
    }

    private static EncodedSubsRes encode(Cache cache, SubsRes r) {
        int sub_id = cache.addSub(r.getSigma());
        return new EncodedSubsRes(r.getNum(), sub_id, r.getNextVarId());
    }

    private static SubsRes decode(Cache cache, EncodedSubsRes r) {
        Sub sub = cache.getSub(r.getSub_id());
        return new SubsRes(r.getNum(), sub, r.getNextVarId());
    }


    Object toJson() {
        if (subsData == null) {
            return JSONObject.NULL;
        } else {
            return F.jsonMap(subsData, EncodedSubsRes::toJson);
        }
    }

    static SizeData fromJson(Object data) {
        if (data == JSONObject.NULL) {
            return new SizeData();
        } else {
            return new SizeData(F.map((JSONArray) data, EncodedSubsRes::fromJson));
        }
    }

}
