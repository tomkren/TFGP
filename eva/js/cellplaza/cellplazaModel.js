function cellplazaRegisterInApp($container, config, jobContainerId) {

    var modelMan = mkModelManager(config, jobContainerId, mkCellplazaMakers());
    var dispatch = modelMan.dispatch;

    var cellplazaView = mkCellplazaView($container);
    cellplazaView.render();

    var cellComparatorView = mkCellComparatorView(cellplazaView.getCellComparatorContainer(), dispatch);
    var pairLoader = modelMan.loaders['pair'];
    pairLoader.addListener(cellComparatorView.render);
    var ajax1 = false; // todo
    modelMan.utils.periodicalCheck(pairLoader, ajax1);


    var cellZoomView = mkCellZoomView(config, cellplazaView.getCellZoomContainer(), dispatch);
    var historyLoader = modelMan.loaders['history'];
    historyLoader.addListener(cellZoomView.render);
    var ajax2 = false; // todo
    modelMan.utils.periodicalCheck(historyLoader, ajax2);

    return {
        model: modelMan.getState(),
        view: cellplazaView
    };
}

function mkCellplazaMakers() {

    function mkCellplazaModel() {

        return {
            isPairNeeded: true,
            lastPairIds: [undefined,undefined],
            lastHistoryVersion: undefined,

            _pairToCompare: undefined,
            _historyVersion: undefined,
            _history: undefined
        };
    }


    function mkCellplazaDispatch(state, jobContainerId, utils) {

        var KEY_jobContainerId = 'jobContainerId';

        function dispatch(action, haxCallback) {
            if (action.cmd === 'job') {

                action[KEY_jobContainerId] = jobContainerId;
                console.log(JSON.stringify(action));

                if (action.jobCmd === 'offerResult') {

                    utils.sendApiAction(action).done(function (response) {
                        console.log(response);
                        state.isPairNeeded = true;

                    }).error(utils.handleError);

                } else if (action.jobCmd === 'zoom') {

                    utils.sendApiAction(action).done(function (response) {

                        haxCallback(response.result);

                    }).error(utils.handleError);
                }
            }
        }

        return dispatch;
    }

    function mkCellplazaLoaders(state, jobContainerId, utils) {
        return {
            pair: mkCellplazaPairStuff(state, jobContainerId, utils),
            history: mkCellplazaHistoryStuff(state, jobContainerId, utils)
        };
    }


    function mkCellplazaHistoryStuff(state, jobContainerId, utils) {

        var historyListeners = [];

        function addHistoryListener(callback) {
            historyListeners.push(callback);
        }

        function isHistoryCheckPerformed() {
            return state.isActive;
        }

        function loadHistoryAndInformListeners(doAfterLoad) {
            var loadFunction = utils.mkLoadStateFun('job/'+jobContainerId+'/historyVersion', '_historyVersion');
            loadFunction(function (historyVersionInfo) {

                if (historyVersionInfo !== null && historyVersionInfo.status === 'ok' && historyVersionInfo.version !== state.lastHistoryVersion) {

                    var loadFunction2 = utils.mkLoadStateFun('job/'+jobContainerId+'/history', '_history');
                    loadFunction2(function (history) {

                        if (history !== null && history.status === 'ok') {

                            state.lastHistoryVersion = historyVersionInfo.version;

                            _.each(historyListeners, function (callback) {
                                callback(history);
                            });

                            if (_.isFunction(doAfterLoad)) {doAfterLoad(historyVersionInfo);}

                        }

                    });

                } else {

                    if (_.isFunction(doAfterLoad)) {doAfterLoad(historyVersionInfo);}

                }

            });
        }

        return {
            addListener: addHistoryListener,
            isCheckPerformed: isHistoryCheckPerformed,
            loadAndInform: loadHistoryAndInformListeners,
            checkingInterval: 1000
        };
    }

    function mkCellplazaPairStuff(state, jobContainerId, utils) {

        var pairListeners = [];

        function addPairListener(callback) {
            pairListeners.push(callback);
        }

        function isPairCheckPerformed() {
            return state.isActive && state.isPairNeeded;
        }

        function loadPairAndInformListeners(doAfterLoad) {
            var loadFunction = utils.mkLoadStateFun('job/'+jobContainerId+'/getPairToCompare', '_pairToCompare');
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

        return {
            addListener: addPairListener,
            isCheckPerformed: isPairCheckPerformed,
            loadAndInform: loadPairAndInformListeners,
            checkingInterval: 1000
        };
    }

    return {
        mkModel: mkCellplazaModel,
        mkDispatch: mkCellplazaDispatch,
        mkLoaders: mkCellplazaLoaders
    };
}