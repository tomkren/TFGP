function mkTypes () {



    function show_internal (typeJson,hideFirstParens) {
        if (_.isArray(typeJson)) {

            if (isFun(typeJson)) {
                return showFun(typeJson,hideFirstParens);
            } else if (isPair(typeJson)) {
                return showTuple(typeJson);
            } else {
                var coreStr = _.join(_.map(typeJson, showAll),' ');
                return hideFirstParens ? coreStr : '('+coreStr+')';
            }

        } else {
            return typeJson;
        }
    }


    function show (typeJson) {
        return show_internal(typeJson,true);
    }

    function showAll (typeJson) {
        return show_internal(typeJson,false);
    }

    function isFun (typeJson) {
        return _.isArray(typeJson) && typeJson[1] === '->' && typeJson.length === 3;
    }

    function isPair (typeJson) {
        return _.isArray(typeJson) && typeJson[0] === 'P' && typeJson.length === 3;
    }

    function getTupleArray (typeJson) {
        var ret = [];
        var acc = typeJson;
        while (isPair(acc)) {
            ret.push(acc[1]);
            acc = acc[2];
        }
        ret.push(acc);
        return ret;
    }

    function showTuple(typeJson) {
        var tupleArr = getTupleArray(typeJson);
        return '('+_.join(_.map(tupleArr, show),',')+')';
    }

    function showFun(typeJson,hideFirstParens) {
        var t1 = typeJson[0];
        var t2 = typeJson[2];

        var coreStr = showAll(t1) +' -> '+ show(t2);
        return hideFirstParens ? coreStr : '('+coreStr+')';
    }

    return {
      show:show
    };
}

var Types = mkTypes();
