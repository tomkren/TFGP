package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.utils.F;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

/** Created by Tomáš Křen on 18.2.2017.*/

public abstract class ServerEva extends AbstractHandler {

    private static JSONObject defaultConfig = F.obj(
            "port", 2342
    );





}
