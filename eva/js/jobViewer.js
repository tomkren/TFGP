function mkJobViewer($container, config, jobViewRegistrars) {

    var KEY_isActive = 'isActive';

    var $subContainer = null;
    var $msgContainer = null;
    var currentJobContainerId = null;

    var jobViews = {};
    var jobModels = {};
    var jobContainers = {};

    function render() {

        $msgContainer = $('<div>');
        $subContainer = $('<div>');

        $container.html('').append([
            '<h2>JobView</h2>',
            $msgContainer,
            $subContainer
        ]);

        renderMsgInTable('Here will be shown view for the selected JobContainer.');
    }

    function showJobView(jobContainerId, jobClass) {

        if (currentJobContainerId !== null && currentJobContainerId !== jobContainerId) {
            jobModels[currentJobContainerId][KEY_isActive] = false;
            jobContainers[currentJobContainerId].hide();
            currentJobContainerId = null;
        }

        if (_.has(jobViewRegistrars, jobClass)) {

            renderMsgInTable('#'+jobContainerId);

            var model;


            if (_.has(jobViews, jobContainerId)) {

                model = jobModels[jobContainerId];
                jobContainers[jobContainerId].show();

            } else {

                var $jobContainer = $('<div>').addClass('jobViewContainer');
                $subContainer.append($jobContainer);

                jobContainers[jobContainerId] = $jobContainer;

                var registerInApp = jobViewRegistrars[jobClass];
                var registrationResult = registerInApp($jobContainer, config, jobContainerId);

                model = registrationResult.model;
                var view = registrationResult.view;

                jobModels[jobContainerId] = model;
                jobViews[jobContainerId] = view;
            }

            model[KEY_isActive] = true;
            currentJobContainerId = jobContainerId;

        } else {

            var msg = "JobContainer #"+jobContainerId+": No job view available for jobClass '"+jobClass+"'.";
            renderMsgInTable(msg);
            console.log(msg);
        }
    }

    function renderMsgInTable(msg) {
        $msgContainer.html(
            $('<table>').addClass("blue-tab").append($('<tr>').append($('<td>')
                .text(msg)))
        );
    }

    return {
        render: render,
        showJobView: showJobView
    };

}