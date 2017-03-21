function mkApp(config) {

    var stateMan = mkStateManager(config);
    var dispatch = stateMan.dispatch;

    var jobsView = mkJobsView($('#jobsView'), dispatch);
    stateMan.addJobsListener(jobsView.render);

    var logView = mkLogView($('#logView'), dispatch);
    stateMan.addLogListener(logView.render);
    logView.renderEmpty();


    stateMan.periodicalCheck(stateMan.loadJobsAndInformListeners, $('#jobsViewLoader'), config.jobsCheckingInterval);
    stateMan.periodicalCheck(stateMan.loadLogAndInformListeners,  $('#logViewLoader'),  config.logCheckingInterval, stateMan.isLogCheckPerformed);


    return {
        getStateMan: function () {return stateMan;}
    };
}