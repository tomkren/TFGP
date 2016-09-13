package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.TypeVar;
import cz.tomkren.utils.ABC;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.*;

/** Created by user on 13. 9. 2016.*/

class Cache {

    private LSolver lSolver;

    private Map<String, TypeData> typeDataMap;
    private List<Sub> subsList;
    private Map<String, Integer> sub2id;

    Cache(LSolver lSolver) {
        this.lSolver = lSolver;
        typeDataMap = new HashMap<>();
        subsList = new ArrayList<>();
        sub2id = new HashMap<>();
    }

    JSONObject toJson() {
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

    private static JSONObject typeDataToJson(TypeData td) {
        return F.jsonMap(td.getSizeDataMap(), x -> sizeTypeDataToJson(x.getSubsData()) );
    }

    private static JSONArray sizeTypeDataToJson(List<ABC<BigInteger,Integer,Integer>> subs) {
        return F.jsonMap(subs, p -> F.arr(p._1().toString(),p._2()));
    }

    List<ABC<BigInteger,Sub,Integer>> subs_k_caching(int k, Type t, int nextVarId) {
        SizeTypeData sizeTypeData = getSizeTypeData(k, t, nextVarId);
        return decodeSubs(sizeTypeData,nextVarId);
    }

    SizeTypeData getSizeTypeData(int k, Type t, int nextVarId) {
        TypeData typeData = typeDataMap.computeIfAbsent(t.toString(), key->new TypeData());
        SizeTypeData sizeTypeData = typeData.getSizeTypeData(k);

        if (!sizeTypeData.isComputed()) {
            List<ABC<BigInteger,Sub,Integer>> subs = lSolver.subs_k_noCaching(k, t, nextVarId);
            //bylo tu:  ... = core_k(k, t, subs_1(gamma), this::subs_ij, LSolver::packSubs, nextVarId);
            sizeTypeData.set(encodeSubs(subs),nextVarId);
        }

        return sizeTypeData;
    }

    private List<ABC<BigInteger,Integer,Integer>> encodeSubs(List<ABC<BigInteger,Sub,Integer>> subs) {
        return F.map(subs, this::encodeSub);
    }

    private ABC<BigInteger,Integer,Integer> encodeSub(ABC<BigInteger,Sub,Integer> subData) {
        Sub sub = subData._2();
        String sub_str = sub.toString();
        Integer sub_id = sub2id.get(sub_str);

        if (sub_id == null) {
            sub_id = subsList.size();
            subsList.add(sub);
            sub2id.put(sub_str, sub_id);
        }

        BigInteger num = subData._1();
        int nextVarId  = subData._3();
        return ABC.mk(num,sub_id,nextVarId);
    }

    private List<ABC<BigInteger,Sub,Integer>> decodeSubs(SizeTypeData sizeTypeData, int nextVarId) {

        List<ABC<BigInteger,Integer,Integer>> encodedSubs = sizeTypeData.getSubsData();
        int nextVarId_input = sizeTypeData.getNextVarId_input();

        List<ABC<BigInteger,Sub,Integer>> unIncremented = F.map(encodedSubs, p -> ABC.mk( p._1(), subsList.get(p._2()), p._3()));

        if (nextVarId <= nextVarId_input) {
            return unIncremented;
        }

        return F.map(unIncremented, subData -> incrementDecodedSub(subData, nextVarId_input, nextVarId));
    }

    private static ABC<BigInteger,Sub,Integer> incrementDecodedSub(ABC<BigInteger,Sub,Integer> subData, int nextVarId_input, int nextVarId) {

        int delta = nextVarId - nextVarId_input;
        Sub sub = subData._2();

        Set<Integer> codomainVarIds = sub.getCodomainVarIds();

        Sub incrementSub = new Sub();

        // todo | neni to nejaky celý blbě? rozmyslet co když to je poprvý volany že nextVarId_input = 0, že to je naky chlupatý
        // todo | ...

        for (Integer codomVarId : codomainVarIds) {
            if (codomVarId >= nextVarId_input) {
                incrementSub.add(codomVarId, new TypeVar(codomVarId + delta)); // TODO nekurvěj to nějak skolemizovaný ?
            }
        }

        Sub newSub = Sub.dot(incrementSub,sub);

        return ABC.mk(subData._1(), newSub, subData._3() + delta);
    }
}
