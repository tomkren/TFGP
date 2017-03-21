function mkLogView($container, dispatch) {

    function render(logInfo) {
        if (logInfo === null) {
            $container.html("Error: Unable to load job's log!");
            return;
        }

        var logLines = logInfo.log;


        var $pre = $('<pre>');

        _.each(logLines, function (line) {
            $pre.append(line + '\n');
        });

        var $header = $('<div>').addClass("log-header").append([
            $('<b>').text("jobId: "),
            logInfo.jobId
        ]);
        var $log = $('<table>').addClass("blue-tab").append($pre);

        $container.html([$header, $log]);
    }

    function renderEmpty() {
        $container.html(
            $('<table>').addClass("blue-tab").append($('<tr>').append($('<td>')
                .text('No job selected.')))
        );
    }

    return {
        render: render,
        renderEmpty: renderEmpty
    };

}