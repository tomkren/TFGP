function mkStateManager(config) {

    var state = {
        jobs: undefined,
        log: undefined,
        currentJobId: undefined
    };

    var jobsListeners = [];
    var logListeners = [];

    var apiUrl = config.apiUrl || 'http://localhost:2342';
    if (_.last(apiUrl) !== '/') {
        apiUrl = apiUrl + '/';
    }
    log(apiUrl);

    function dispatch(action) {
        if (action.cmd === 'run') {
            var json = JSON.stringify(action);
            var encodedJson = encodeURIComponent(json);

            //log(json);
            //log(encodedJson);

            $.get(apiUrl+'?'+encodedJson).done(function (result) {
                log(result);

                loadJobsAndInformListeners(function () {
                    if (state.currentJobId === undefined) {
                        log("currentJob auto-set...");
                        dispatch({
                            cmd: "setCurrentJob",
                            jobId: result.jobId
                        });
                    }
                });

            }).error(function () {
                log("ERROR !");
            });

        } else if (action.cmd == 'setCurrentJob') {
            state.currentJobId = action.jobId;

            loadLogAndInformListeners();

        }
    }

    function periodicalCheck(loadAndInformListeners, ajax, checkingInterval) {
        ajax.fadeIn("fast");
        loadAndInformListeners(function (info) {
            if (info !== null && info.status === 'ok') {

                ajax.fadeOut("fast");
                setTimeout(function(){
                    periodicalCheck(loadAndInformListeners, ajax, checkingInterval);
                }, checkingInterval);

            } else {

                var errMsg = (info === null ? "info = null" : "info.status = "+info.status);
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

    function loadLogAndInformListeners(doAfterLoad) {

        if (!_.isNumber(state.currentJobId)) {
            if (_.isFunction(doAfterLoad)) {
                doAfterLoad({status: "ok", msg: "no current job"});
            }
            return;
        }

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
        loadJobs: loadJobs,
        loadJobsAndInformListeners: loadJobsAndInformListeners,
        loadLogAndInformListeners: loadLogAndInformListeners,
        addJobsListener: addJobsListener,
        addLogListener: addLogListener,
        dispatch: dispatch,
        periodicalCheck: periodicalCheck,
        getState: function () {return state;}
    };
}
