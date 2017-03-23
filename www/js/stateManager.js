function mkStateManager(config) {

    var state = {
        jobs: undefined,
        log: undefined,
        currentJobId: undefined,

        cellEvaJobId: null,
        isPairNeeded: true,
        lastPairIds: [undefined,undefined],

        lastHistoryVersion: undefined
    };

    var jobsListeners = [];
    var logListeners = [];
    var pairListeners = [];
    var historyListeners = [];

    var apiUrl = config.apiUrl || 'http://localhost:2342';
    if (_.last(apiUrl) !== '/') {
        apiUrl = apiUrl + '/';
    }
    log(apiUrl);

    function dispatch(action) {
        if (action.cmd === 'run') {

            sendApiAction(action).done(function (result) {
                log(result);
                loadJobsAndInformListeners(function () {
                    if (config.autoSelectCurrentJob && result.status === "ok") {
                        log("currentJob auto-set to "+result.jobId+"...");
                        dispatch({
                            cmd: "setCurrentJob",
                            jobId: result.jobId
                        });
                    }
                });
            }).error(handleError);


        } else if (action.cmd === 'setCurrentJob') {
            state.currentJobId = action.jobId;

            loadLogAndInformListeners();

        } else if (action.cmd === 'job') {
            if (action.jobCmd === 'offerResult') {

                action.jobId = state.cellEvaJobId;
                log(JSON.stringify(action));

                sendApiAction(action).done(function (response) {
                    log(response);
                    //App.getCellComparatorView().loadNewPair();
                    state.isPairNeeded = true;

                }).error(handleError);

            } else {
                log("Unknown jobCmd '"+action.jobCmd+"' in action: "+JSON.stringify(action));
            }

        } else {
            log("Unknown cmd '"+action.cmd+"' in action: "+JSON.stringify(action));
        }
    }

    function encodeAction(action) {
        var json = JSON.stringify(action);
        return encodeURIComponent(json);
    }

    function sendApiAction(action) {
        var encodedAction = encodeAction(action);
        return $.get(apiUrl+'?'+encodedAction);
    }

    function handleError(e) {
        log('ERROR!!!! : '+e);
    }


    function findCellEvaJobId(jobsInfo) {
        if (jobsInfo === null) {return;}
        if (state.cellEvaJobId === null) {
            // takes the last job with 'CellEva' jobName
            _.each(jobsInfo['jobs'], function (job) {
                var jobName = job['jobOpts']['job'];
                if (jobName === 'CellEva') {
                    state.cellEvaJobId = job['jobId'];
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

    function addJobsListener(callback) {
        jobsListeners.push(callback);
    }

    function addLogListener(callback) {
        logListeners.push(callback);
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

    function isLogCheckPerformed() {
        return _.isNumber(state.currentJobId);
    }



    function addHistoryListener(callback) {
        historyListeners.push(callback);
    }

    function isHistoryCheckPerformed() {
        return state.cellEvaJobId !== null;
    }

    function loadHistoryAndInformListeners(doAfterLoad) {

        var loadFunction = mkLoadStateFun('job/'+state.cellEvaJobId+'/historyVersion');
        loadFunction(function (historyVersionInfo) {

            if (historyVersionInfo !== null && historyVersionInfo.status === 'ok' && historyVersionInfo.version !== state.lastHistoryVersion) {

                var loadFunction2 = mkLoadStateFun('job/'+state.cellEvaJobId+'/history');
                loadFunction2(function (history) {

                    if (history !== null && history.status === 'ok') {

                        state.lastHistoryVersion = historyVersionInfo.version;

                        _.each(historyListeners, function (callback) {
                            callback(history);
                        });

                    }

                });
            }

            if (_.isFunction(doAfterLoad)) {
                doAfterLoad(historyVersionInfo);
            }
        });
    }





    function addPairListener(callback) {
        pairListeners.push(callback);
    }

    function isPairCheckPerformed() {
        return state.cellEvaJobId !== null && state.isPairNeeded;
    }

    function loadLogAndInformListeners(doAfterLoad) {

        var loadFunction = mkLoadStateFun('log/'+state.currentJobId, 'log');

        loadFunction(function (logInfo) {
            _.each(logListeners, function (callback) {
                callback(logInfo);
            });
            if (_.isFunction(doAfterLoad)) {
                doAfterLoad(logInfo);
            }
        });
    }

    function loadPairAndInformListeners(doAfterLoad) {

        var loadFunction = mkLoadStateFun('job/'+state.cellEvaJobId+'/getPairToCompare');

        loadFunction(function (pairToCompareInfo) {

            if (pairToCompareInfo !== null && pairToCompareInfo.status === 'ok') {
                var pair = pairToCompareInfo['pair'];
                if (pair !== null && !(pair[0].id === state.lastPairIds[0] && pair[1].id === state.lastPairIds[1])) {

                    state.lastPairIds = [pair[0].id, pair[1].id];
                    state.isPairNeeded = false;

                    _.each(pairListeners, function (callback) {
                        callback(pairToCompareInfo);
                    });
                }
            }

            if (_.isFunction(doAfterLoad)) {
                doAfterLoad(pairToCompareInfo);
            }


        });

    }


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

    var loadJobs = mkLoadStateFun('jobs', 'jobs');

    return {
        dispatch: dispatch,

        addPairListener: addPairListener,
        addJobsListener: addJobsListener,
        addLogListener: addLogListener,
        addHistoryListener: addHistoryListener,

        loadJobsAndInformListeners: loadJobsAndInformListeners,
        loadLogAndInformListeners: loadLogAndInformListeners,
        loadPairAndInformListeners: loadPairAndInformListeners,
        loadHistoryAndInformListeners: loadHistoryAndInformListeners,

        isLogCheckPerformed: isLogCheckPerformed,
        isPairCheckPerformed: isPairCheckPerformed,
        isHistoryCheckPerformed: isHistoryCheckPerformed,

        findCellEvaJobId: findCellEvaJobId,

        periodicalCheck: periodicalCheck,
        getState: function () {return state;},
        handleError: handleError
    };
}
