function mkTreeView($el, config) {

    //var treeJson;

    config.treantContainerName = config.treantContainerName || 'treeView-treant';

    config.height = config.height || 300;

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

    function processTree(tree) {

        function mkChildren(tree) {
            return [
                processTree(tree.fun),
                processTree(tree.arg)
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
                text :{
                    name : tree.node
                    /*title: tree.type*/
                }
            };
         }

        return nodeStructure;
    }




    var chart;

    var simple_chart_config = {
        chart: {
            container: "#"+config.treantContainerName
        },

        nodeStructure: processTree(loadTree())
    };

    chart = new Treant(simple_chart_config);

    return {
        getChart : function () {return chart;}
    };
}