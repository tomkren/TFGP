package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.fishtron.ugen.server.jobs.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/** Created by tom on 05.03.2017. */

class JobFactory {


    private final Map<String,Class<? extends EvaJob>> jobClasses;

    JobFactory() {
        jobClasses = new HashMap<>();
    }

    void addJobClass(String jobName, Class<? extends EvaJob> jobClass) {
        jobClasses.put(jobName, jobClass);
    }


    EvaJob mkJob(String jobName) {

        Class<? extends EvaJob> jobClass = jobClasses.get(jobName);

        if (jobClass == null) {return null;}

        try {
            Constructor<? extends EvaJob> jobConstructor = jobClass.getConstructor();

            return jobConstructor.newInstance();

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }

        /* if (jobName.equals("test")) {
            return new Test();
        }
        return null;*/
    }

}
