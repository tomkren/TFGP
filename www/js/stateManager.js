function mkStateManager(config) {

    var state = {
        jobs: undefined
    };

    var jobsListeners = [];

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


                loadJobsAndInformListeners();

            }).error(function () {
                log("ERROR !");
            });

        }
    }

    function addJobsListener(callback) {
        jobsListeners.push(callback);
    }

    function loadJobsAndInformListeners() {
        loadJobs(function (jobs) {
            _.each(jobsListeners, function (callback) {
                callback(jobs);
            });
        });
    }

    function mkLoadStateFun(apiCmdStr, stateKey, projection) {
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

    var loadJobs = mkLoadStateFun('jobs', 'jobs', function (r) {return r.jobs;});

    return {
        loadJobs: loadJobs,
        loadJobsAndInformListeners: loadJobsAndInformListeners,
        addJobsListener: addJobsListener,
        dispatch: dispatch,
        getState: function () {return state;}
    };
}
