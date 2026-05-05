package uni.gaben.iscat.login.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.utils.components.AutoFittingLabel;
import uni.gaben.iscat.utils.rapporto_aureo.ScalareAureo;
import uni.gaben.iscat.utils.rapporto_aureo.TipografiaAurea;

import java.util.stream.Stream;

public class LoginScene extends Scene {

    private static final String FONT = "Miracode";
    private static final String CSS_TEXT_CLASS = "login-text";
    private static final String CSS_TEXT_STATUS_CLASS = "login-text-status";

    // Colori tematici
    private static final Color COL_EMPTY   = Color.WHITE;
    private static final Color COL_EXISTS  = Color.LIMEGREEN;
    private static final Color COL_MISSING = Color.GOLD;
    private static final Color COL_ERROR   = Color.TOMATO;

    private final LoginModel model;
    private final LoginController controller;

    private StackPane root;
    private VBox contentBox;

    private AutoFittingLabel usernameLabel;
    private HBox usernameField;
    private FontIcon loginIcon;

    private AutoFittingLabel passwordLabel;
    private HBox passwordField;
    private FontIcon passwdIcon;

    private AutoFittingLabel statusLabel;
    private Label blinkLabel;

    private final ObjectProperty<Paint> errorFlashColor = new SimpleObjectProperty<>(null);

    public LoginScene(LoginModel loginModel, LoginController loginController) {
        super(new StackPane(), 800, 600);
        this.model = loginModel;
        this.controller = loginController;

        initStyles();
        initNodes();
        initLayout();
        initBindings();
        initEventFilters();
        setupErrorAnimation();
    }

    private void initStyles() {
        this.root = (StackPane) getRoot();
        var cssUrl = getClass().getResource("/uni/gaben/iscat/styles/login.css");
        if(cssUrl != null) getStylesheets().add(cssUrl.toExternalForm());
    }

    private void initNodes() {
        // Cursore pulsante
        blinkLabel = new Label("_");
        blinkLabel.getStyleClass().add(CSS_TEXT_CLASS);
        blinkLabel.setMouseTransparent(true);

        Timeline blinkAnim = new Timeline(new KeyFrame(Duration.millis(530), e -> blinkLabel.setVisible(!blinkLabel.isVisible())));
        blinkAnim.setCycleCount(Animation.INDEFINITE);
        blinkAnim.play();

        usernameLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, CSS_TEXT_CLASS);
        passwordLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], FONT, CSS_TEXT_CLASS);
        statusLabel   = new AutoFittingLabel(TipografiaAurea.LABEL[TipografiaAurea.LARGE], FONT, CSS_TEXT_STATUS_CLASS);

        loginIcon = new FontIcon("fas-id-card-alt");
        loginIcon.iconSizeProperty().bind(usernameLabel.baseFontSizeProperty());
        HBox.setMargin(loginIcon, new Insets(0, 16, 16, 16));

        passwdIcon = new FontIcon("fas-asterisk");
        passwdIcon.iconSizeProperty().bind(passwordLabel.baseFontSizeProperty());
        HBox.setMargin(passwdIcon, new Insets(0, 16, 16, 16));
    }

    private void initLayout() {
        contentBox = new VBox(); // Spaziatura aurea leggermente più ampia
        contentBox.setAlignment(Pos.CENTER);

        usernameField = new HBox(loginIcon, usernameLabel);
        usernameField.setAlignment(Pos.CENTER_LEFT);
        usernameField.setPickOnBounds(true); // Fondamentale per il click su spazi vuoti

        passwordField = new HBox(passwdIcon, passwordLabel);
        passwordField.setAlignment(Pos.CENTER_LEFT);
        passwordField.setPickOnBounds(true);

        // Click sui campi per cambiare focus nel modello
        usernameField.setOnMouseClicked(e -> model.setLoginState(false));
        passwordField.setOnMouseClicked(e -> model.setLoginState(true));

        // Sync del cursore (il tuo codice originale, con pulizia binding)
        model.loginStateProperty().addListener((obs, old, isTypingPass) -> {
            usernameField.getChildren().remove(blinkLabel);
            passwordField.getChildren().remove(blinkLabel);
            blinkLabel.fontProperty().unbind();

            if (isTypingPass) {
                passwordField.getChildren().add(blinkLabel);
                blinkLabel.fontProperty().bind(passwordLabel.fontProperty());
            } else {
                usernameField.getChildren().add(blinkLabel);
                blinkLabel.fontProperty().bind(usernameLabel.fontProperty());
            }
        });

        // Setup iniziale cursore
        usernameField.getChildren().add(blinkLabel);
        blinkLabel.fontProperty().bind(usernameLabel.fontProperty());

        contentBox.getChildren().addAll(usernameField, passwordField, statusLabel);

        contentBox.maxWidthProperty().bind(this.widthProperty().multiply(ScalareAureo.IPHI_D));

        Stream.of(usernameLabel, passwordLabel, statusLabel).forEach(l -> {
            l.setAlignment(Pos.CENTER_LEFT);
            l.setLimit(contentBox.maxWidthProperty());
            l.setEllipsisString("");
        });

        root.getChildren().add(contentBox);
    }

    private void initBindings() {
        usernameLabel.textProperty().bind(model.usernameProperty());
        passwordLabel.textProperty().bind(model.passwordProperty());
        statusLabel.textProperty().bind(model.statusProperty());

        //BINDING COLORI DINAMICI
        var userColorBinding = Bindings.createObjectBinding(() -> {
            if (errorFlashColor.get() != null) return errorFlashColor.get();
            if (model.getUsername().isEmpty()) return COL_EMPTY;
            return model.userExistsProperty().get() ? COL_EXISTS : COL_MISSING;
        }, model.usernameProperty(), model.userExistsProperty(), errorFlashColor);

        usernameLabel.textFillProperty().bind(userColorBinding);
        loginIcon.iconColorProperty().bind(userColorBinding);

        blinkLabel.textFillProperty().bind(Bindings.createObjectBinding(() -> model.getLoginState() ? passwordLabel.getTextFill() : usernameLabel.getTextFill()
                , model.loginStateProperty(), usernameLabel.textFillProperty(), passwordLabel.textFillProperty()));

        // Colore Password: Errore > Vuoto > Bianco
        var passColorBinding = Bindings.createObjectBinding(() -> {
            if (errorFlashColor.get() != null) return errorFlashColor.get();
            return COL_EMPTY;
        }, model.passwordProperty(), errorFlashColor);

        passwordLabel.textFillProperty().bind(passColorBinding);
        passwdIcon.iconColorProperty().bind(passColorBinding);
    }

    private void setupErrorAnimation() {
        // Se il controller triggera "wrongCredentials", facciamo lampeggiare tutto in rosso
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> errorFlashColor.set(COL_ERROR)),
                new KeyFrame(Duration.millis(300), e -> errorFlashColor.set(null))
        );

        // Assumiamo model.wrongCredentialsProperty() (BooleanProperty)
        model.wrongCredentialsProperty().addListener((obs, old, triggered) -> {
            if (triggered) {
                flash.playFromStart();
            }
        });
    }

    private void initEventFilters() {
        addEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
    }
}