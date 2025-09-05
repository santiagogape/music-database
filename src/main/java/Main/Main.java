package Main;

import Main.Database.MainDatabase;
import app.model.utilities.ChronometerListener;
import dependencies.model.SQLite.MusicSQLiteDatabase;

public class Main {


    public static void main(String[] args) {

        /*
        AccessToken token = tokenManager.requestToken();
        token.getChronometer().registerListener(checkChronometer());
        //TODO: control of chronometer an refresh token to then update so called accessToken for the one who calls api
         */
        MainDatabase database = new MainDatabase(new MusicSQLiteDatabase("C:\\Users\\santi\\Desktop","music.db"));


    }



    private static ChronometerListener checkChronometer() {
        return new ChronometerListener() {
            @Override
            public void OnFinish() {
                refreshToken();
            }
            @Override
            public void OnTick() {
                updateTimeLeft();
            }
        };
    }

    private static void updateTimeLeft() {
    }

    private static void refreshToken(){}


}
