package app.control.api.endpoints;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IDsFormater {
    public static String urlFromIds(String endpoint, List<String> ids) {
        return String.join("?", endpoint,joinAndFormate(ids));
    }

    private static String joinAndFormate(List<String> ids) {
        return String.format(
                "ids=%s",
                URLEncoder.encode(String.join(",",ids), StandardCharsets.UTF_8));
    }
}
