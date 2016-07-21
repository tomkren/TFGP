package cz.tomkren.fishtron.latticegen;

import java.util.HashMap;
import java.util.Map;

/** Created by tom on 18. 7. 2016. */

class TypeData {

    private Map<Integer,SizeTypeData> sizeDataMap;

    TypeData() {
        sizeDataMap = new HashMap<>();
    }

    SizeTypeData getSizeTypeData(int k) {
        return sizeDataMap.computeIfAbsent(k,_k -> new SizeTypeData());
    }

    public Map<Integer, SizeTypeData> getSizeDataMap() {
        return sizeDataMap;
    }
}
