package cz.tomkren.fishtron.ugen.cache;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.utils.F;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.List;

/** Created by user on 18. 7. 2016. */

class SizeTypeData {

    private List<EncodedSubsRes> subsData;

    SizeTypeData() {
        subsData = null;
    }

    private void ensureSubsDataIsComputed(Gen gen, Cache cache, int k, Type t) {
        if (subsData == null) {
            List<SubsRes> subs = gen.subs_compute(k, t, 0);
            subsData = F.map(subs, subsRes -> encodeSub(cache, subsRes));
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
        return F.map(subsData, p -> decodeSub(cache, p));
    }

    private static EncodedSubsRes encodeSub(Cache cache, SubsRes subsRes) {
        int sub_id = cache.addSub(subsRes.getSigma());
        return new EncodedSubsRes(subsRes.getNum(), sub_id, subsRes.getNextVarId());
    }

    private static SubsRes decodeSub(Cache cache, EncodedSubsRes subsRes) {
        Sub sub = cache.getSub(subsRes.getSub_id());
        return new SubsRes(subsRes.getNum(), sub, subsRes.getNextVarId());
    }



    Object toJson() {
        if (subsData == null) {
            return JSONObject.NULL;
        } else {
            return F.jsonMap(subsData, p -> F.arr(p.getNum().toString(),p.getSub_id()));
        }
    }

}
