package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setScene(scene);
        stage.setTitle("Demo JavaFX + SQLite (FXML)");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
