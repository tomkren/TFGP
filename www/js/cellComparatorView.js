function mkCellComparatorView($container, dispatch) {

    var cellEvaJobId = null;
    var actualPairInfo = null;

    function render(jobsInfo) {

        if (actualPairInfo !== null) {return;}

        if (jobsInfo === null) {
            $container.html('Error: Unable to load jobs info!');
            return;
        }


        if (cellEvaJobId === null) {
            // takes the last job with 'CellEva' jobName
            _.each(jobsInfo['jobs'], function (job) {
                var jobName = job['jobOpts']['job'];
                if (jobName === 'CellEva') {
                    cellEvaJobId = job['jobId'];
                }
            });
        }

        if (cellEvaJobId === null) {return;}

        // todo zatim obcházime stateMan, ale ted neni cas na nehaxy!

        loadPair();
    }

    function loadNewPair() {
        setTimeout(loadPair,200);
    }

    function loadPair() {

        var stateMan = App.getStateMan();
        var apiUrl = stateMan.getApiUrl();
        $.get(apiUrl+'/job/'+cellEvaJobId+'/getPairToCompare').done(function (pairToCompareInfo) {

            log(pairToCompareInfo);

            if (pairToCompareInfo.status === 'initializing') {
                actualPairInfo = null;
                $container.html('Evolution is initializing <img src="img/ajax2.gif" class="ajax2">');
            } else {
                actualPairInfo = pairToCompareInfo;
                render_core(pairToCompareInfo);
            }

        }).error(stateMan.handleError);
    }

    function render_core(pairToCompareInfo) {
        if (pairToCompareInfo === null) {
            $container.html("Error: Unable to load CellComparator data!");
            return;
        }

        var pair = pairToCompareInfo['pair'];

        if (pair === null) {
            $container.html('Individuals are not yet prepared <img src="img/ajax2.gif" class="ajax2">');
            actualPairInfo = null;
            return;
        }

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
                    .attr("src","../"+framePath)
                    .click(function () {

                        var comparisonResult =  {
                            i1wins: indivIndex === 0,
                            frame: i,
                            ids: [pair[0]['id'], pair[1]['id']],
                            frame: framePath
                        };

                        var cmd = {
                            cmd: 'job',
                            jobId: cellEvaJobId,
                            jobCmd: 'offerResult',
                            result: comparisonResult
                        };

                        log(JSON.stringify(cmd));
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
        render:render,
        loadNewPair: loadNewPair
    };

}