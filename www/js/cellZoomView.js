function mkCellZoomView(config, $container, dispatch) {

    var $selectedContainer;

    function render(history) {


        var clickWinners = history['clickWinners'];

        $selectedContainer = mkSelectedContainer();
        var $thumbnails = mkThumbnails(clickWinners);

        select(_.last(clickWinners));

        $container.html('');
        $container.append([
            $selectedContainer,
            $thumbnails,
            $('<pre>').text(JSON.stringify(history, null, 2))
        ]);


    }

    function mkSelectedContainer() {
        var $selected = $('<div>');
        return $selected;
    }

    function mkThumbnails(clickWinners) {
        var $thumbnails = $('<div>');

        for (var i = 0; i < clickWinners.length; i++) {
            var $img = mkThumbnail(clickWinners[i]);
            $thumbnails.append($img);

            if (i % config.numThumbnailsOnLine == config.numThumbnailsOnLine-1) {
                $thumbnails.append('<br>');
            }
        }
        return $thumbnails;
    }


    function select(clickWinner) {
        var src = '../' + clickWinner['frame'];
        var $img = $('<img>').attr('src',src);
        $selectedContainer.html($img);
    }

    function mkThumbnail(clickWinner) {
        return $('<img>').addClass('thumbnail').addClass('hand').attr('src', '../' + clickWinner['frame1px']).click(function () {
            select(clickWinner);
        });
    }





    return {render:render};
}