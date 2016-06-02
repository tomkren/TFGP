package cz.tomkren.utils;

public class TODO extends Error {

    public TODO () {
        super("TODO");
    }

    public TODO (String str) {
        super("TODO : "+str);
    }


}
