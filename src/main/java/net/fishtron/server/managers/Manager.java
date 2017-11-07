package net.fishtron.server.managers;

import net.fishtron.server.api.ApiCmd;

import java.util.Collections;
import java.util.List;

/**
 * Created by tom on 27.08.2017.
 */
public interface Manager {

    default String greetings() {
        return "Hello there! Me? I am an anonymous Manager ninja.";
    }

    default List<ApiCmd> mkApiCmds() {
        return Collections.emptyList();
    }
}
