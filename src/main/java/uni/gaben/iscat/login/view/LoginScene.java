package uni.gaben.iscat.login.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    private final LoginModel model;
    private final LoginController controller;

    private StackPane root;
    private VBox loginContent;
    private AutoFittingLabel usernameLabel;
    private AutoFittingLabel passwordLabel;
    private AutoFittingLabel statusLabel;
    private Label blinkLabel;

    public LoginScene(LoginModel loginModel, LoginController loginController) {
        super(new StackPane());
        this.model = loginModel;
        this.controller = loginController;

        initStyles();
        initNodes();
        initLayout();
        initBindings();
        initCursorLogic();
        initEventFilters();

        // Trigger iniziale
        Platform.runLater(() -> model.setLoginState(false));
    }

    private void initStyles() {
        this.root = (StackPane) getRoot();
        String css = Objects.requireNonNull(getClass().getResource("/uni/gaben/iscat/styles/login.css")).toExternalForm();
        getStylesheets().add(css);
    }

    private void initNodes() {
        // Cursore
        blinkLabel = new Label("|");
        blinkLabel.getStyleClass().add(CSS_TEXT_CLASS);
        blinkLabel.setManaged(false);
        blinkLabel.setMouseTransparent(true);

        Timeline blinkAnim = new Timeline(new KeyFrame(Duration.millis(530), e -> blinkLabel.setVisible(!blinkLabel.isVisible())));
        blinkAnim.setCycleCount(Animation.INDEFINITE);
        blinkAnim.play();

        // Labels
        usernameLabel = new AutoFittingLabel(TipografiaAurea.DISPLAY[TipografiaAurea.MEDIUM], CSS_TEXT_CLASS);
        passwordLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], CSS_TEXT_CLASS);
        statusLabel   = new AutoFittingLabel(TipografiaAurea.LABEL[TipografiaAurea.MEDIUM], CSS_TEXT_STATUS_CLASS);
    }

    private void initLayout() {
        loginContent = new VBox(usernameLabel, passwordLabel, statusLabel);
        loginContent.setAlignment(Pos.CENTER);
        loginContent.setFillWidth(true);

        // Spaziatura aurea: $\phi^{-4}$
        loginContent.spacingProperty().bind(root.heightProperty().multiply(Math.pow(ScalareAureo.IPHI_D, 4)));

        // Vincoli aurei per la VBox
        loginContent.maxWidthProperty().bind(root.widthProperty().multiply(ScalareAureo.IPHI_D));
        loginContent.maxHeightProperty().bind(root.heightProperty().multiply(ScalareAureo.IPHI_D));

        Stream.of(usernameLabel, passwordLabel, statusLabel).forEach(l -> {
            l.setMinWidth(0);
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            l.setLimit(loginContent.maxWidthProperty());
        });

        root.getChildren().addAll(loginContent, blinkLabel);
    }

    private void initBindings() {
        usernameLabel.textProperty().bind(model.usernameProperty());
        passwordLabel.textProperty().bind(model.passwordProperty());
        statusLabel.textProperty().bind(model.statusProperty());
    }

    private void initCursorLogic() {
        model.loginStateProperty().addListener((obs, oldState, isTypingPassword) -> {
            AutoFittingLabel target = isTypingPassword ? passwordLabel : usernameLabel;

            // Definiamo come aggiornare il cursore per il target attuale
            Runnable updater = () -> syncCursor(target);

            // Rilega i trigger del cursore al nuovo target
            target.widthProperty().addListener(e -> Platform.runLater(updater));
            target.textProperty().addListener(e -> Platform.runLater(updater));
            root.widthProperty().addListener(e -> Platform.runLater(updater));

            updater.run();
        });
    }

    private void syncCursor(AutoFittingLabel target) {
        if (target.getScene() == null) return;

        // Convertiamo le coordinate locali della label in coordinate del root (StackPane)
        Bounds boundsInScene = target.localToScene(target.getBoundsInLocal());
        Bounds b = root.sceneToLocal(boundsInScene);

        // Poiché la label è centrata, il testo finisce a: CentroLabel + (MetàLarghezzaTesto)
        double textW = target.getEffectiveTextWidth();
        double centerX = b.getMinX() + (b.getWidth() / 2.0);
        double cursorX = centerX + (textW / 2.0);

        blinkLabel.setLayoutX(cursorX);
        blinkLabel.setLayoutY(b.getMinY());

        // Sincronizza font-size e stile con il target attuale
        blinkLabel.setStyle(target.getStyle());
    }

    private void initEventFilters() {
        addEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
    }
}