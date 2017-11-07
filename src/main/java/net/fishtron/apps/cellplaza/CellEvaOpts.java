package net.fishtron.apps.cellplaza;

import net.fishtron.apps.cellplaza.v2.CellPlaza;
import net.fishtron.server.api.Api;
import net.fishtron.utils.Either;
import net.fishtron.utils.F;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by tom on 28.10.2017.
 */
public class CellEvaOpts {

    private final JSONObject config;
    private final String logPath;

    CellEvaOpts(JSONObject config, String logPath) {
        this.config = config;
        this.logPath = logPath;
    }

    public static Either<CellEvaOpts,JSONObject> mkDefault() {

        String configPath = CellPlaza.CONFIG_PATH;
        String logPath = CellPlaza.BASE_DIR+"/"+"evaLog";

        try {

            JSONObject config = F.tryLoadJson(configPath);
            return Either.ok(new CellEvaOpts(config, logPath));

        } catch (IOException e) {
            F.log(e);
            String configDescription = "the default";
            String errorMsg = "Unable to load "+configDescription+" config file '"+configPath+"', please check the path.";
            F.log(errorMsg);

            return Either.ko(Api.error(errorMsg));
        }

    }

    public JSONObject getConfig() {
        return config;
    }

    public String getLogPath() {
        return logPath;
    }

    @Override
    public String toString() {
        return "CellEvaOpts{logPath='" + logPath + "', config=" + config + "}";
    }
}
