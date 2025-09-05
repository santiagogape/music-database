package Main.configurations;

import java.util.prefs.Preferences;

public final class UserConfig {
    private static final Preferences prefs = Preferences.userNodeForPackage(UserConfig.class);

    private static final String DB_PATH_KEY = "db_path";

    public static String getDatabasePath() {
        return prefs.get(DB_PATH_KEY, null);
    }

    public static void setDatabasePath(String path) {
        prefs.put(DB_PATH_KEY, path);
    }

    public static void clearDBPath(){
        prefs.remove(DB_PATH_KEY);
    }
}
