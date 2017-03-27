function mkApp(config) {

    var stateMan = mkStateManager(config);
    var dispatch = stateMan.dispatch;


    var jobsView = mkJobsView($('#jobsView'), dispatch);
    stateMan.addJobsListener(jobsView.render);

    var logView = mkLogView($('#logView'), dispatch);
    stateMan.addLogListener(logView.render);
    logView.renderEmpty();


    stateMan.addJobsListener(stateMan.findCellEvaJobId);

    var cellComparatorView1 = mkCellComparatorView($('#cellComparatorView1'), dispatch);
    stateMan.addPairListener(cellComparatorView1.render);

    var cellZoomView = mkCellZoomView(config, $('#cellZoomView'), dispatch);
    stateMan.addHistoryListener(cellZoomView.render);

    stateMan.periodicalCheck(stateMan.loadJobsAndInformListeners,    $('#jobsViewLoader'),             config.jobsCheckingInterval);
    stateMan.periodicalCheck(stateMan.loadLogAndInformListeners,     $('#logViewLoader'),              config.logCheckingInterval,     stateMan.isLogCheckPerformed);
    stateMan.periodicalCheck(stateMan.loadPairAndInformListeners,    $('#cellComparatorViewLoader1'),  config.pairCheckingInterval,    stateMan.isPairCheckPerformed);
    stateMan.periodicalCheck(stateMan.loadHistoryAndInformListeners, $('#cellZoomViewLoader'),         config.historyCheckingInterval, stateMan.isHistoryCheckPerformed);

    return {
        getStateMan: function () {return stateMan;}
    };
}