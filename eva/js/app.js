function mkApp(config) {

    var pages = [
        {name: 'jobs', containerId:'page-jobs', selected: true}
    ];

    var jobViewRegistrars = {
        CellEva: cellplazaRegisterInApp,
        CellplazaJob: cellplazaRegisterInApp
    };

    var stateMan = mkStateManager(config, pages);

    var userView = null;
    var pageView = null;
    var serverInfoView = null;
    var jobsView = null;
    var jobViewer = null;
    var logView = null;


    var dispatch = stateMan.dispatch;


    stateMan.loadUserData(function (userData) {
        userView = mkUserView($('#userView'), dispatch);
        userView.render(userData);

        pageView = mkPageView($('#pageView'), dispatch);
        stateMan.addPageChangeListener(pageView.render);
        pageView.render(stateMan.getState('pages'), stateMan.getState('currentPageIndex'));


        stateMan.loadInitState(function(initState) {

            serverInfoView = mkServerInfoView($('#serverInfoView'));
            serverInfoView.render(initState);


        });



        jobsView = mkJobsView($('#jobsView'), dispatch);
        stateMan.addJobsListener(jobsView.render);
        stateMan.loadJobsAndInformListeners();

        jobViewer = mkJobViewer($('#jobViewer'), config, jobViewRegistrars);
        jobViewer.render();
        stateMan.addJobViewListener(jobViewer.showJobView);



        logView = mkLogView($('#logView'), dispatch);
        stateMan.addLogListener(logView.render);
        logView.renderEmpty();

        stateMan.periodicalCheck(stateMan.loadLogAndInformListeners,  $('#logViewLoader'),  config.logCheckingInterval, stateMan.isLogCheckPerformed);
        //stateMan.periodicalCheck(stateMan.loadJobsAndInformListeners, $('#jobsViewLoader'), config.jobsCheckingInterval);


    });



    return {
        dispatch: function(action) {dispatch(action);},
        getState : function (key) {return stateMan.getState(key);},
        getStateMan   : function () {return stateMan;},
        getJobsView   : function () {return jobsView;},
        getUsername: function () {return stateMan.getState('userData')['username'];}
    };
}
