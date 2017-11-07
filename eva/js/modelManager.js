function mkModelManager(config, jobContainerId, makers) {

    var apiUrl = config.apiUrl;
    if (_.last(apiUrl) !== '/') {apiUrl = apiUrl + '/';}

    var state = makers.mkModel();
    state.isActive = true;

    function sendApiAction(action) {
        var encodedAction = encodeURIComponent(JSON.stringify(action));
        return $.get(apiUrl+'?'+encodedAction);
    }

    function handleError(e) {
        console.error('ERROR!!!! : '+JSON.stringify(e));
    }

    function mkLoadStateFun(apiCmdStr, stateKey, projection) {
        if (projection === undefined) {
            projection = function (x) {return x;};
        }

        return function (callback, errCallback) {
            if (callback === undefined) {
                callback = function () {};
            }

            var cmdUrl = apiUrl+apiCmdStr;
            return $.get(cmdUrl).done(function (result) {
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

    function periodicalCheck(loader, ajax) {

        var loadAndInformListeners = loader.loadAndInform;
        var isCheckPerformedFun = loader.isCheckPerformed;
        var checkingInterval = loader.checkingInterval;


        if (_.isFunction(isCheckPerformedFun)) {
            if (!isCheckPerformedFun()) {
                setTimeout(function(){
                    periodicalCheck(loader, ajax);
                }, checkingInterval);
                return;
            }
        }

        if (ajax) {ajax.fadeIn("fast");}

        loadAndInformListeners(function (info) {

            if (info !== null && (info.status === 'ok' || info.status === 'initializing')) {

                if (ajax) {ajax.fadeOut("fast");}

                setTimeout(function(){
                    periodicalCheck(loader, ajax);
                }, checkingInterval);

            } else {

                var errMsg = (info === null ? "server is probably turned off" : "info.status = "+info.status);

                if (ajax) {ajax.text("[error: "+errMsg+"]").addClass("red");}

                console.error(errMsg);
            }
        });
    }

    var utils = {
        sendApiAction: sendApiAction,
        handleError: handleError,
        mkLoadStateFun: mkLoadStateFun,
        periodicalCheck: periodicalCheck
    };

    var dispatch = makers.mkDispatch(state, jobContainerId, utils);
    var loaders = makers.mkLoaders(state, jobContainerId, utils);

    return {
        dispatch: dispatch,
        loaders: loaders,
        utils: utils,
        getState: function () {return state;}
    };
}
