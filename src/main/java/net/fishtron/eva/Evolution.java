package net.fishtron.eva;

import net.fishtron.server.api.Api;

/**
 * Created by tom on 28.10.2017.
 */
public interface Evolution {

    void startEvolution();

    default Api getApi() {
        return null;
    }

}
