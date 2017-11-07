package net.fishtron.server.jobs;

import net.fishtron.utils.AB;
import net.fishtron.utils.F;

import org.json.JSONArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/** Created by Tomáš Křen on 13.12.2016. */

public class JobFactory {

    private final Map<String,AB<Class<? extends EvaJob>,Object>> jobClasses;

    public JobFactory() {
        jobClasses = new HashMap<>();
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass, Object initData) {
        jobClasses.put(jobName, AB.mk(jobClass, initData));
    }

    public void addJobClass(String jobName, Class<? extends EvaJob> jobClass) {
        addJobClass(jobName, jobClass, null);
    }

    public EvaJob mkJob(String jobName) {

        AB<Class<? extends EvaJob>,Object> jobClassData = jobClasses.get(jobName);

        if (jobClassData == null) {return null;}

        Class<? extends EvaJob> jobClass = jobClassData._1();
        Object initData = jobClassData._2();

        try {
            if (initData == null) {
                Constructor<? extends EvaJob> jobConstructor = jobClass.getConstructor();
                return jobConstructor.newInstance();
            } else {
                Constructor<? extends EvaJob> jobConstructor = jobClass.getConstructor(Object.class);
                return jobConstructor.newInstance(initData);
            }
        } catch (NoSuchMethodException e) {
            F.log("NoSuchMethodException: "+ e.getMessage());
            return null;
        } catch(InstantiationException e) {
            F.log("InstantiationException: "+e.getMessage());
            return null;
        } catch (IllegalAccessException e) {
            F.log("IllegalAccessException: "+e.getMessage());
            return null;
        } catch (InvocationTargetException e) {
            F.log("InvocationTargetException: "+e.getMessage());
            return null;
        }
    }

    public JSONArray getJobNames() {
        return F.jsonMap(jobClasses.keySet());
    }

}
