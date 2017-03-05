function mkApp(config) {

    var stateMan = mkStateManager(config);
    var jobsView = null;

    stateMan.loadJobs(function (jobs) {
        jobsView = mkJobsView($('#jobsView'));
        jobsView.render(jobs);
    });


    return {
        getStateMan: function () {return stateMan;}
    };
}