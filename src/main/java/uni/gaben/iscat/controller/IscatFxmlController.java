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


}