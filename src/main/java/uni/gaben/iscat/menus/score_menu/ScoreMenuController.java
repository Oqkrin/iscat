package uni.gaben.iscat.menus.score_menu;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

import java.util.Objects;

public class ScoreMenuController {

    public BorderPane rootPane;

    @FXML
    private void handleBack(ActionEvent event) {
        ImageView blackOverlay = new ImageView(
                new Image(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream("/uni/gaben/iscat/sprites/black.png")
                        )
                )
        );
        blackOverlay.setFitWidth(5000);
        blackOverlay.setFitHeight(5000);

        blackOverlay.setOpacity(0);
        rootPane.getChildren().add(blackOverlay);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5),blackOverlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        fadeIn.setOnFinished(e -> {
                    IscatNavigator.getInstance().navigateTo(IscatScenes.MAIN_MENU);
                    rootPane.getChildren().remove(blackOverlay);
                }
        );
    }
}
