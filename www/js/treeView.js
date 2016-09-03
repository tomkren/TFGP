function mkTreeView($el, config) {



    var clickNodeListeners = [];
    var nodeIdPrefix = 'node-';
    var nodeLabelPrefix = 'node-L-';


    function addClickNodeListener (callback) {
        clickNodeListeners.push(callback);
    }

    config = _.assign({
        treantContainerName: 'treeView-treant',
        height: 400
    }, config || {});

    $el.html('');

    var $textArea = $('<textarea>')
        .css({width: '80%'})
        .text(JSON.stringify(mkBadTreeExample()));

    var $updateButt = $('<button>')
        .text('update')
        .css({
            width: '18%',
            height: '35px',
            display: 'block',
            float:'right'
        }).click(load);

    var $treantContainer = $('<div>')
        .attr('id',config.treantContainerName)
        .css({
            flex: '0 0 50%',
            height: config.height+'px',
            'border-right': '1px solid black'
        });

    var $typeInfo = $('<div>').addClass('typeInfo');

    var $typeBox = $('<div>')
        .css({
            flex: '1',
            'padding-left': '15px'
        }).html($typeInfo);

    var $wrapper = $('<div>')
        .css({
            display: 'flex',
            border: '1px solid black',
            'border-top': '0'
        })
        .append($treantContainer, $typeBox);

    var $nodeInfo = $('<div>').css({
        //width: '100%',
        border: '1px solid black'
    });

    $el.append($textArea, $updateButt, $nodeInfo, $wrapper);

    function loadTree() {
        var treeJson = JSON.parse($textArea.val());
        console.log(treeJson);
        return treeJson;
    }

    var id2subtree = {};
    var nextNodeId = 0;

    function processTree(tree, idPrefix) {

        var nodeId = idPrefix+nextNodeId;
        id2subtree[nextNodeId] = tree;
        tree.id = nextNodeId;
        nextNodeId++;

        function mkChildren(tree) {
            return [
                processTree(tree.fun, idPrefix),
                processTree(tree.arg, idPrefix)
            ];
        }

        var nodeStructure;

        if (tree.node === '@') {
            nodeStructure = {
                text : {name : '@'},
                HTMLclass : 'app',
                children : mkChildren(tree)
            };
        } else {
            nodeStructure = {
                HTMLclass: 'leaf',
                text :{name : tree.node /*,title: ':Tx'*/}
            };
        }

        nodeStructure.HTMLid = nodeId;

        return nodeStructure;
    }

    var $previousSelectedNode = undefined;

    function selectNode(id) {
        var $selected = $('#'+nodeIdPrefix+id);
        if ($previousSelectedNode !== undefined) {
            $previousSelectedNode.removeClass('selectedNode');
        }
        $selected.addClass('selectedNode');
        $previousSelectedNode = $selected;

        showNodeInfo(id2subtree[id]);
    }

    function showNodeInfo(subtree) {

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

    function addUltraShortType(subtree, id) {

        var $node = $('#'+nodeIdPrefix+id);
        var pos = $node.position();
        var width = $node.width();

        var $box = $('<div>').attr({id:nodeLabelPrefix+id}).css({
            position:'absolute',
            left: (pos.left + width+3)+'px',
            top:  (pos.top + 5) + 'px',
            color: 'gray',
            'font-size':'xx-small'
        }).text(' : T'+subtree.typeInfo.i);

        $node.parent().append($box);
    }

    var errorNode = undefined;

    function addClicks() {

        _.forIn(id2subtree, function (subtree, id) {

            addUltraShortType(subtree, id);

            if (subtree.error === true) {
                errorNode = subtree.fun.id;
            }

            $('#'+nodeIdPrefix+id).click(function (e) {
                selectNode(id);
                _.each(clickNodeListeners, function (callback) {
                    callback(subtree, e);
                });
            });
        });

    }


    var typeInfo;

    function load() {

        id2subtree = {};
        nextNodeId = 0;
        $previousSelectedNode = undefined;

        $('#'+config.treantContainerName).html('');

        var tree = loadTree();

        typeInfo = mkTypeInfo(tree);
        $typeInfo.html(typeInfo.getSummary());

        new Treant({
            chart: {container: "#"+config.treantContainerName},
            nodeStructure: processTree(tree, nodeIdPrefix)
        }, function () {
            addClicks();
            selectNode(errorNode === undefined ? 0 : errorNode);
            $('#'+nodeLabelPrefix+errorNode).css({color:'red'});
        });
    }

    load();

    return {
        getTypeInfo : function () {return typeInfo;},
        addClickNodeListener: addClickNodeListener
    };
}