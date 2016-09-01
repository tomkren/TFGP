function mkApp($container) {

    var $treeView = $('<div>');
    $container.append($treeView);

    var treeView = mkTreeView($treeView, {
        treantContainerName: 'treeView-treant',
        height: 550
    });

    return {
        getChart : treeView.getChart
    };
}