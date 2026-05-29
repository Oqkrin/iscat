package uni.gaben.iscat.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Interfaccia che aiuta nel caricamento del FXML nelle scene che lo usano.
 * Include metodi di utilità grafica e "tween" riutilizzabili nei vari menu.
 */
public interface IscatFxmlController {

    void setContentRoot(StackPane contentRoot);

    /**
     * Applica un'icona Ikonli a una Label impostando un contenitore fisso
     * per garantire un allineamento geometrico perfetto (es. icone strette come il fulmine).
     */
    default void applyIconLabel(Label label, String iconCode) {
        if (label == null) return;

        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(24);
        icon.getStyleClass().add("score-icon");

        StackPane iconContainer = new StackPane(icon);
        iconContainer.setMinSize(32, 32);
        iconContainer.setPrefSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.setAlignment(Pos.CENTER);

        label.setGraphic(iconContainer);
        label.setContentDisplay(ContentDisplay.LEFT);
        label.setGraphicTextGap(15);
    }

    /**
     * Applica un'icona Ikonli a un Button impostando un contenitore fisso
     * quadrato. Utile per i pulsanti di controllo o di navigazione nei menu.
     */
    default void applyIconButton(Button button, String iconCode) {
        if (button == null) return;

        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(20);
        icon.getStyleClass().add("button-icon");

        StackPane iconContainer = new StackPane(icon);
        iconContainer.setMinSize(28, 28);
        iconContainer.setPrefSize(28, 28);
        iconContainer.setMaxSize(28, 28);
        iconContainer.setAlignment(Pos.CENTER);

        button.setGraphic(iconContainer);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(10);
    }
    /**
     * Configura un effetto di micro-animazione (pop-out e traslazione)
     * quando il mouse entra o esce da un pulsante.
     * Accetta qualsiasi Node, ma è ottimizzato per i Button del gioco.
     */
    default void setupButtonHoverTween(Node button) {
        if (button == null) return;

        TranslateTransition translate = new TranslateTransition(Duration.millis(120), button);
        ScaleTransition scale = new ScaleTransition(Duration.millis(120), button);

        translate.setInterpolator(Interpolator.EASE_OUT);
        scale.setInterpolator(Interpolator.EASE_OUT);

        button.setOnMouseEntered(e -> {
            translate.stop();
            scale.stop();

            translate.setToX(8.0);
            scale.setToX(1.03);
            scale.setToY(1.03);

            translate.play();
            scale.play();
        });

        button.setOnMouseExited(e -> {
            translate.stop();
            scale.stop();

            translate.setToX(0.0);
            scale.setToX(1.0);
            scale.setToY(1.0);

            translate.play();
            scale.play();
        });
    }

    /**
     * Esegue un'animazione di spawn fluida (combinazione di Scale e Fade)
     * su un determinato componente grafico (es. preview box, intere card o elementi di UI).
     */
    default void playSpawnTween(Node node) {
        if (node == null) return;

        node.setScaleX(0.6);
        node.setScaleY(0.6);
        node.setOpacity(0.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(250), node);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.SPLINE(0.1, 1.0, 0.2, 1.0));

        FadeTransition fade = new FadeTransition(Duration.millis(150), node);
        fade.setToValue(1.0);

        ParallelTransition tweenGroup = new ParallelTransition(scale, fade);
        tweenGroup.play();
    }
}