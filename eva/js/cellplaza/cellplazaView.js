var Cellplaza = {
    pathPrefix: function(path) {
        return path.substring(4);
    }
};

function mkCellplazaView($container) {

    var $cellComparatorContainer = null;
    var $cellZoomContainer = null;

    function render() {

        $cellComparatorContainer = $('<div>');
        $cellZoomContainer = $('<div>');

        $container.html('').append([
            '<h3>CellplazaView</h3>',
            $cellComparatorContainer,
            $cellZoomContainer
        ]);

    }

    return {
        render: render,
        getCellComparatorContainer: function () {return $cellComparatorContainer;},
        getCellZoomContainer: function () {return $cellZoomContainer;}
    };

}