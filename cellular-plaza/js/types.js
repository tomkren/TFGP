function mkTypes () {

    function diff (t1,t2) {

        var diffTab = diffTable(t1,t2);

        function mkPair(txt1,txt2) {
            return $('<div>').append([
                $('<p>').text(txt1),
                $('<p>').text(txt2)
            ]);
        }

        function showTab (tab) {
            if (_.isArray(tab)) {

                var body = _.map(tab, function (x) {
                    return $('<td>').html(showTab(x));
                });

                var $tr = $('<tr>').append(
                   _.concat(
                       [$('<td>').html(mkPair('(','('))],
                       body,
                       [$('<td>').html(mkPair(')',')'))]
                   )
                );

                return $('<table>').append($tr);

            } else {

                var $div = mkPair(tab.col[0],tab.col[1]);

                if (!tab.ok) {
                    $div.addClass('error');
                }

                return $div;
            }
        }

        return showTab(diffTab);
    }

    function diffTable (t1,t2) {
        if (show(t1) === show(t2)) {
            return [{col:[show(t1),show(t2)],ok:true}];
        } else if (_.isArray(t1) && _.isArray(t2) && t1.length === t2.length) {

            return [_.flatten(_.map(_.zip(t1,t2), function (p) {
                return diffTable(p[0],p[1]);
            }))];

        } else {
            return [{col:[show(t1),show(t2)],ok:false}];
        }
    }


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
        show:show,
        diffTable:diffTable,
        diff:diff
    };
}

var Types = mkTypes();
