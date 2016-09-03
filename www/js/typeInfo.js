function mkTypeInfo(tree) {

    var subtypes = processTree(tree);

    function processTree(tree) {

        var subtypes = [];

        var queue = [{tree: tree, isArg: true, iParent:undefined}];
        var i = 0;

        while (!_.isEmpty(queue)) {
            var head = queue.shift();

            var iParent = head.iParent;
            var iMy = i;
            i++;

            var subtree = head.tree;
            var isArg = head.isArg;
            var type = subtree.type;

            var typeInfo = {
                i: iMy,
                isShort: !isArg,
                type: isArg ? type : [iMy-1,iParent],
                origo: type,
                getShort: mkGetShort(iMy),
                getExpanded: mkGetExpanded(iMy)
            };

            subtypes.push(typeInfo);
            subtree.typeInfo = typeInfo;

            if (subtree.node === '@') {
                queue.push({tree: subtree.arg, isArg: true,  iParent: iMy});
                queue.push({tree: subtree.fun, isArg: false, iParent: iMy});
            }
        }

        return subtypes;
    }

    function mkGetShort(i) {
        return function () {
            return getShort(i) + ' = T'+i;
        };
    }

    function mkGetExpanded(i) {
        return function () {
            return Types.show(expand(i));
        };
    }

    function expand(i) {
        var o = subtypes[i];
        if (o.isShort) {
            var t1 = expand(o.type[0]);
            var t2 = expand(o.type[1]);
            return [t1,'->',t2];
        } else {
            return o.type;
        }
    }

    function getOrigo(i) {
        return Types.show(subtypes[i].origo);
    }

    function getShort(i) {
        var o = subtypes[i];
        if (o.isShort) {
            return 'T'+o.type[0] +' -> T'+ o.type[1];
        } else {
            return Types.show(o.type);
        }
    }


    function getSummary() {

        var rows = _.map(_.range(subtypes.length),function (i) {
            var expanded = Types.show(expand(i));
            var origo    = getOrigo(i);

            var isError = (expanded !== origo);

            if (isError) {
                return '<p class="error">'+'T'+i+' =  ERROR !!!<p>';
            } else {
                return '<p>'+'T'+i+' = '+getShort(i)+'</p>';
            }


        });

        return _.join(rows ,'\n');

    }


    return {
        getSummary: getSummary
    };
}
