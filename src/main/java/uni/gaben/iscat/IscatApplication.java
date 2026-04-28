package uni.gaben.iscat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class IscatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(IscatApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), stage.getMaxWidth()*IscatApplicationSettings.IPHI, 240);
        stage.setTitle("ISCAT!");
        stage.setScene(scene);
        stage.show();
    }
}
