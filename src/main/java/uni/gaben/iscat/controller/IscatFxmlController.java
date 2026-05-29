package uni.gaben.iscat.controller;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Interfaccia che aiuta nel caricamento del FXML nelle scene che lo usano.
 * Include metodi di utilità grafica riutilizzabili nei vari menu.
 */
public interface IscatFxmlController {

    void setContentRoot(StackPane contentRoot);

    /**
     * Applica un'icona Ikonli a una Label impostando un contenitore fisso
     * per garantire un allineamento geometrico perfetto (es. icone strette come il fulmine).
     */
    default void applyIconLabel(Label label, String iconCode) {
        if (label == null) return;

        // Istanza dell'icona con classe CSS per i colori del tema
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(24);
        icon.getStyleClass().add("score-icon");

        // Scudo spaziale: StackPane quadrato fisso 32x32 per centrare l'icona
        StackPane iconContainer = new StackPane(icon);
        iconContainer.setMinSize(32, 32);
        iconContainer.setPrefSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.setAlignment(Pos.CENTER);

        // Configurazione della Label
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
}