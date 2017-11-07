function mkJobsView($container, dispatch) {

    var KEY_jobConfigs = "jobConfigs";

    var KEY__id = '_id';
    var KEY_jobName = 'jobName';
    var KEY_jobNames = 'jobNames';
    var KEY_opts = 'opts';
    var KEY_delay = "delay";
    var KEY_period = "period";
    var KEY_scheduleOnStartup = "scheduleOnStartup";
    var KEY_jobConfig = 'jobConfig';
    var ALIAS_jobClass = 'jobClass';

    var MODE_log = 'log';
    var MODE_errorLog = 'errorLog';

    var jobConfigCols = [
        {key: KEY__id},
        {key: KEY_jobName, editable:true, dropdown: undefined, alias: ALIAS_jobClass},
        {key: KEY_opts, editable:true, object: true},
        {key: KEY_delay, editable:true, width: 50, int: true},
        {key: KEY_period, editable:true, width: 50, int: true},
        {key: KEY_scheduleOnStartup, editable:true, checkBox: true, alias:'autoStart'},
        {buttons: true}
    ];


    var addJobConfigCols = _.cloneDeep(jobConfigCols);
    _.assign(addJobConfigCols[0], {editable:true, width:125, placeholder: 'New unique '+KEY__id+'.'});
    _.assign(addJobConfigCols[1], {placeholder: 'New '+ALIAS_jobClass+'.'});
    addJobConfigCols[addJobConfigCols.length-1] = {addButton: true};

    var defaultAddRow = {_prefix:'addJobConfig', jobName:null, opts:{}, delay:0, period:null, scheduleOnStartup:false};

    // ------------------------------------------------------------

    var KEY_jobContainers = 'jobContainers';

    var KEY_jobContainerId = 'jobContainerId';
    var KEY_jobStatus = 'jobStatus';

    var jobContainerCols = [
        {key: KEY_jobContainerId, alias:'jc_id'},
        {key: [KEY_jobConfig, KEY__id], alias:'_id'},
        {key: [KEY_jobConfig, KEY_jobName], alias: ALIAS_jobClass},
        {key: [KEY_jobConfig, KEY_opts], alias: 'opts'},
        {key: KEY_jobStatus},
        {showLogButton: true},
        {showViewButton: true},
        {showErrorLogButton: true},
        {stopJobContainerButton: true},
        {restartJobContainerButton: true}
    ];


    // -------------------------------------------------------------

    function render(jobs) {
        if (jobs === null) {
            $container.html('Error: Unable to load jobs!');
            return;
        }

        var jobClasses = jobs[KEY_jobNames];

        jobConfigCols[1].dropdown = jobClasses;
        addJobConfigCols[1].dropdown = _.concat(jobClasses);
        addJobConfigCols[1].placeholder = 'select JobClass...';


        //console.log(jobs);
        $container.html('');
        $container.append([
            '<h2>JobConfigs</h2>',
            '<p>A JobConfig is a <i>job template</i>, a blueprint from which a running job is constructed.</p>',
            mkJobConfigsTable(jobs[KEY_jobConfigs]),
            '<h3>Add new JobConfig</h3>',
            mkAddJobConfigTable(),
            '<h2>JobContainers</h2>',
            '<p>A JobContainer is a kind of vehicle for a <i>running job instance</i>.',
            mkRefreshButton(),
            mkJobContainersTable(jobs[KEY_jobContainers])
        ]);
    }
    
    function mkRefreshButton() {
        function refresh() {
            dispatch({type:'LOAD_JOBS'});
        }
        return $('<button>').text('refresh').addClass('refresh').click(refresh);
    }

    function mkDropdown(values, placeholder, selectedVal) {
        var $select = $('<select>');

        if (_.isString(placeholder)) {
            $select.append(
                $('<option>').attr({disabled: true, selected: true}).val('').text(placeholder)
            );
        }

        $select.append(_.map(values, function (value) {
            var $opt = $('<option>').val(value).text(value);
            if (value === selectedVal) {
                $opt.attr({selected: true});
            }
            return $opt;
        }));
        return $select;
    }


    function mkTable(tablePrefix, rows, cols, mkCell) {

        if (mkCell === undefined) {
            mkCell = mkTd;
        }

        var header = _.map(cols, function (col) {return _.get(col, 'alias', _.get(col, 'key', ''));});
        var $table = $('<table>').addClass("job-tab");
        $table.append('<tr><th>' + _.join(header, '</th><th>') + '</th></tr>');
        $table.append(_.map(rows, function (rowData) {
            return $('<tr>').append(_.map(cols, function (colData) {
                return mkCell(tablePrefix, rowData, colData, cols);
            }));
        }));
        return $table;
    }

    function mkJobConfigsTable(jobConfigs) {
        return mkTable("jobConfigsTable", jobConfigs, jobConfigCols);
    }

    function mkJobContainersTable(jobContainers) {
        return mkTable("jobContainersTable", jobContainers, jobContainerCols);
    }

    function mkAddJobConfigTable() {
        return mkTable("addJobTable", [defaultAddRow], addJobConfigCols);
    }

    function mkInputId(tablePrefix, rowData, key) {
        var prefix = rowData._prefix || rowData._id;
        return 'input-'+tablePrefix+'-'+prefix+'-'+key;
    }

    function mkTd(tablePrefix, rowData, colData, colDatas) {

        if (colData.buttons) {
            return $('<td>').append([
                mkUpdateButt(tablePrefix, rowData, colDatas),
                mkRunButt(tablePrefix, rowData, colDatas)
            ]);
        }

        if (colData.showViewButton) {
            return $('<td>').append(
                mkShowViewButt(rowData)
            );
        }

        if (colData.showLogButton) {
            return $('<td>').append(
                mkShowLogButt(rowData, MODE_log)
            );
        }

        if (colData.showErrorLogButton) {
            return $('<td>').append(
                mkShowLogButt(rowData, MODE_errorLog)
            );
        }

        if (colData.stopJobContainerButton) {
            return $('<td>').append(
                mkStopJobContainerButt(rowData)
            );
        }

        if (colData.restartJobContainerButton) {
            return $('<td>').append(
                mkRestartJobContainerButt(rowData)
            );
        }


        if (colData.addButton) {
            return $('<td>').append(
                mkAddButt(tablePrefix, rowData, colDatas)
            );
        }

        var key = colData.key;

        var val = _.get(rowData, key);
        var $html;

        if (colData.editable) {

            var inputId = mkInputId(tablePrefix, rowData, key);

            if (_.isArray(colData.dropdown)) {

                $html = mkDropdown(colData.dropdown, colData.placeholder, val).attr({id: inputId});

            } else {

                $html = $('<input>').attr({
                    id: inputId,
                    placeholder: colData.placeholder
                });

                if (colData.width) {
                    $html.css({width: colData.width});
                }

                if (colData.checkBox) {
                    $html.attr('type', 'checkbox').prop('checked', val);
                } else {

                    if (_.isObject(val)) {
                        val = JSON.stringify(val);
                    }

                    $html.val(val);
                }

            }

        } else {

            if (_.isObject(val)) {
                val = JSON.stringify(val);
            }

            $html = $('<div>').html(val);
        }

        var $td = $('<td>').addClass('job-td').append($html);

        if (colData.checkBox) {
            $td.addClass('centered');
        }

        return $td;
    }

    function addToUpdateJson(tablePrefix, colInfo, updateJson, jobConfig) {

        var key = colInfo.key;

        if (key === undefined) {
            return;
        }

        if (colInfo.editable) {

            var $input = $('#'+mkInputId(tablePrefix, jobConfig,key));
            if (colInfo.checkBox) {

                _.set(updateJson, key, $input.is(':checked'));

            } else {
                var val = $input.val();

                if (colInfo.object) {
                    val = JSON.parse(val);
                }

                if (colInfo.int && val !== '') {
                    val = +val;
                }

                _.set(updateJson, key, val === '' ? null : val);
            }

        } else {

            _.set(updateJson, key,  _.get(jobConfig, key));

        }

    }


    function addDataFromInputs(tablePrefix, action, rowData, colDatas, addNoneditables) {
        _.each(colDatas, function (colInfo) {
            if (colInfo.editable || addNoneditables) {
                addToUpdateJson(tablePrefix, colInfo, action, rowData);
            }
        });
        return action;
    }

    function mkAddButt(tablePrefix, rowData, colDatas) {

        function addJobConfig() {
            var action = addDataFromInputs(tablePrefix, {cmd: 'addJobConfig'}, rowData, colDatas);
            dispatch(action);
        }

        return $('<button>').text('Add').click(addJobConfig);
    }

    function mkUpdateButt(tablePrefix, rowData, colDatas) {

        function updateJobConfigRow() {
            var action = addDataFromInputs(tablePrefix, {cmd: 'updateJobConfig', _id: rowData._id}, rowData, colDatas);
            dispatch(action);
        }

        return $('<button>').text('update').addClass('jobConfigButt').click(updateJobConfigRow);
    }

    function mkRunButt(tablePrefix, jobConfig, colDatas) {

        function runJobConfigRow() {
            var action = addDataFromInputs(tablePrefix, {cmd: 'run'}, jobConfig, colDatas, true);
            dispatch(action);
        }

        return $('<button>').text('run').addClass('jobConfigButt').click(runJobConfigRow);
    }

    function mkShowViewButt(rowData) {

        function showView() {
            var jobContainerId = _.get(rowData, KEY_jobContainerId);
            var jobClass = _.get(rowData, [KEY_jobConfig, KEY_jobName]);
            dispatch({type: 'SHOW_JOB_VIEW', jobContainerId: jobContainerId, jobClass: jobClass});
        }

        return $('<button>').text('view').click(showView);
    }

    function mkShowLogButt(rowData, logMode) {

        function showLog() {
            var jobContainerId = _.get(rowData, KEY_jobContainerId);
            dispatch({type: 'SET_CURRENT_JOB_CONTAINER', jobContainerId: jobContainerId, logMode: logMode});
        }

        return $('<button>').text(logMode).click(showLog);
    }

    function mkStopJobContainerButt(rowData) {

        function stopJobContainer() {
            var jobContainerId = _.get(rowData, KEY_jobContainerId);
            dispatch({type: 'STOP_JOB_CONTAINER', jobContainerId: jobContainerId});
        }

        return $('<button>').text('stop').click(stopJobContainer);
    }

    function mkRestartJobContainerButt(rowData) {

        function restartJobContainer() {
            var jobContainerId = _.get(rowData, KEY_jobContainerId);
            dispatch({type: 'RESTART_JOB_CONTAINER', jobContainerId: jobContainerId});
        }

        return $('<button>').text('restart').click(restartJobContainer);
    }


    return {render: render};
}
