function mkNodeInfo ($nodeInfo) {

    var isRowOn = {
        status: {isOn:false}
    };

    function render(subtree) {

        var $box = $('<table>').addClass('nodeInfo').append([
            mkRow('shortType',subtree.typeInfo.getShort()),
            mkRow('original',Types.show(subtree.type)),
            mkRow('node',subtree.node),
            mkRow('status',mkStatus(subtree),true),
            mkRow('debug log', mkDebugLog(subtree),true)
        ]);

        $nodeInfo.html($box);
        setRowsVisibility();
    }

    function mkRow(title, text, isHtml) {

        var $td = $('<td>');
        if (isHtml) {
            $td.html(text);
        } else {
            $td.text(text);
        }

        var $th = $('<th>').text(title).click(function () {
            var val = isRowOn[title];
            val.isOn = !val.isOn;
            setRowVis(val);
        });

        if (isRowOn[title] === undefined) {
            isRowOn[title] = {isOn:true};
        }
        isRowOn[title].$td = $td;
        isRowOn[title].$th = $th;


        return $('<tr>').append($th, $td);
    }

    function mkDebugLog (subtree) {
        var $table = $('<table>').addClass('nodeInfo');
        var log = subtree.debugInfo.log;
        var k = log.k;

        function addRow (key,val) {
            if (_.isFunction(val)) {val = val(log[key]);}
            val = val || log[key];
            $table.append($('<tr>').append($('<th>').text(key), $('<td>').text(val)));
        }


        addRow('k');
        addRow('input type',Types.show);
        addRow('normalized type',Types.show);
        addRow('ball','1+'+log.ball);

        if (k === 1) {
            addRow("s");
            addRow("t_s", Types.show);
            addRow("t_s_fresh", Types.show);
            addRow('mu', JSON.stringify);
        }

        addRow('fromNF',JSON.stringify);

        addRow('','...');
        addRow('(all-keys)', _.keys(log).toString());


        return $table;
    }

    function mkStatus(subtree) {

        var typeInfo = subtree.typeInfo;

        var expandedType = typeInfo.getExpanded();
        var originalType = subtree.type;

        var expandedStr = Types.show(expandedType);
        var originalStr = Types.show(originalType);

        var $status = $('<span>').text('OK');

        if (expandedStr !== originalStr) {

            var $diff = Types.diff(expandedType, originalType);

            $status = $('<div>').append([
                $('<p>').addClass('error').text('ERROR: expandedType !== origoType'),
                $('<p>').text('expanded: '+expandedStr),
                $('<p>').text('original: '+originalStr),
                $diff
            ]);
        }

        return $status;
    }

    function setRowVis (val) {
        if (val.isOn) {
            val.$td.show();
            val.$th.removeClass('offRow');
        } else {
            val.$td.hide();
            val.$th.addClass('offRow');
        }
    }

    function setRowsVisibility () {
        _.forIn(isRowOn, setRowVis);
    }

    return {
        render:render
    };
}