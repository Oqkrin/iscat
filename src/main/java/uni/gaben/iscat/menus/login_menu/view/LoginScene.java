package uni.gaben.iscat.menus.login_menu.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatSceneAbstract;
import uni.gaben.iscat.menus.login_menu.controller.LoginController;
import uni.gaben.iscat.menus.login_menu.model.LoginModel;
import uni.gaben.iscat.utils.components.AutoFittingLabel;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.stream.Stream;

/**
 * Schermata di login con input custom e animazioni.
 * Usa AutoFittingLabel invece di TextField per mantenere lo stile unico.
 */
public class LoginScene extends IscatSceneAbstract {

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
    private Timeline blinkAnimation;

    private boolean isErrorFlashing = false;

    public LoginScene(LoginModel loginModel, LoginController loginController) {
        super(new StackPane(), true); // Enable starry background
        this.model = loginModel;
        this.controller = loginController;
        this.root = getContentRoot();
        
        // Make root transparent so stars show through
        root.setStyle("-fx-background-color: transparent;");
        
        initialize();
    }

    @Override
    protected void initStyles() {
        var cssUrl = getClass().getResource("/uni/gaben/iscat/styles/login.css");
        if (cssUrl != null) {
            getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    @Override
    protected void initNodes() {
        // Cursore lampeggiante
        blinkCursor = new Label("_");
        blinkCursor.getStyleClass().add("login-cursor");
        blinkCursor.setMouseTransparent(true);

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

    @Override
    protected void initLayout() {
        contentBox = new VBox();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setStyle("-fx-background-color: transparent;"); // Ensure stars show through

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

    @Override
    protected void initBindings() {
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

    @Override
    protected void initEventHandlers() {
        addEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
    }

    @Override
    protected void initAnimations() {
        // Cursor blink animation
        blinkAnimation = new Timeline(
            new KeyFrame(Duration.millis(530), e -> blinkCursor.setVisible(!blinkCursor.isVisible()))
        );
        blinkAnimation.setCycleCount(Animation.INDEFINITE);
        
        // Error flash animation
        Timeline errorFlash = new Timeline(
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
                errorFlash.playFromStart();
            }
        });
    }

    @Override
    public void onShow() {
        // Avvia animazione cursore quando la scena diventa visibile
        if (blinkAnimation != null) {
            blinkAnimation.play();
        }
        // Enable mouse-following mode for starry background
        if (getStarryBackground() != null) {
            getStarryBackground().setFollowMouse(true);
            // Track mouse at scene level to ensure we capture all movement
            setOnMouseMoved(e -> getStarryBackground().updateMousePosition(e.getSceneX(), e.getSceneY()));
        }
    }

    @Override
    public void onHide() {
        // Ferma animazione cursore quando la scena viene nascosta
        if (blinkAnimation != null) {
            blinkAnimation.pause();
        }
        // Clean up mouse handler
        setOnMouseMoved(null);
    }

    // --- Helper methods ---

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
}
