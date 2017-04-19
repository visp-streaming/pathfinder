package net.knasmueller.pathfinder;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by bernhard on 19.04.17.
 */
public class TestUtil {
    public static String resourceToString(Resource res) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(res.getURI()));
        return new String(encoded);
    }
}
