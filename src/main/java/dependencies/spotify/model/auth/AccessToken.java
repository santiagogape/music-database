package dependencies.spotify.model.auth;

import app.model.utilities.Chronometer;
import app.model.utilities.ChronometerListener;

public class AccessToken {

    private final String token;
    private final String type;
    private final Chronometer chronometer;

    public AccessToken(String token, String type) {
        this.token = token;
        this.type = type;
        this.chronometer = new Chronometer(3600);
    }



    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public Chronometer getChronometer() {
        return chronometer;
    }
}
