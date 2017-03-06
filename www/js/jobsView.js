function mkJobsView($container, dispatch) {


    function render(jobs) {
        if (jobs === null) {
            $container.html('Error: Unable to load jobs!');
            return;
        }

        //log(jobs);
        $container.html('');
        $container.append([
            mkJobsTable(jobs),
            mkJobStarter()
        ]);
    }

    function mkJobStarter() {
        var $select = mkJobDropdown();
        return $('<div>').append([
            $select,
            mkJobRunButton($select)
        ]);
    }

    var jobNames = ["test","test2","hax"];

    function mkJobDropdown() {
        var $select = $('<select>');
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
        $table.addClass("job-tab").html('<tr><th>id</th><th>opts</th><th>status</th></tr>');
        _.each(jobs, function (job) {
            $table.append(mkJobRow(job));
        });
        return $table;
    }

    function mkJobRow(job) {

        return $('<tr>').append([
            mkTd(job, 'jobId'),
            mkTd(job, 'jobOpts'),
            mkTd(job, 'jobStatus')
            //mkUpdateButt(job)
        ]);

    }

    function mkInputId(job, key) {
        return 'input-'+key+'-'+job.jobId;
    }

    function mkTd(job, key, editable, checkBox) {
        var val = job[key];
        var $html;
        if (editable) {
            $html = $('<input>').attr('id', mkInputId(job,key));
            if (checkBox) {
                $html.attr('type', 'checkbox').prop('checked', val);
            } else {
                $html.val(val);
            }
        } else {

            if (_.isString(val) || _.isNumber(val)) {
                $html = $('<div>').html(val);
            } else {
                $html = $('<div>').html(JSON.stringify(val));
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
