package net.fishtron.eva.multi;

import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.util.List;
import java.util.function.BiFunction;

/**Created by tom on 20.03.2017.*/

public class Configs {

    public static final String numEvaluations = "numEvaluations";
    public static final String numIndividualsToGenerate = "numIndividualsToGenerate";
    public static final String minPopulationSizeToOperate = "minPopulationSizeToOperate";
    public static final String maxPopulationSize = "maxPopulationSize";
    public static final String generatingMaxTreeSize = "generatingMaxTreeSize";
    public static final String tournamentBetterWinsProbability = "tournamentBetterWinsProbability";
    public static final String seed = "seed";
    public static final String timeLimit = "timeLimit";
    public static final String sleepTime = "sleepTime";
    public static final String dummyFitness = "dummyFitness";
    public static final String poolSize = "poolSize";
    //public static final String
    //public static final String
    //public static final String


    public static Checker handleRandomSeed(JSONObject config, Checker checker) {
        Long seed = config.has(Configs.seed) ? config.getLong(Configs.seed) : null;
        if (checker == null) {checker = new Checker(seed);}
        if (seed    == null) {config.put(Configs.seed, checker.getSeed());}
        return checker;
    }


    // todo udelat bezpečnějc dyštak
    public static List<Boolean> getIsMaxims(JSONObject config) {
        return F.map(config.getJSONArray("isMaxims"), x->(boolean)x);
    }


    public static String getString(JSONObject config, String key, String defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getString);
    }

    public static int getInt(JSONObject config, String key, int defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getInt);
    }

    public static double getDouble(JSONObject config, String key, double defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getDouble);
    }

    public static boolean getBoolean(JSONObject config, String key, boolean defaultValue) {
        return getValue(config,key,defaultValue, JSONObject::getBoolean);
    }

    private static <A> A getValue(JSONObject config, String key, A defaultValue, BiFunction<JSONObject, String, A> accessFun) {
        if (config.has(key)) {
            return accessFun.apply(config,key);
        } else {
            config.put(key, defaultValue);
            return defaultValue;
        }
    }
}
