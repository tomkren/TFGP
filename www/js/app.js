function mkApp(config) {

    var $treeView = $('<div>');
    config.$container.append($treeView);

    var treeView = mkTreeView($treeView);
    var popup = mkPopup(config.$popup);

    /*
    treeView.addClickNodeListener(function (subtree, e) {
        var type = Types.show(subtree.type);
        log(type);
        popup.show(e, subtree.typeInfo.getShort());
    });
    */

    return {
        getTypeInfo: treeView.getTypeInfo,
        treeView: treeView
    };
}