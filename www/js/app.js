function mkApp(config) {

    var stateMan = mkStateManager(config);
    var dispatch = stateMan.dispatch;

    //var jobsView = null;
    /*stateMan.loadJobs(function (jobs) {
        jobsView = mkJobsView($('#jobsView'), dispatch);
        jobsView.render(jobs);
        stateMan.addJobsListener(jobsView.render);
    });*/

    var jobsView = mkJobsView($('#jobsView'), dispatch);


    stateMan.addJobsListener(jobsView.render);
    stateMan.loadJobsAndInformListeners();



    return {
        getStateMan: function () {return stateMan;}
    };
}