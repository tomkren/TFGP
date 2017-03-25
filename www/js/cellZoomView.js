function mkCellZoomView(config, $container, dispatch) {

    var $selectedContainer;
    var tilesDir;

    var $tileContainers;
    var tileIndices;

    var selectedWinner;
    var selectedTiles;

    var $zoomContainer;


    function render(history) {


        var clickWinners = history['clickWinners'];
        tilesDir =  history['tiles']['dir'];
        var tiles = history['tiles']['tiles'];

        $selectedContainer = mkContainer();
        $tileContainers = _.map(tiles, function () {return mkContainer();});

        selectedWinner = null;
        selectedTiles  = _.map(tiles, function () {return null;});

        if (tileIndices === undefined) {
            tileIndices = _.map(tiles, function () {return 0;});
        }

        var $tiles = mkTiles(tiles);
        var $thumbnails = mkThumbnails(clickWinners);

        if (!_.isEmpty(clickWinners)){
            select(_.last(clickWinners));
        }

        _.each(tiles, function (pack,iPack) {
            if (!_.isEmpty(pack)) {
                selectTile(pack[tileIndices[iPack]],iPack,tileIndices[iPack]);
            }
        });

        $zoomContainer = $('<div>').text('zoom container');
        //var $zoomButton = $('<button>').html('Zoom!').addClass('hand').click(zoom);

        $container.html('');
        $container.append([
            $('<table>').html($('<tr>').append([
                $('<td>').html($zoomContainer),
                $('<td>').append([$selectedContainer,'<br>'/*,$zoomButton*/]),
                $('<td>').html($tiles)
            ])),
            $thumbnails
            //$('<pre>').text(JSON.stringify(history, null, 2))
        ]);
    }

    function zoom() {

        if (selectedWinner === null || !_.isEmpty(_.filter(selectedTiles, function (x) {return x === null;}))) {
            return;
        }

        var action = {
            cmd: 'job',
            jobCmd: 'zoom',
            plaza : selectedWinner,
            tiles : selectedTiles
        };

        dispatch(action, updateZoom);
    }

    function updateZoom(zoomResultSrc) {
        log(zoomResultSrc);
        var $img = $('<img>').attr('src', '../'+zoomResultSrc);
        $zoomContainer.html($img);
    }


    function mkTiles(tiles) {
        var $tiles = $('<table>').addClass('tiles-tab');
        _.each(tiles, function (pack,i) {
            $tiles.append(mkTilesPack(pack,i));
        });
        return $tiles;
    }

    function mkTilesPack(pack,iPack) {
        var $tilesPack = $('<td>');
        for (var i = 0; i < pack.length; i++) {
            var $img = mkTile(pack[i],iPack, i);
            $tilesPack.append($img);
            if (i % config.numTilesOneRow == config.numTilesOneRow-1) {
                $tilesPack.append('<br>');
            }
        }
        return $('<tr>').append([$('<td>').html($tileContainers[iPack]), $tilesPack]);
    }

    function mkTile(tile, iPack, i) {
        return $('<img>').addClass('thumbnail').addClass('hand').attr('src', "../"+tilesDir +"/"+ tile['src']).click(function () {
            selectTile(tile, iPack, i);
        });
    }

    function selectTile(tile, iPack, i) {
        selectedTiles[iPack] = tilesDir +'/'+ tile['src'];
        tileIndices[iPack] = i;
        var src = "../"+ tilesDir +'/'+ tile['src'];
        var $img = $('<img>').attr('src',src).css("width", 100);
        $tileContainers[iPack].html($img);
        zoom();
    }



    function mkContainer() {
        return $('<div>');
    }

    function mkThumbnails(clickWinners) {
        var $thumbnails = $('<div>');
        for (var i = 0; i < clickWinners.length; i++) {
            var $img = mkThumbnail(clickWinners[i]);
            $thumbnails.append($img);

            if (i % config.numThumbnailsOneRow == config.numThumbnailsOneRow-1) {
                $thumbnails.append('<br>');
            }
        }
        return $thumbnails;
    }


    function select(clickWinner) {
        selectedWinner = clickWinner['frame1px'];
        var src = '../' + clickWinner['frame'];
        var $img = $('<img>').attr('src',src);
        $selectedContainer.html($img);
        zoom();
    }

    function mkThumbnail(clickWinner) {
        return $('<img>').addClass('thumbnail').addClass('hand').attr('src', '../' + clickWinner['frame1px']).click(function () {
            select(clickWinner);
        });
    }




    return {render:render};
}