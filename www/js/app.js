function mkApp($container) {

    var $treeView = $('<div>');
    $container.append($treeView);

    var treeView = mkTreeView($treeView, {
        treantContainerName: 'treeView-treant',
        height: 300
    });

    return {
        getChart : treeView.getChart
    };
}