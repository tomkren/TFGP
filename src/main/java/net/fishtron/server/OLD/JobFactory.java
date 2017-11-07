package net.fishtron.server.OLD;

import net.fishtron.utils.AB;
import net.fishtron.utils.F;
import org.json.JSONArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/** Created by tom on 05.03.2017. */

class JobFactory {

    private final Map<String,AB<Class<? extends EvaJob_OLD>,Object>> jobClasses;

    JobFactory() {
        jobClasses = new HashMap<>();
    }

    void addJobClass(String jobName, Class<? extends EvaJob_OLD> jobClass, Object initData) {
        jobClasses.put(jobName, AB.mk(jobClass, initData));
    }

    EvaJob_OLD mkJob(String jobName) {

        AB<Class<? extends EvaJob_OLD>,Object> jobClassData = jobClasses.get(jobName);

        if (jobClassData == null) {return null;}

        Class<? extends EvaJob_OLD> jobClass = jobClassData._1();
        Object initData = jobClassData._2();

        try {

            if (initData == null) {
                Constructor<? extends EvaJob_OLD> jobConstructor = jobClass.getConstructor();
                return jobConstructor.newInstance();
            } else {
                Constructor<? extends EvaJob_OLD> jobConstructor = jobClass.getConstructor(Object.class);
                return jobConstructor.newInstance(initData);
            }


        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    JSONArray getJobNames() {
        return F.jsonMap(jobClasses.keySet());
    }

}
