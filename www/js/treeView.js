// == TREE VIEW component ========
function mkTreeView($el, config) {

    // -- default config --
    config = _.assign({
        treantContainerName: 'treeView-treant',
        height: 400
    }, config || {});

    // -- listeners -------
    var clickNodeListeners = [];

    // -- components ------
    var nodeInfo;
    var typeInfo;

    // -- useful elems ----
    var $textArea;
    var $typeInfo;
    var $nodeInfo;

    // -- constants -------
    var nodeIdPrefix = 'node-';
    var nodeLabelPrefix = 'node-L-';

    // -- state vars ------
    var id2subtree;
    var nextNodeId;
    var $previousSelectedNode;
    var errorNodeIds;


    // ==  High level functions  ===========================

    // Main constructor.
    function init () {
        resetState();
        renderHTML();
        initComponents();
        renderInputTree();
    }

    // Initializes auxiliary state variables for shown tree.
    function resetState () {
        id2subtree = {};
        nextNodeId = 0;
        $previousSelectedNode = undefined;
        errorNodeIds = [];
    }

    // Constructs html elements using jquery.
    function renderHTML () {
        $el.html('');
        $el.append(mkInputSection(), mkNodeSection(), mkTreeSection());
    }

    // Constructs sub-components of this component.
    function initComponents () {
        nodeInfo = mkNodeInfo($nodeInfo);
        typeInfo = mkTypeInfo($typeInfo);
    }


    // == HTML =============================================

    function mkInputSection () {
        $textArea = $('<textarea>')
            .css({width: '80%'})
            .text(JSON.stringify(mkBadTreeExample()));

        var $updateButt = $('<button>')
            .text('update')
            .css({
                width: '16%',
                height: '35px',
                display: 'block',
                float:'right'
            }).click(renderInputTree);

        return $('<div>').append($textArea, $updateButt);
    }

    function mkNodeSection () {
        $nodeInfo = $('<div>').css({
            border: '1px solid black'
        });
        return $nodeInfo;
    }

    function mkTreeSection () {
        var $treantContainer = $('<div>')
            .attr('id',config.treantContainerName)
            .css({
                flex: '0 0 50%',
                height: config.height+'px',
                'border-right': '1px solid black'
            });

        $typeInfo = $('<div>').addClass('typeInfo');

        var $typeInfoWrapper = $('<div>')
            .css({
                flex: '1',
                'padding-left': '15px'
            }).html($typeInfo);

        return $('<div>')
            .css({
                display: 'flex',
                border: '1px solid black',
                'border-top': '0'
            })
            .append($treantContainer, $typeInfoWrapper);
    }


    // == Core functions ======================================

    function renderInputTree() {
        resetState();

        var tree = loadTree();

        typeInfo.render(tree);

        var treantConfig = {
            chart: {container: "#"+config.treantContainerName},
            nodeStructure: processTree(tree, nodeIdPrefix)
        };

        $('#'+config.treantContainerName).html('');
        new Treant(treantConfig, doAfterTreeIsRendered);
    }

    function loadTree() {
        var treeJson = JSON.parse($textArea.val());
        console.log(treeJson);
        return treeJson;
    }

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

    function doAfterTreeIsRendered () {
        addVeryShortTypeLabels();
        findErrorNodes();
        markErrorNodes();
        addNodeClicks();
        selectNode(getDefaultSelectedNode());
    }

    function addVeryShortTypeLabels() {
        _.forIn(id2subtree, function (subtree, id) {

            var $node = $('#' + nodeIdPrefix + id);
            var pos = $node.position();
            var width = $node.width();

            var $box = $('<div>').attr({id: nodeLabelPrefix + id}).css({
                position: 'absolute',
                left: (pos.left + width + 3) + 'px',
                top: (pos.top + 5) + 'px',
                color: 'gray',
                'font-size': 'xx-small'
            }).text(' : T' + subtree.typeInfo.i);

            $node.parent().append($box);
        });
    }

    function findErrorNodes () {
        _.forIn(id2subtree, function (subtree) {
            if (subtree.error === true) {
                errorNodeIds.push(subtree.fun.id);
            }
        });
    }

    function markErrorNodes () {
        _.each(errorNodeIds, function (errorNodeId) {
            $('#'+nodeLabelPrefix+errorNodeId).css({color:'red'});
        });
    }

    function addNodeClicks () {
        _.forIn(id2subtree, function (subtree, id) {
            $('#'+nodeIdPrefix+id).click(function (e) {
                selectNode(id);
                _.each(clickNodeListeners, function (callback) {
                    callback(subtree, e);
                });
            });
        });
    }

    function selectNode(id) {
        var $selected = $('#'+nodeIdPrefix+id);
        if ($previousSelectedNode !== undefined) {
            $previousSelectedNode.removeClass('selectedNode');
        }
        $selected.addClass('selectedNode');
        $previousSelectedNode = $selected;

        nodeInfo.render(id2subtree[id]);
    }

    function getDefaultSelectedNode() {
        return _.isEmpty(errorNodeIds) ? 0 : errorNodeIds[0];
    }


    // ==  Listener adding  =================================

    function addClickNodeListener (callback) {
        clickNodeListeners.push(callback);
    }

    // == Constructor execution =============================

    init();

    // == Public interface ==================================

    return {
        getTypeInfo : function () {return typeInfo;},
        addClickNodeListener: addClickNodeListener
    };
}