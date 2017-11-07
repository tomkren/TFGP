package net.fishtron.server.jobs;

import net.fishtron.server.api.Api;
import net.fishtron.server.api.Configs;
import net.fishtron.utils.F;
import org.json.JSONObject;

/**
 * Created by tom on 22.08.2017.
 */
public interface EvaJob extends Api {
    JSONObject runJob(Config jobConfig, JobContainer jobContainer);

    class Config {
        private final JSONObject configJson;

        private final String _id;
        private String name;
        private JSONObject opts;
        private long delay;
        private Long period;
        private boolean scheduleOnStartup;

        public Config(JSONObject configJson) {
            this.configJson = configJson;
            this._id = Configs.get_String(configJson, Api.KEY__id, null);
            this.name = Configs.get_String(configJson, Api.KEY_jobName, null);
            this.opts = Configs.get_JSONObject(configJson, Api.KEY_opts, null);
            this.delay = Configs.get_long(configJson, Api.KEY_delay,  0L);
            this.period = Configs.get_Long(configJson, Api.KEY_period, null);
            this.scheduleOnStartup = Configs.get_boolean(configJson, Api.KEY_scheduleOnStartup, false);
        }

        public Config(Config c) {
            this.configJson = new JSONObject(c.configJson.toString());

            this._id = c._id;
            this.name = c.name;
            this.opts = new JSONObject(c.opts.toString());
            this.delay = c.delay;
            this.period = c.period;
            this.scheduleOnStartup = c.scheduleOnStartup;
        }


        public Config(String _id, String name, JSONObject opts, long delay, Long period, boolean scheduleOnStartup) {
            this._id = _id;
            this.name = name;
            this.opts = opts;
            this.delay = delay;
            this.period = period;
            this.scheduleOnStartup = scheduleOnStartup;

            this.configJson = F.obj(
                    Api.KEY__id         , _id,
                    Api.KEY_jobName     , name,
                    Api.KEY_opts        , opts,
                    Api.KEY_delay       , delay,
                    Api.KEY_period      , period
            );
        }

        public void update(JSONObject updateQuery) {

            name   = Configs.get_String(updateQuery, Api.KEY_jobName, name);
            opts   = Configs.get_JSONObject(updateQuery, Api.KEY_opts, opts);
            delay  = Configs.get_long(updateQuery, Api.KEY_delay, delay);
            period = Configs.get_Long(updateQuery, Api.KEY_period, period);
            scheduleOnStartup = Configs.get_boolean(updateQuery, Api.KEY_scheduleOnStartup, scheduleOnStartup);

            configJson.put(Api.KEY_jobName, name);
            configJson.put(Api.KEY_opts, opts);
            configJson.put(Api.KEY_delay, delay);
            configJson.put(Api.KEY_period, period == null ? JSONObject.NULL : period);
            configJson.put(Api.KEY_scheduleOnStartup, scheduleOnStartup);
        }

        public JSONObject json() {return configJson;}

        public JSONObject getOpts() { return opts; }

        public String getName() {return name;}
        public long getDelay() {return delay;}
        public Long getPeriod() {return period;}
        public boolean isScheduleOnStartup() {return scheduleOnStartup;}

        boolean isRepeated() {return period != null;}
        public boolean has_id() {return _id != null;}
        public String get_id() {return _id;}
    }
}
