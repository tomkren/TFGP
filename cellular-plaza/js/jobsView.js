function mkJobsView($container, dispatch) {

    //var jobNames = ["test","test2","hax"];

    var firstTime = true;
    var previousJobNames = null;
    var previousJobs = null;

    var $jobStarterContainer;
    var $jobsTableContainer;

    function render(jobsInfo) {
        if (jobsInfo === null) {
            $container.html('Error: Unable to load jobs!');
            return;
        }

        var jobs = jobsInfo['jobs'];
        var jobNames = jobsInfo['jobNames'];

        if (firstTime) {
            firstTime = false;
            previousJobNames = jobNames;
            previousJobs = jobs;

            $jobStarterContainer = $('<div>').html(mkJobStarter(jobNames));
            $jobsTableContainer  = $('<div>').html(mkJobsTable(jobs));

            $container.html('');
            $container.append([$jobStarterContainer, $jobsTableContainer]);

        } else {

            // todo prasárna ale to vyřeší přechod na react, tak ted na rychlo to nevadí

            if (!_.isEqual(jobNames, previousJobNames)) {
                $jobStarterContainer.html(mkJobStarter(jobs));
                previousJobNames = jobNames;
            }
            if (!_.isEqual(jobs, previousJobs)) {
                $jobsTableContainer.html(mkJobsTable(jobs));
                previousJobs = jobs;
            }
        }


    }

    function mkJobStarter(jobNames) {
        var $select = mkJobDropdown(jobNames);
        return $('<div>').addClass("job-starter").append([
            $select,
            mkJobRunButton($select)
        ]);
    }



    function mkJobDropdown(jobNames) {
        var $select = $('<select>').addClass("job-dropdown");
        $select.append(_.map(jobNames, function (jobName) {
            return $('<option>').val(jobName).html(jobName)
        }));
        return $select;
    }

    function mkJobRunButton($select) {
        return $('<button>').html("run").click(function () {
            dispatch({
                cmd: 'run',
                job: $select.val()
            });
        });
    }

    function mkJobsTable(jobs) {


        var $table = $('<table>');
        $table.addClass("blue-tab");

        if (_.isEmpty(jobs)) {
            $table.append($('<tr>').append($('<td>').text('There are no running or finished jobs.')));
        } else {
            $table.html('<tr><th>id</th><th>status</th><th>opts</th></tr>');
            _.each(jobs, function (job) {
                $table.append(mkJobRow(job));
            });
        }

        return $table;
    }

    function mkJobRow(job) {

        return $('<tr>').append([
            mkTd(job, 'jobId', {click: showJob(job['jobId'])}),
            mkTd(job, 'jobStatus'),
            mkTd(job, 'jobOpts')
            //mkUpdateButt(job)
        ]);

    }

    function showJob(jobId) {
        return function () {
            dispatch({
                cmd: 'setCurrentJob',
                jobId: jobId
            });
        }
    }

    function mkInputId(job, key) {
        return 'input-'+key+'-'+job['jobId'];
    }

    function mkTd(job, key, opts) {

        opts  = opts || {editable: false, checkBox: false, click: undefined};

        var val = job[key];
        var $html;
        if (opts.editable) {
            $html = $('<input>').attr('id', mkInputId(job,key));
            if (opts.checkBox) {
                $html.attr('type', 'checkbox').prop('checked', val);
            } else {
                $html.val(val);
            }
        } else {

            $html = opts.click ? $('<button>').addClass('hand') : $('<div>');

            if (_.isString(val) || _.isNumber(val)) {
                $html.html(val);
            } else {
                $html.html(JSON.stringify(val));
            }

            if (opts.click) {
                $html.click(opts.click);
            }

        }

        return $('<td>').append($html);
    }

    function addToUpdateJson(updateJson, job, key, checkBox) {
        var $input = $('#'+mkInputId(job,key));
        if (checkBox) {
            updateJson[key] = $input.is(':checked');
        } else {
            var val = $input.val();
            updateJson[key] = (val === '' ? null : val);
        }
    }

    function mkUpdateButt(job) {
        return $('<button>').text('update').click(function () {
            var updateJson = {_id: job._id};

            addToUpdateJson(updateJson, job, 'process');
            addToUpdateJson(updateJson, job, 'delay');
            addToUpdateJson(updateJson, job, 'period');
            addToUpdateJson(updateJson, job, 'scheduleOnStartup', true);

            log(updateJson);
            log("TODO: just logging update...");
            //updateRow(updateJson);
        });
    }


    return {
        render: render
    };
}
