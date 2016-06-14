package cz.tomkren.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Created by  tom on 23.7.2015. */

public class ResourceLoader {

    public ResourceLoader() {
    }

    public InputStream loadStream(String resourcePath) {
        try {
            return getClass().getResource(resourcePath).openStream();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public String loadString(String resourcePath, String encoding) {
        try {
            return IOUtils.toString(loadStream(resourcePath), encoding);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public String loadFile(String filePath) {
        try {
            Charset encoding = Charset.forName("UTF-8");
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            return new String(encoded, encoding);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public String loadString(String resourcePath) {
        return loadString(resourcePath, "UTF-8");
    }

    public JSONObject loadJSON(String resourcePath) {
        return new JSONObject(loadString(resourcePath));
    }

}
