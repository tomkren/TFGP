package net.fishtron.server.managers;

import net.fishtron.server.api.Api;
import net.fishtron.server.api.ApiCmd;
import net.fishtron.server.jobs.EvaJob;
import net.fishtron.server.jobs.JobContainer;
import net.fishtron.server.jobs.JobFactory;

import net.fishtron.utils.Either;
import net.fishtron.utils.F;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;


import static com.mongodb.client.model.Filters.*;


import java.util.*;

/** Created by sekol on 12.12.2016.*/

public class JobManager implements Manager {

    private static final String MONGO_jobs = "jobs";
    private static final String MONGO__id = "_id";

    private List<EvaJob.Config> jobConfigs;
    private Set<String> jobConfigIds;

    private final JobFactory jobFactory;
    private final Map<Integer, JobContainer> jobContainers;
    private final Map<String, Integer> _id2jobContainerId;
    private int nextJobContainerId;

    private Timer timer;
    private MongoManager mongoMan;

    public JobManager(MongoManager mongoMan) {
        this(getJobConfigsFromMongo(mongoMan));
        this.mongoMan = mongoMan;
    }

    public JobManager(JSONArray jobConfigsJson) {
        this(getJobConfigsFromJson(jobConfigsJson));
    }

    private static List<EvaJob.Config> getJobConfigsFromMongo(MongoManager mongoMan) {
        return getJobConfigsFromJson(mongoMan.find("jobs"));
    }

    private static List<EvaJob.Config> getJobConfigsFromJson(JSONArray jobConfigsJson) {
        if (jobConfigsJson == null) { return Collections.emptyList(); }
        return F.map(jobConfigsJson, o -> new EvaJob.Config((JSONObject)o));
    }

    private JobManager(List<EvaJob.Config> jobConfigs) {
        this.jobConfigs = new ArrayList<>(jobConfigs);
        this.jobFactory = new JobFactory();
        this.jobContainers = new HashMap<>();
        this._id2jobContainerId = new HashMap<>();
        this.nextJobContainerId = 1;
        this.timer = null;

        fillJobConfigIds();
    }

    private void fillJobConfigIds(){
        jobConfigIds = new HashSet<>(F.map(jobConfigs, EvaJob.Config::get_id));
    }

    @Override
    public String greetings() {
        return "JobManager looking for your jobContainers, since 2017.";
    }

    @Override
    public List<ApiCmd> mkApiCmds() {
        return Arrays.asList(
                new ApiCmd(Api.CMD_jobs, this::jobs_api,         "Shows info about currently running jobs and more. Possible call is '/"+Api.CMD_jobs+"'."),
                new ApiCmd(Api.CMD_job,  this::job_api,          "Shows info about specific jobContainer, possible call is '/"+Api.CMD_job+"/<"+Api.KEY_jobContainerId+">'."),
                new ApiCmd(Api.CMD_run,  this::runJobContainer_api, "Starts new jobContainer, possible call is '/"+Api.CMD_run+"/<"+Api.KEY_jobName+">'."),
                new ApiCmd(Api.CMD_addJobConfig, this::addJobConfig_api, Api.MAN_todo),
                new ApiCmd(Api.CMD_updateJobConfig, this::updateJobConfig_api, Api.MAN_todo)
        );
    }


    public void start() {
        timer = new Timer(false); // todo opravdu not daemon?
        for (EvaJob.Config jobConfig : jobConfigs) {
            if (jobConfig.isScheduleOnStartup()) {
                scheduleJob(jobConfig);
            }
        }
    }

    public void stop() {
        timer.cancel();
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass, Object initData) {
        jobFactory.addJobClass(jobName, jobClass, initData);
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass) {
        jobFactory.addJobClass(jobName, jobClass);
    }

    private JSONObject jobs_api() {
        return Api.ok(
                Api.KEY_jobContainers, F.jsonMap(jobContainers.values(), JobContainer::toJson),
                Api.KEY_jobConfigs, F.jsonMap(jobConfigs, EvaJob.Config::json),
                Api.KEY_jobNames, jobFactory.getJobNames()
        );
    }

    private JSONObject job_api(JSONArray path, JSONObject query) {
        addPathDataToQuery(path, query);

        Either<Integer,JSONObject> jobContainerIdRes = getJobContainerId(query);
        if (jobContainerIdRes.isKO()) {return jobContainerIdRes.getKO();}
        int jobContainerId = jobContainerIdRes.getOK();
        JobContainer jobContainer = jobContainers.get(jobContainerId);

        if (jobContainer == null) {
            return Api.error("There is no jobContainer with jobContainerId "+jobContainerId+".");
        }

        String jobCmd = query.optString(Api.KEY_jobCmd, null);

        if (jobCmd == null) {
            return Api.addOk(jobContainer.toJson());
        }

        if (jobCmd.equals(Api.JCMD_log) || jobCmd.equals(Api.JCMD_errorLog)) { // special case:   /job/XYZ/log/N   or   /job/XYZ/errorLog/N
            int numLast = query.optInt(Api.KEY_numLast, -1);
            if (path.length() > 3) {
                try {numLast = Integer.parseInt(path.getString(3));} catch (NumberFormatException ignored) {}
            }

            if (jobCmd.equals(Api.JCMD_log)) {
                return getJobLog(jobContainer, numLast);
            } else {
                return getJobErrorLog(jobContainer, numLast);
            }
        }

        if (jobCmd.equals(Api.JCMD_stop)) {
            return stopJobContainer(jobContainer);
        }

        if (jobCmd.equals(Api.JCMD_restart)) {
            return restartJobContainer(jobContainer);
        }

        // Process jobCmd, e.g. when /job/1/someJobCmd  or  /job/1?{"jobCmd":"someJobCmd"} or ?{"cmd":"job", "jobContainerId":1, "jobCmd":"someJobCmd"} was called.
        return jobContainer.getJobApi().processApiCall(path, query);
    }

    private static Either<Integer, JSONObject> getJobContainerId(JSONObject query) {
        if (!query.has(Api.KEY_jobContainerId)) {
            return Either.ko(Api.error("Missing key: "+ Api.KEY_jobContainerId));
        }

        Object jobContainerIdObj = query.get(Api.KEY_jobContainerId);
        if (!(jobContainerIdObj instanceof Integer)) {
            return Either.ko(Api.error(Api.KEY_jobContainerId +" must be an integer."));
        }

        return Either.ok((int) jobContainerIdObj);
    }

    private JSONObject getJobLog(JobContainer jobContainer, int numLast) {
        return Api.ok(
                Api.KEY_log, jobContainer.getLog(numLast),
                Api.KEY_jobContainerId, jobContainer.getJobContainerId(),
                Api.KEY_numLast, numLast,
                Api.KEY_maxLogSize, jobContainer.getMaxLogSize(),
                Api.KEY_numAllLogLines, jobContainer.getNumAllLogLines(),
                Api.KEY_maxErrorLogSize, jobContainer.getMaxErrorLogSize(),
                Api.KEY_numAllErrorLogLines, jobContainer.getNumAllErrorLogLines()
        );
    }

    private JSONObject getJobErrorLog(JobContainer jobContainer, int numLast) {
        return Api.ok(
                Api.KEY_log, jobContainer.getErrorLog(numLast),
                Api.KEY_jobContainerId, jobContainer.getJobContainerId(),
                Api.KEY_numLast, numLast,
                Api.KEY_maxLogSize, jobContainer.getMaxLogSize(),
                Api.KEY_numAllLogLines, jobContainer.getNumAllLogLines(),
                Api.KEY_maxErrorLogSize, jobContainer.getMaxErrorLogSize(),
                Api.KEY_numAllErrorLogLines, jobContainer.getNumAllErrorLogLines()
        );
    }

    private JSONObject stopJobContainer(JobContainer jobContainer) {
        jobContainer.stopJobContainer();
        return Api.ok(
                Api.KEY_msg, "Requested stop for jobContainer.",
                Api.KEY_jobContainerId, jobContainer.getJobContainerId()
        );
    }

    private JSONObject restartJobContainer(JobContainer jobContainer) {
        jobContainer.restartJobContainer();
        return Api.ok(
                Api.KEY_msg, "Requested restart for jobContainer.",
                Api.KEY_jobContainerId, jobContainer.getJobContainerId()
        );
    }


    private JSONObject runJobContainer_api(JSONObject query) {
        query.remove(Api.KEY_cmd);
        EvaJob.Config jobConfig = new EvaJob.Config(query);
        return scheduleJob(jobConfig);
    }

    private JSONObject addJobConfig_api(JSONObject query) {
        query.remove(Api.KEY_cmd);
        EvaJob.Config jobConfig = new EvaJob.Config(query);
        return addJobConfig(jobConfig);
    }

    private JSONObject updateJobConfig_api(JSONObject query) {
        query.remove(Api.KEY_cmd);
        return updateJobConfig(query);
    }

    private JSONObject addJobConfig(EvaJob.Config jobConfig) {

        String newId = jobConfig.get_id();

        if (newId == null) { return Api.error("Job config _id cannot be null."); }
        if (newId.equals("")) { return Api.error("Job config _id cannot be an empty string."); }
        if (jobConfigIds.contains(newId)) { return Api.error("Job config _id is not unique."); }

        jobConfigs.add(jobConfig);
        jobConfigIds.add(newId);

        if (mongoMan != null) {
            installJob(jobConfig.json());
        }

        return Api.ok(Api.KEY_jobConfigs, jobConfig.json());
    }

    private JSONObject updateJobConfig(JSONObject updateQuery) {

        String _id = updateQuery.optString(Api.KEY__id, null);

        if (_id == null) {
            return Api.error("Missing "+Api.KEY__id+" in updateQuery.");
        }

        List<EvaJob.Config> hits = F.filter(jobConfigs, jc -> jc.get_id().equals(_id));

        if (hits.size() == 0) {
            return Api.error("Missing jobConfig with "+Api.KEY__id+" = "+_id+".");
        }

        if (hits.size() > 1) {
            return  Api.error("More than one jobConfig with "+Api.KEY__id+" = "+_id+".");
        }

        EvaJob.Config jobConfig = hits.get(0);

        jobConfig.update(updateQuery);

        if (mongoMan != null) {

            JSONObject toUpdate = new JSONObject(jobConfig.json().toString());
            toUpdate.remove(MONGO__id);
            Document newJobConfigValueInMongo = MongoManager.toDoc(toUpdate);

            mongoMan.getCollection(MONGO_jobs).updateOne(eq(MONGO__id, jobConfig.get_id()), new Document("$set", newJobConfigValueInMongo));
        }

        return Api.ok(
            Api.KEY_jobConfigs, jobConfig.json()
        );
    }

    /*public JSONObject runJob(EvaJob.Config jobConfig) {
        //jobConfigs.add(jobConfig);
        return scheduleJob(jobConfig);
    }*/

    private Either<JobContainer,JSONObject> createJobContainer(EvaJob.Config jobConfig) {

        EvaJob job = jobFactory.mkJob(jobConfig.getName());

        if (job == null) {
            return Either.ko(Api.error("Unknown job name: "+jobConfig.getName()));
        }

        int jobContainerId = nextJobContainerId;
        nextJobContainerId ++;

        JobContainer newJobContainer = new JobContainer(jobContainerId, job, jobConfig);

        jobContainers.put(jobContainerId, newJobContainer);

        if (jobConfig.has_id()) {
            _id2jobContainerId.put(jobConfig.get_id(), jobContainerId);
        }

        F.log("New '"+jobConfig.getName()+"' jobContainer ["+ Api.KEY_jobContainerId +"="+jobContainerId+"] successfully created.");

        return Either.ok(newJobContainer);
    }

    public JSONObject scheduleJob(EvaJob.Config jobConfig) {

        long delay = jobConfig.getDelay();
        Long period = jobConfig.getPeriod();

        Either<JobContainer,JSONObject> jobContainerRes = createJobContainer(jobConfig);

        if (jobContainerRes.isOK()) {
            JobContainer jobContainer = jobContainerRes.getOK();

            String msg = "";

            if (period == null) {
                timer.schedule(jobContainer, delay);
                msg += "One-time";
            } else {
                timer.scheduleAtFixedRate(jobContainer, delay, period);
                msg += "Periodic";
            }

            msg +=  " job scheduled .. delay: "+ delay +
                    " period:"+ period +
                    " jobName: "+ jobConfig.getName() +
                    " .. "+ jobConfig.json();

            F.log(msg);

            return Api.ok(Api.KEY_jobContainerId,jobContainer.getJobContainerId());
        } else {
            F.log("!!! ERROR jobContainer-schedule-error: "+jobContainerRes.getKO());
            return jobContainerRes.getKO();
        }
    }


    public void install(List<JSONObject> defaultJobs) {
        if (jobConfigs.isEmpty()) {
            F.log("Jobs are empty, installing default jobs ...");
            for (JSONObject jobConfig : defaultJobs) {
                installJob(jobConfig);
                F.log("  "+jobConfig +" added.");
            }

            jobConfigs = getJobConfigsFromMongo(mongoMan);
            fillJobConfigIds();

        } else {
            F.log("Jobs are not empty, INSTALLING NOTHING. Current jobs ("+ jobConfigs.size()+"):");
            for (EvaJob.Config job : jobConfigs) {
                F.log("  "+job.json());
            }
        }
    }

    private void installJob(JSONObject jobConfig) {
        mongoMan.insert(MONGO_jobs, jobConfig);
    }

    // TODO remove
    /*private void installJob_OLD(JSONObject jobConfig, boolean doScheduleTheJob) {
        mongoMan.insert("jobs", jobConfig);
        if (doScheduleTheJob) {
            Job_OLD job = new Job_OLD(jobConfig);
            scheduleJob_OLD(job);
        }
    }*/

    /*private static void mainTest() throws InterruptedException {
        F.log("Testujeme JobManager!");

        JSONArray jobs = F.arr(
                F.obj("delay", 1000),
                F.obj("process", "test2")
        );

        List<Job_OLD> jobList = F.map(jobs, jobConfig -> new Job_OLD((JSONObject) jobConfig));

        JobManager jobMan = new JobManager(jobList);
        jobMan.start();

        F.log("After jobMan start.");

        Thread.sleep(2000);

        jobMan.scheduleJob_OLD(new Job_OLD(F.obj("process","test", "period",500)));
    }

    public static void main(String[] args) throws InterruptedException {
        mainTest();
    }*/


    private void addPathDataToQuery(JSONArray path, JSONObject query) {
        if (path.length() > 1) {
            String idStr = path.getString(1);
            try {
                int idInt = Integer.parseInt(idStr);
                query.put(Api.KEY_jobContainerId, idInt);
            } catch (NumberFormatException e) {

                Integer maybeContainerId = _id2jobContainerId.get(idStr);
                if (maybeContainerId != null) {
                    query.put(Api.KEY_jobContainerId, maybeContainerId);
                    query.put(Api.KEY__id, idStr);
                }
            }
        }

        if (!query.has(Api.KEY_jobContainerId) && query.has(Api.KEY__id) && query.get(Api.KEY__id) instanceof String) {
            String idStr = query.getString(Api.KEY__id);
            Integer maybeContainerId = _id2jobContainerId.get(idStr);
            if (maybeContainerId != null) {
                query.put(Api.KEY_jobContainerId, maybeContainerId);
            }
        }

        if (path.length() > 2) {
            String jobCmdStr = path.getString(2);
            query.put(Api.KEY_jobCmd, jobCmdStr);
        }
    }



}
