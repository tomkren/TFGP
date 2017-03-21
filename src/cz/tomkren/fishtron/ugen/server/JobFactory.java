package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;
import org.json.JSONArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/** Created by tom on 05.03.2017. */

class JobFactory {


    private final Map<String,AB<Class<? extends EvaJob>,Object>> jobClasses;

    JobFactory() {
        jobClasses = new HashMap<>();
    }

    void addJobClass(String jobName, Class<? extends EvaJob> jobClass, Object initData) {
        jobClasses.put(jobName, AB.mk(jobClass, initData));
    }

    EvaJob mkJob(String jobName) {

        AB<Class<? extends EvaJob>,Object> jobClassData = jobClasses.get(jobName);
        Class<? extends EvaJob> jobClass = jobClassData._1();
        Object initData = jobClassData._2();

        if (jobClass == null) {return null;}

        try {


            if (initData == null) {
                Constructor<? extends EvaJob> jobConstructor = jobClass.getConstructor();
                return jobConstructor.newInstance();
            } else {
                Constructor<? extends EvaJob> jobConstructor = jobClass.getConstructor(Object.class);
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
