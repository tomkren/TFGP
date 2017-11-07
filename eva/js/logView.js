function mkLogView($container, dispatch) {

    var KEY_log = 'log';
    var KEY_jobContainerId = 'jobContainerId';
    var KEY_numAllErrorLogLines = 'numAllErrorLogLines';
    var KEY_numAllLogLines = 'numAllLogLines';

    function render(logInfo, logOpts) {
        if (logInfo === null) {
            //$container.html("Error: Unable to load job's log!");

            renderEmpty();
            return;
        }


        var logLines = logInfo[KEY_log];
        var $log = $('<table>').addClass("blue-tab").append(
            $('<pre>').append(_.map(_.reverse(logLines), function(lineObj) {
                return lineObj2str(lineObj) + '\n';
            }))
        );

        $container.html([
            mkHeader(logInfo, logOpts),
            $log
        ]);
    }

    function mkHeader(logInfo, logOpts) {

        var logInfo_rest = _.clone(logInfo);
        delete logInfo_rest[KEY_log];

        var $offButton = $('<button>').text('OFF').click(function () {
            dispatch({type: 'SET_CURRENT_JOB_CONTAINER', jobContainerId: null});
        });

        return $('<div>').addClass("log-header").append([
            $('<b>').text("jobContainerId: "), logInfo[KEY_jobContainerId], '<br>',
            $('<b>').text("numAllLogLines: "), logInfo[KEY_numAllLogLines], '<br>',
            $('<b>').text("numErrors: "), logInfo[KEY_numAllErrorLogLines], '<br>',
            mkLogOptsBox(logOpts),
            $('<b>').text("logInfo: "), JSON.stringify(logInfo_rest), '<br>',
            $offButton
        ]);
    }

    function mkLogOptsBox(logOpts) {
        return $('<div>').append([
            $('<b>').text("logOpts: "), JSON.stringify(logOpts)
        ]);
    }

    function lineObj2str(lineObj) {
        if (_.isString(lineObj)) {
            return lineObj;
        } else {
            return JSON.stringify(lineObj, null, 2);
        }
    }

    function renderEmpty() {
        $container.html(
            $('<table>').addClass("blue-tab").append($('<tr>').append($('<td>')
                .text('Here will be shown log for the selected JobContainer.')))
        );
    }

    return {
        render: render,
        renderEmpty: renderEmpty
    };

}
