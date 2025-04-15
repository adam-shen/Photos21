/**
 * Photos is the main entry point for the Photos application.
 *
 * <p>
 * This class extends the JavaFX Application class and is responsible for bootstrapping the application.
 * It initially loads the login view from an FXML file and sets up the primary scene. The static method
 * setRoot() is provided for seamless transitions between different views by loading the corresponding FXML files.
 * The overall design of the application follows an MVC architecture, where FXML files define the view,
 * controllers handle user interactions, and models represent application data.
 * </p>
 *
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */
package app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point.
 */
public class Photos extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Load login.fxml first
        scene = new Scene(loadFXML("login"), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Photos - Login");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Photos.class.getResource("/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
