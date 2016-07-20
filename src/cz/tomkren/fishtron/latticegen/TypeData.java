package cz.tomkren.fishtron.latticegen;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.TODO;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/** Created by tom on 18. 7. 2016. */

public class TypeData {

    private Map<Integer,SizeData> sizeDataMap;

    SizeData getSizeData(int k) {
        return sizeDataMap.computeIfAbsent(k,key -> new SizeData());
    }


}
