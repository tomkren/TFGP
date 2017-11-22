package net.fishtron.server.api;

import net.fishtron.utils.F;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tom on 27.08.2017.
 */

// TODO sjednotit s net.fishtron.eva.multi;

public class Configs {

    // TODO revidovata sjednotit s TFGP a F.

    public static boolean get_boolean(JSONObject o, String key, boolean defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_boolean warning");
            return defaultVal;
        }
        try {
            return o.has(key) ? o.getBoolean(key) : defaultVal;
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    public static JSONObject get_JSONObject(JSONObject o, String key, JSONObject defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_JSONObject warning");
            return defaultVal;
        }
        try {
            if (o.has(key)) {
                if (o.get(key) == JSONObject.NULL) {
                    return null;
                }
                return o.getJSONObject(key);
            } else {
                return defaultVal;
            }
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    public static JSONArray get_JSONArray(JSONObject o, String key, JSONArray defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_JSONObject warning");
            return defaultVal;
        }
        try {
            if (o.has(key)) {
                if (o.get(key) == JSONObject.NULL) {
                    return null;
                }
                return o.getJSONArray(key);
            } else {
                return defaultVal;
            }
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    public static Long get_Long(JSONObject o, String key, Long defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_Long warning");
            return defaultVal;
        }
        try {
            if (o.has(key)) {
                if (o.get(key) == JSONObject.NULL) {
                    return null;
                }
                return o.getLong(key);
            } else {
                return defaultVal;
            }
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    public static long get_long(JSONObject o, String key, long defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_long warning");
            return defaultVal;
        }
        try {
            return o.has(key) ? o.getLong(key) : defaultVal;
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    public static double get_double(JSONObject o, String key, double defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_double warning");
            return defaultVal;
        }
        try {
            return o.has(key) ? o.getDouble(key) : defaultVal;
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    public static int get_int(JSONObject o, String key, int defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_int warning");
            return defaultVal;
        }
        try {
            return o.has(key) ? o.getInt(key) : defaultVal;
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    public static String get_String(JSONObject o, String key, String defaultVal) {
        if (o == null) {
            F.log("WARNING: NULL obj get_String warning");
            return defaultVal;
        }
        try {
            if (o.has(key)) {
                if (o.get(key) == JSONObject.NULL) {
                    return null;
                }
                return o.getString(key);
            } else {
                return defaultVal;
            }
        } catch (JSONException e) {
            return defaultVal;
        }
    }

}
