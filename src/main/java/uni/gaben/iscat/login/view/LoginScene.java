package uni.gaben.iscat.login.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.login.controller.LoginController;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.utils.components.AutoFittingLabel;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.stream.Stream;

/**
 * Schermata di login con input custom e animazioni.
 * Usa AutoFittingLabel invece di TextField per mantenere lo stile unico.
 */
public class LoginScene extends Scene {

    private static final String FONT = "Miracode";

    private final LoginModel model;
    private final LoginController controller;

    private StackPane root;
    private VBox contentBox;

    // Username components
    private AutoFittingLabel usernameLabel;
    private AutoFittingLabel usernamePlaceholder;
    private FontIcon loginIcon;

    // Password components
    private AutoFittingLabel passwordLabel;
    private AutoFittingLabel passwordPlaceholder;
    private FontIcon passwdIcon;

    // Status and cursor
    private AutoFittingLabel statusLabel;
    private Label blinkCursor;

    private boolean isErrorFlashing = false;

    public LoginScene(LoginModel loginModel, LoginController loginController) {
        super(new StackPane(), 800, 600);
        this.model = loginModel;
        this.controller = loginController;

        initStyles();
        initNodes();
        initLayout();
        initBindings();
        setupErrorAnimation();
        initEventFilters();
    }

    private void initStyles() {
        this.root = (StackPane) getRoot();
        var cssUrl = getClass().getResource("/uni/gaben/iscat/styles/login.css");
        if(cssUrl != null) getStylesheets().add(cssUrl.toExternalForm());
    }

    private void initNodes() {
        // Cursore lampeggiante
        blinkCursor = new Label("_");
        blinkCursor.getStyleClass().add("login-cursor");
        blinkCursor.setMouseTransparent(true);

        Timeline blinkAnim = new Timeline(
            new KeyFrame(Duration.millis(530), e -> blinkCursor.setVisible(!blinkCursor.isVisible()))
        );
        blinkAnim.setCycleCount(Animation.INDEFINITE);
        blinkAnim.play();

        // Username
        usernameLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-text");
        usernamePlaceholder = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-placeholder");
        usernamePlaceholder.setText("username");
        usernamePlaceholder.setMouseTransparent(true);
        
        loginIcon = new FontIcon("fas-id-card-alt");
        loginIcon.iconSizeProperty().bind(usernameLabel.baseFontSizeProperty());
        loginIcon.getStyleClass().add("login-icon");
        HBox.setMargin(loginIcon, new Insets(0, 16, 16, 16));

        // Password
        passwordLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], FONT, "login-text");
        passwordPlaceholder = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], FONT, "login-placeholder");
        passwordPlaceholder.setText("**************");
        passwordPlaceholder.setMouseTransparent(true);
        
        passwdIcon = new FontIcon("fas-asterisk");
        passwdIcon.iconSizeProperty().bind(passwordLabel.baseFontSizeProperty());
        passwdIcon.getStyleClass().add("login-icon");
        HBox.setMargin(passwdIcon, new Insets(0, 16, 16, 16));

        // Status
        statusLabel = new AutoFittingLabel(TipografiaAurea.LABEL[TipografiaAurea.LARGE], FONT, "login-text-status");
    }

    private void initLayout() {
        contentBox = new VBox();
        contentBox.setAlignment(Pos.CENTER);

        // Username: HBox(label + cursor) stacked with placeholder
        HBox usernameTextBox = new HBox(usernameLabel);
        usernameTextBox.setAlignment(Pos.CENTER_LEFT);
        StackPane usernameStack = new StackPane(usernamePlaceholder, usernameTextBox);
        usernameStack.setAlignment(Pos.CENTER_LEFT);
        HBox usernameField = new HBox(loginIcon, usernameStack);
        usernameField.setAlignment(Pos.CENTER_LEFT);
        usernameField.setPickOnBounds(true);
        usernameField.setOnMouseClicked(e -> model.setLoginState(false));

        // Password: HBox(label + cursor) stacked with placeholder
        HBox passwordTextBox = new HBox(passwordLabel);
        passwordTextBox.setAlignment(Pos.CENTER_LEFT);
        StackPane passwordStack = new StackPane(passwordPlaceholder, passwordTextBox);
        passwordStack.setAlignment(Pos.CENTER_LEFT);
        HBox passwordField = new HBox(passwdIcon, passwordStack);
        passwordField.setAlignment(Pos.CENTER_LEFT);
        passwordField.setPickOnBounds(true);
        passwordField.setOnMouseClicked(e -> model.setLoginState(true));

        // Cursor management
        model.loginStateProperty().addListener((obs, old, isTypingPass) -> {
            usernameTextBox.getChildren().remove(blinkCursor);
            passwordTextBox.getChildren().remove(blinkCursor);
            blinkCursor.fontProperty().unbind();
            blinkCursor.textFillProperty().unbind();

            if (Boolean.TRUE.equals(isTypingPass)) {
                passwordTextBox.getChildren().add(blinkCursor);
                blinkCursor.fontProperty().bind(passwordLabel.fontProperty());
                blinkCursor.textFillProperty().bind(passwordLabel.textFillProperty());
            } else {
                usernameTextBox.getChildren().add(blinkCursor);
                blinkCursor.fontProperty().bind(usernameLabel.fontProperty());
                blinkCursor.textFillProperty().bind(usernameLabel.textFillProperty());
            }
        });

        // Initial cursor setup
        usernameTextBox.getChildren().add(blinkCursor);
        blinkCursor.fontProperty().bind(usernameLabel.fontProperty());
        blinkCursor.textFillProperty().bind(usernameLabel.textFillProperty());

        contentBox.getChildren().addAll(usernameField, passwordField, statusLabel);
        contentBox.maxWidthProperty().bind(this.widthProperty().multiply(ScalareAureo.IPHI_D));

        Stream.of(usernameLabel, passwordLabel, statusLabel, usernamePlaceholder, passwordPlaceholder).forEach(l -> {
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

        // Hide placeholders when text present
        usernamePlaceholder.visibleProperty().bind(model.usernameProperty().isEmpty());
        passwordPlaceholder.visibleProperty().bind(model.passwordProperty().isEmpty());

        // Dynamic styling
        model.usernameProperty().addListener((obs, old, val) -> updateUsernameStyle());
        model.userExistsProperty().addListener((obs, old, val) -> updateUsernameStyle());
        
        updateUsernameStyle();
        updatePasswordStyle();
    }

    private void updateUsernameStyle() {
        if (isErrorFlashing) return;
        
        usernameLabel.getStyleClass().removeAll("login-text-empty", "login-text-exists", "login-text-missing");
        loginIcon.getStyleClass().removeAll("login-icon-empty", "login-icon-exists", "login-icon-missing");
        
        if (model.getUsername().isEmpty()) {
            usernameLabel.getStyleClass().add("login-text-empty");
            loginIcon.getStyleClass().add("login-icon-empty");
            loginIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        } else if (model.userExistsProperty().get()) {
            usernameLabel.getStyleClass().add("login-text-exists");
            loginIcon.getStyleClass().add("login-icon-exists");
            loginIcon.setIconColor(javafx.scene.paint.Color.LIMEGREEN);
        } else {
            usernameLabel.getStyleClass().add("login-text-missing");
            loginIcon.getStyleClass().add("login-icon-missing");
            loginIcon.setIconColor(javafx.scene.paint.Color.GOLD);
        }
    }
    
    private void updatePasswordStyle() {
        if (isErrorFlashing) return;
        
        passwordLabel.getStyleClass().removeAll("login-text-empty", "login-text-error");
        passwdIcon.getStyleClass().removeAll("login-icon-empty", "login-icon-error");
        
        passwordLabel.getStyleClass().add("login-text-empty");
        passwdIcon.getStyleClass().add("login-icon-empty");
        passwdIcon.setIconColor(javafx.scene.paint.Color.WHITE);
    }

    private void setupErrorAnimation() {
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    isErrorFlashing = true;
                    usernameLabel.getStyleClass().add("login-text-error");
                    passwordLabel.getStyleClass().add("login-text-error");
                    loginIcon.setIconColor(javafx.scene.paint.Color.TOMATO);
                    passwdIcon.setIconColor(javafx.scene.paint.Color.TOMATO);
                }),
                new KeyFrame(Duration.millis(300), e -> {
                    isErrorFlashing = false;
                    usernameLabel.getStyleClass().remove("login-text-error");
                    passwordLabel.getStyleClass().remove("login-text-error");
                    updateUsernameStyle();
                    updatePasswordStyle();
                })
        );

        model.wrongCredentialsProperty().addListener((obs, old, triggered) -> {
            if (Boolean.TRUE.equals(triggered)) {
                flash.playFromStart();
            }
        });
    }

    private void initEventFilters() {
        addEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
    }
}
