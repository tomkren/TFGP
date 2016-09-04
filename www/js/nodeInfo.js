function mkNodeInfo ($nodeInfo) {

    function render(subtree) {

        var typeInfo = subtree.typeInfo;

        var shortStr = typeInfo.getShort();
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

        function mkRow(title, text, isHtml) {

            var $td = $('<td>');
            if (isHtml) {
                $td.html(text);
            } else {
                $td.text(text);
            }

            return $('<tr>').append([
                $('<th>').text(title),
                $td
            ]);
        }

        var $box = $('<table>').addClass('nodeInfo').append([
            mkRow('original',originalStr),
            mkRow('shortType',shortStr),
            mkRow('node',subtree.node),
            mkRow('status',$status,true)
        ]);

        $nodeInfo.html($box);
    }

    return {
        render:render
    };
}