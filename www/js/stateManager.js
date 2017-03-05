function mkStateManager(config) {

    var state = {
        jobs: undefined
    };

    var apiUrl = config.apiUrl || 'http://localhost:2342';
    if (_.last(apiUrl) !== '/') {
        apiUrl = apiUrl + '/';
    }
    log(apiUrl);

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
        loadJobs        : loadJobs,
        getState        : function () {return state;},
        getJobs         : function () {return state.jobs;}
    };
}
