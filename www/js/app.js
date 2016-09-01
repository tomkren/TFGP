function mkApp($container) {

    var $treeView = $('<div>');
    $container.append($treeView);

    var treeView = mkTreeView($treeView);
    var popup = mkPopup($('#popup'));

    treeView.addClickNodeListener(function (subtree, e) {
        log(subtree.type);
        popup.show(e, subtree.type);
    });

    return {
        getChart : treeView.getChart
    };
}