function mkStateManager(config, pages) {

    var KEY_jobContainerId = 'jobContainerId';
    var KEY_jobClass = 'jobClass';
    var CMD_job = 'job';
    var JCMD_stop = 'stop';
    var JCMD_restart = 'restart';
    var KEY_cmd = 'cmd';
    var KEY_jobCmd = 'jobCmd';


    var pageChangeListeners = [];
    var logListeners = [];
    var setInfoListeners = [];
    var jobsListeners = [];
    var jobViewListeners = [];

    var state = {
        pages: pages,
        currentPageIndex: findCurrentPageIndex(pages),
        jobs: undefined,
        log: undefined,
        logOpts: {
            currentJobContainerId: null,
            logMode: 'log',
            numLast: 300,
            reverse: true,
            prettify: true
        },
        initState: undefined,
        userData: undefined
    };

    var apiUrl = config.apiUrl || 'http://localhost:4224';
    if (_.last(apiUrl) !== '/') {
        apiUrl = apiUrl + '/';
    }
    log(apiUrl);


    function dispatch(action) {

        console.log(action);
        var jobContainerId; // to avoid multiple declarations, no hax

        if (action.type === 'LOAD_JOBS') {

            loadJobsAndInformListeners();

        } else if (action.type === 'SHOW_JOB_VIEW') {

            jobContainerId = action[KEY_jobContainerId];
            var jobClass = action[KEY_jobClass];

            _.each(jobViewListeners, function (listener) {
                listener(jobContainerId, jobClass);
            });


        } else if (action.type === 'STOP_JOB_CONTAINER') {

            jobContainerId = action[KEY_jobContainerId];

            generalApiCall(mkJobCmdQuery(jobContainerId, JCMD_stop), function (result) {
                log(result);
                loadJobsAndInformListeners();
            });


        } else if (action.type === 'RESTART_JOB_CONTAINER') {

            jobContainerId = action[KEY_jobContainerId];

            generalApiCall(mkJobCmdQuery(jobContainerId, JCMD_restart), function (result) {
                log(result);
                loadJobsAndInformListeners();
            });

        } else if (action.cmd === 'run') {

            generalApiCall(action, function (result) {
               log(result);
               loadJobsAndInformListeners();
            });

        } else if (action.cmd === 'addJobConfig') {

            generalApiCall(action, function (result) {
                log(result);
                loadJobsAndInformListeners();
            });


        } else if (action.cmd === 'updateJobConfig') {

            generalApiCall(action, function (result) {
                log(result);
                loadJobsAndInformListeners();
            });

        } else if (action.type === 'SET_CURRENT_JOB_CONTAINER') {

            state.logOpts.currentJobContainerId = action[KEY_jobContainerId];
            state.logOpts.logMode = action.logMode;


            loadLogAndInformListeners();


        } else if (action.type === 'CHANGE_USER') {

            changeUser(action.username); // až tam bude víc věcí tak loadovat ze servru ..

        } else if (action.type === 'SELECT_PAGE') {


            selectPage(action.name);
            _.each(pageChangeListeners, function (listener) {
                listener(state.pages, state.currentPageIndex);
            });


        } else if (action.type === undefined && action.cmd !== undefined) {
            // straight to server api...
            log('BOND HERE! <3 General Api Call was performed!');
            //log(action);

            generalApiCall(action, function (result) {
                log("result:");
                log(result);
            });
        } else {
            log('!!! UNHANDLED ACTION: '+JSON.stringify(action));
        }
    }


    function mkJobCmdQuery(jobContainerId, jobCmd, query) {
        if (query === undefined) {query = {};}
        query[KEY_cmd] = CMD_job;
        query[KEY_jobContainerId] = jobContainerId;
        query[KEY_jobCmd] = jobCmd;
        return query;
    }


    function generalApiCall(query, callback, errCallback) {
        var cmdUrl = apiUrl+'?'+ encodeURIComponent(JSON.stringify(query));
        return $.get(cmdUrl).done(function (result) {
            if (_.isFunction(callback)) {
                callback(result);
            }
        }).error(function () {
            log("ERROR in response to "+cmdUrl+" !");
            if (_.isFunction(errCallback)) {
                errCallback();
            } else {
                if (_.isFunction(callback)) {
                    callback(null);
                }
            }
        });
    }


    function addPageChangeListener(listener)  {pageChangeListeners.push(listener);}
    function addLogListener(listener)         {logListeners.push(listener);}
    function addSetInfoListener(listener)     {setInfoListeners.push(listener);}
    function addJobsListener(listener)        {jobsListeners.push(listener);}
    function addJobViewListener(listener)     {jobViewListeners.push(listener);}


    function mkLoadStateFun(apiCmdStr, stateKey, projection) {
        if (projection === undefined) {
            projection = function (x) {return x;};
        }
        return function (callback, errCallback) {
            if (callback === undefined) {
                callback = function () {};
            }
            return $.get(apiUrl+apiCmdStr).done(function (result) {
                state[stateKey] = projection(result);
                callback(state[stateKey]);
            }).error(function () {
                state[stateKey] = null;
                log("ERROR loading "+stateKey+" !");
                if (errCallback === undefined) {
                    callback(null);
                } else {
                    errCallback();
                }
            });
        }
    }


    function periodicalCheck(loadAndInformListeners, ajax, checkingInterval, isCheckPerformedFun) {

        if (_.isFunction(isCheckPerformedFun)) {
            if (!isCheckPerformedFun()) {
                setTimeout(function(){
                    periodicalCheck(loadAndInformListeners, ajax, checkingInterval, isCheckPerformedFun);
                }, checkingInterval);
                return;
            }
        }

        ajax.fadeIn("fast");
        loadAndInformListeners(function (info) {
            if (info !== null && (info.status === 'ok' || info.status === 'initializing')) {

                ajax.fadeOut("fast");
                setTimeout(function(){
                    periodicalCheck(loadAndInformListeners, ajax, checkingInterval,isCheckPerformedFun);
                }, checkingInterval);

            } else {

                var errMsg = (info === null ? "server is probably turned off" : "info.status = "+info.status);
                ajax.text("[error: "+errMsg+"]").addClass("red");
                console.error(errMsg);

            }
        });
    }

    function isLogCheckPerformed() {
        return _.isNumber(state.logOpts.currentJobContainerId);
    }

    function loadLogAndInformListeners(doAfterLoad) {

        function informAndStuff(logInfo) {
            _.each(logListeners, function (callback) {
                callback(logInfo, state.logOpts);
            });
            if (_.isFunction(doAfterLoad)) {
                doAfterLoad(logInfo);
            }
        }

        if (_.isNumber(state.logOpts.currentJobContainerId)) {

            var jobContainerId = state.logOpts.currentJobContainerId;
            var jobCmd = state.logOpts.logMode;
            var numLast = state.logOpts.numLast;

            var loadFunction = mkLoadStateFun('job/'+jobContainerId+'/'+jobCmd+'/'+numLast, 'log'); // TODO stateKey by se mel menovat spiš logInfo
            loadFunction(informAndStuff);

        } else {
            informAndStuff(null);
        }

    }

    function loadJobsAndInformListeners(doAfterLoad) {
        loadJobs(function (jobsInfo) {
            _.each(jobsListeners, function (callback) {
                callback(jobsInfo);
            });
            if (_.isFunction(doAfterLoad)) {
                doAfterLoad(jobsInfo);
            }
        });
    }





    function findCurrentPageIndex(pages) {
        for (var i = 0; i < pages.length; i++) {
            if (pages[i]['selected']) {return i;}
        }
        pages[0]['selected'] = true;
        return 0;
    }

    function selectPage(name) {
        for (var i = 0; i < pages.length; i++) {
            if (pages[i]['name'] === name) {
                delete pages[state.currentPageIndex]['selected'];// = undefined;
                pages[i]['selected'] = true;
                state.currentPageIndex = i;
                return;
            }
        }
    }


    function mkUserData(username) {
        return {username:username};
    }

    function changeUser(username) {
        state.userData = mkUserData(username);
        localStorage.setItem('username', username);
    }

    function loadUserData(callback, errorCallback) {
        var username = localStorage.getItem('username');
        if (username === null) {username = 'anonymous';}
        state.userData = mkUserData(username);
        callback(state.userData);
    }

    var loadInitState = mkLoadStateFun('getInitState', 'initState');
    var loadJobs = mkLoadStateFun('jobs', 'jobs', function (r) {return r/*[KEY_jobConfigs]*/;});





    function getState(key) {
        if (key === undefined) {
            return state;
        }
        return state[key];
    }

    return {
        dispatch        : dispatch,

        periodicalCheck: periodicalCheck,
        isLogCheckPerformed: isLogCheckPerformed,

        loadLogAndInformListeners: loadLogAndInformListeners,
        loadJobsAndInformListeners: loadJobsAndInformListeners,

        addJobsListener: addJobsListener,
        addJobViewListener: addJobViewListener,
        addPageChangeListener: addPageChangeListener,
        addLogListener: addLogListener,
        addSetInfoListener: addSetInfoListener,

        generalApiCall: generalApiCall,

        loadUserData    : loadUserData,
        loadInitState   : loadInitState,
        loadJobs        : loadJobs,
        getState        : getState,
        getJobs         : function () {return state.jobs;}
    };
}
