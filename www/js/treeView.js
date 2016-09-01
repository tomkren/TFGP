function mkTreeView($el, config) {

    var clickNodeListeners = [];

    function addClickNodeListener (callback) {
        clickNodeListeners.push(callback);
    }

    config = _.assign({
        treantContainerName: 'treeView-treant',
        height: 550
    }, config || {});

    $el.html('');

    var $treantContainer = $('<div>')
        .attr('id',config.treantContainerName)
        .css({
            height: config.height+'px',
            border: '1px solid black'
        });

    var $textArea = $('<textarea>')
        .css({
            width: '100%'
        }).text(JSON.stringify(mkBadTreeExample()));

    $el.append($textArea, $treantContainer);

    function loadTree() {
        var treeJson = JSON.parse($textArea.text());
        console.log(treeJson);
        return treeJson;
    }

    var id2subtree = {};
    var nextNodeId = 1;

    function processTree(tree, idPrefix) {

        var nodeId = idPrefix+nextNodeId;
        nextNodeId++;

        id2subtree[nodeId] = tree;

        function mkChildren(tree) {
            return [
                processTree(tree.fun, idPrefix),
                processTree(tree.arg, idPrefix)
            ];
        }

        var nodeStructure;

        if (tree.node === '@') {
            nodeStructure = {
                text : {name : '+'},
                HTMLclass : 'app',
                children : mkChildren(tree)
            };
        } else {
            nodeStructure = {
                HTMLclass: 'leaf',
                text :{name : tree.node /*,title: tree.type*/}
            };
        }

        nodeStructure.HTMLid = nodeId;

        return nodeStructure;
    }

    function addClicks() {

        _.forIn(id2subtree, function (subtree, id) {
            $('#'+id).click(function (e) {
                _.each(clickNodeListeners, function (callback) {
                    callback(subtree, e);
                });
            });
        });

    }


    var chart;

    var simple_chart_config = {
        chart: {
            container: "#"+config.treantContainerName
        },
        nodeStructure: processTree(loadTree(), 'node-')
    };

    chart = new Treant(simple_chart_config, function () {
        addClicks();
    });


    return {
        getChart : function () {return chart;},
        addClickNodeListener: addClickNodeListener
    };
}