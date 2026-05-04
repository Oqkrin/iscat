package uni.gaben.iscat.login.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.util.Duration;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.utils.components.AutoFittingLabel;
import uni.gaben.iscat.utils.rapporto_aureo.ScalareAureo;
import uni.gaben.iscat.utils.rapporto_aureo.TipografiaAurea;

import java.util.Objects;
import java.util.stream.Stream;

public class LoginScene extends Scene {

    private static final String CSS_TEXT_CLASS = "login-text";
    private static final String CSS_TEXT_STATUS_CLASS = "login-text-status";

    public LoginScene(LoginModel loginModel, LoginController loginController) {
        // 1. Usiamo StackPane direttamente come Root
        super(new StackPane());
        StackPane root = (StackPane) getRoot();

        String css = Objects.requireNonNull(getClass().getResource("/uni/gaben/iscat/styles/login.css")).toExternalForm();
        getStylesheets().add(css);

        // 2. Cursore (Blinking Caret)
        Label blinkLabel = new Label("|");
        blinkLabel.getStyleClass().add(CSS_TEXT_CLASS);
        blinkLabel.setManaged(false); // <--- Esclude il cursore dalle logiche del layout automatico
        blinkLabel.setMouseTransparent(true);

        Timeline blink = new Timeline(new KeyFrame(Duration.millis(530), e -> blinkLabel.setVisible(!blinkLabel.isVisible())));
        blink.setCycleCount(Animation.INDEFINITE);
        blink.play();

        // 3. Componenti
        AutoFittingLabel usernameLabel = new AutoFittingLabel(TipografiaAurea.DISPLAY[TipografiaAurea.MEDIUM], CSS_TEXT_CLASS);
        AutoFittingLabel passwordLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], CSS_TEXT_CLASS);
        AutoFittingLabel statusLabel   = new AutoFittingLabel(TipografiaAurea.LABEL[TipografiaAurea.MEDIUM], CSS_TEXT_STATUS_CLASS);

        // 4. Layout VBox
        VBox loginContent = new VBox(usernameLabel, passwordLabel, statusLabel);
        loginContent.setAlignment(Pos.CENTER);
        loginContent.setFillWidth(true); // <--- Forza le label a seguire la larghezza della VBox

        // Spaziatura aurea
        loginContent.spacingProperty().bind(root.heightProperty().multiply(Math.pow(ScalareAureo.IPHI_D, 4)));

        // Vincoli della VBox (Rapporto Aureo)
        loginContent.maxWidthProperty().bind(root.widthProperty().multiply(ScalareAureo.IPHI_D));
        loginContent.maxHeightProperty().bind(root.heightProperty().multiply(ScalareAureo.IPHI_D));

        Stream.of(usernameLabel, passwordLabel, statusLabel).forEach(l -> {
            l.setEllipsisString("");
            l.setMinWidth(0); // <--- Permette alla label di rimpicciolirsi sotto la dimensione del testo
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER); // Mantiene il testo centrato se la label si espande
            // Il limite è la larghezza della VBox
            l.setLimit(loginContent.maxWidthProperty());
        });

        // Aggiungiamo al root: prima il contenuto, poi il cursore sopra
        root.getChildren().addAll(loginContent, blinkLabel);

        // 5. Logica del Cursore (Coordinate Assolute)
        // Usiamo un trucco: invece di bindare layoutX, osserviamo i bounds reali della label
        loginModel.loginStateProperty().addListener((o, old, isTypingPassword) -> {
            AutoFittingLabel target = isTypingPassword ? passwordLabel : usernameLabel;

            // Ogni volta che la finestra cambia, la label cambia testo o il font scala, riposizioniamo
            Runnable updateCursor = () -> {
                // Calcoliamo la fine del testo visualizzato
                // getMaxX() del testo rispetto alla scena
                double x = target.localToScene(target.getBoundsInLocal()).getMaxX();
                double y = target.localToScene(target.getBoundsInLocal()).getMinY();

                blinkLabel.setLayoutX(x);
                blinkLabel.setLayoutY(y);
                blinkLabel.setStyle(target.getStyle()); // Sincronizza la dimensione font
            };

            // Bindiamo il movimento a qualsiasi cosa possa spostare il testo
            target.widthProperty().addListener(e -> updateCursor.run());
            target.textProperty().addListener(e -> updateCursor.run());
            root.widthProperty().addListener(e -> updateCursor.run());
            root.heightProperty().addListener(e -> updateCursor.run());

            updateCursor.run();
        });

        // 6. Binding Dati
        usernameLabel.textProperty().bind(loginModel.usernameProperty());
        passwordLabel.textProperty().bind(loginModel.passwordProperty());
        statusLabel.textProperty().bind(loginModel.statusProperty());

        // Inizializzazione posizione cursore
        Platform.runLater(() -> loginModel.setLoginState(false));

        addEventFilter(KeyEvent.KEY_PRESSED, loginController::onKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, loginController::onKeyTyped);
    }
}