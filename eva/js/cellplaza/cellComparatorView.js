function mkCellComparatorView($container, dispatch) {

    var pathPrefix = Cellplaza.pathPrefix;

    function render(pairToCompareInfo) {

        var pair = pairToCompareInfo['pair'];

        function mkHeaderTR(pair) {
            var fs = pair[0]['frames'];
            return $('<tr>').append(_.map(fs, function(f,i){
                return $('<td>').text(i===0 ? '' : (i===1 ? 'seed' : 'step'+(i-1)));
            })).append('step'+(fs.length-1));
        }

        function mkIndivTR(indivData,i) {
            return $('<tr>').append('indiv'+pair[i]['id'],_.map(indivData['frames'], mkFrameTD(i)));
        }

        function mkFrameTD(indivIndex) {
            return function (framePath, i) {
                var $img = $('<img>').addClass('hand')
                    .attr("src",pathPrefix(framePath))
                    .click(function () {

                        $container.html($('<div>').addClass('comparatorAjaxHolder').html($('<img>').attr("src", "img/ajax2.gif").addClass('ajax2')));

                        var comparisonResult =  {
                            i1wins: indivIndex === 0,
                            ids: [pair[0]['id'], pair[1]['id']],
                            frame: framePath
                        };

                        var cmd = {
                            cmd: 'job',
                            jobCmd: 'offerResult',
                            result: comparisonResult
                        };

                        dispatch(cmd);
                    });

                return $('<td>').html($img);
            }
        }

        var $table = $('<table>').append(
            mkHeaderTR(pair),
            _.map(pair, mkIndivTR)
        );

        $container.html($table);
    }


    return {
        render:render
    };

}
