function mkApp(config) {

    var stateMan = mkStateManager(config);
    var dispatch = stateMan.dispatch;


    stateMan.addJobsListener(stateMan.findCellEvaJobId);

    var jobsView = mkJobsView($('#jobsView'), dispatch);
    stateMan.addJobsListener(jobsView.render);

    var logView = mkLogView($('#logView'), dispatch);
    stateMan.addLogListener(logView.render);
    logView.renderEmpty();

    var cellComparatorView = mkCellComparatorView($('#cellComparatorView'), dispatch);
    //stateMan.addJobsListener(cellComparatorView.render);
    stateMan.addPairListener(cellComparatorView.render);

    stateMan.periodicalCheck(stateMan.loadJobsAndInformListeners, $('#jobsViewLoader'), config.jobsCheckingInterval);
    stateMan.periodicalCheck(stateMan.loadLogAndInformListeners,  $('#logViewLoader'),  config.logCheckingInterval, stateMan.isLogCheckPerformed);

    stateMan.periodicalCheck(stateMan.loadPairAndInformListeners,  $('#cellComparatorViewLoader'),  config.pairCheckingInterval, stateMan.isPairCheckPerformed);


    return {
        getStateMan: function () {return stateMan;},
        getCellEvaJobId: function () {return stateMan.getCellEvaJobId();}
    };
}