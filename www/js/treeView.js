function mkTreeView($el, config) {

    var treeJson;

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
        treeJson = $textArea.text();
        console.log(treeJson);
    }

    loadTree();


    var chart;

    var simple_chart_config = {
        chart: {
            container: "#"+config.treantContainerName
        },

        nodeStructure: {
            text: { name: "Parent node" },
            children: [
                {
                    text: { name: "First child" }
                },
                {
                    text: { name: "Second child" }
                }
            ]
        }
    };

    chart = new Treant(simple_chart_config);

    return {
        getChart : function () {return chart;}
    };
}