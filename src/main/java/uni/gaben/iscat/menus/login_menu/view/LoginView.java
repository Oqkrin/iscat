package uni.gaben.iscat.menus.login_menu.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.AbstractIscatStackPane;
import uni.gaben.iscat.IscatScenes;
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
public class LoginView extends AbstractIscatStackPane {

    private static final String FONT = "Miracode";

    private final LoginModel model;
    private final LoginController controller;

    private StackPane root;
    private VBox contentBox;

    // Username components
    private AutoFittingLabel welcomeTitle;
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

    private AutoFittingLabel loggedInUserLabel;
    private HBox usernameField;
    private HBox passwordField;

    public LoginView(LoginModel loginModel, LoginController loginController) {
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
        // Titolo
        welcomeTitle = new AutoFittingLabel(TipografiaAurea.DISPLAY[TipografiaAurea.MEDIUM], FONT, "login-title");
        welcomeTitle.setText("WELCOME TO ISCAT");

        // Cursore lampeggiante
        blinkCursor = new Label("_");
        blinkCursor.getStyleClass().add("login-cursor");
        blinkCursor.setMouseTransparent(true);

        // Username
        usernameLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-text");
        usernamePlaceholder = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-placeholder");
        usernamePlaceholder.setText("Nome Utente");
        usernamePlaceholder.setMouseTransparent(true);

        loginIcon = new FontIcon("fas-id-card-alt");
        loginIcon.iconSizeProperty().bind(usernameLabel.baseFontSizeProperty());
        loginIcon.getStyleClass().add("login-icon");
        HBox.setMargin(loginIcon, new Insets(0, 16, 16, 16));

        // Password
        passwordLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], FONT, "login-text");
        passwordPlaceholder = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], FONT, "login-placeholder");
        passwordPlaceholder.setText("Password");
        passwordPlaceholder.setMouseTransparent(true);

        passwdIcon = new FontIcon("fas-asterisk");
        passwdIcon.iconSizeProperty().bind(passwordLabel.baseFontSizeProperty());
        passwdIcon.getStyleClass().add("login-icon");
        HBox.setMargin(passwdIcon, new Insets(0, 16, 16, 16));

        // Status
        statusLabel = new AutoFittingLabel(TipografiaAurea.LABEL[TipografiaAurea.LARGE], FONT, "login-text-status");

        // Label che apparirà dopo il login
        loggedInUserLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-user-success");
        loggedInUserLabel.setOpacity(0); // Invisibile all'inizio
        loggedInUserLabel.setManaged(false);
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

        usernameField = new HBox(loginIcon, usernameStack);
        usernameField.setAlignment(Pos.CENTER_LEFT);
        usernameField.setPickOnBounds(true);
        usernameField.setOnMouseClicked(e -> model.setLoginState(false));

        // Password: HBox(label + cursor) stacked with placeholder
        HBox passwordTextBox = new HBox(passwordLabel);
        passwordTextBox.setAlignment(Pos.CENTER_LEFT);

        StackPane passwordStack = new StackPane(passwordPlaceholder, passwordTextBox);
        passwordStack.setAlignment(Pos.CENTER_LEFT);

        passwordField = new HBox(passwdIcon, passwordStack);
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

        StackPane headerStack = new StackPane(welcomeTitle, loggedInUserLabel);
        headerStack.setAlignment(Pos.CENTER);

        contentBox.getChildren().addAll(headerStack, loggedInUserLabel, usernameField, passwordField, statusLabel);
        contentBox.maxWidthProperty().bind(this.widthProperty().multiply(ScalareAureo.IPHI_D));

        // Title settings
        welcomeTitle.setAlignment(Pos.CENTER); // Se lo vuoi centrato
        welcomeTitle.maxWidthProperty().bind(contentBox.maxWidthProperty());
        welcomeTitle.setLimit(contentBox.maxWidthProperty());

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
        // visto che ora la classe extende StackPane non possiamo assegnare qui ma in onShow
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

        model.isLoggedInProperty().addListener((obs, old, loggedIn) -> {
            if (Boolean.TRUE.equals(loggedIn)) {
                playLoginSuccessAnimation();
            }
        });
    }

    @Override
    public void onShow() {
        // Esegue la logica centralizzata (fadeIn e tracciamento mouse delle stelle)
        super.onShow();
        // Avvia animazione cursore quando la scena diventa visibile
        if (blinkAnimation != null) {
            blinkAnimation.play();
        }
        // Aggancia i listener di input globali
        if (getScene() != null) {
            getScene().addEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
            getScene().addEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
        }
    }

    @Override
    public void onHide() {
        // Esegue la pulizia centralizzata dei listener del mouse
        super.onHide();
        // Ferma animazione cursore quando la scena viene nascosta
        if (blinkAnimation != null) {
            blinkAnimation.pause();
        }
        // Rimuove i filtri della tastiera
        if (getScene() != null) {
            getScene().removeEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
            getScene().removeEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
        }
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

    private void playLoginSuccessAnimation() {
        // 1. Spariscono i campi (Username/Password/Status)
        FadeTransition fadeOutU = new FadeTransition(Duration.millis(400), usernameField);
        fadeOutU.setToValue(0);
        FadeTransition fadeOutP = new FadeTransition(Duration.millis(400), passwordField);
        fadeOutP.setToValue(0);
        FadeTransition fadeOutS = new FadeTransition(Duration.millis(400), statusLabel);
        fadeOutS.setToValue(0);

        // 2. Il titolo sale leggermente o scende (decidi tu l'offset)
        TranslateTransition moveTitle = new TranslateTransition(Duration.millis(600), welcomeTitle);
        moveTitle.setToY(-20); // Lo alziamo un po' per fare spazio sotto
        moveTitle.setInterpolator(Interpolator.EASE_BOTH);

        // 3. Appare il nome utente scendendo
        loggedInUserLabel.setText(model.getUsername());
        // NON USARE setManaged(true) qui!

        TranslateTransition moveLabel = new TranslateTransition(Duration.millis(600), loggedInUserLabel);
        moveLabel.setToY(400);   // Scende sotto il titolo
        moveLabel.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeInUser = new FadeTransition(Duration.millis(500), loggedInUserLabel);
        fadeInUser.setFromValue(0);
        fadeInUser.setToValue(1);
        fadeInUser.setDelay(Duration.millis(200));

        // 4. Esecuzione
        ParallelTransition successAnim = new ParallelTransition(
                fadeOutU, fadeOutP, fadeOutS, moveTitle, moveLabel, fadeInUser
        );

        successAnim.setOnFinished(e -> {
            IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, root);
        });

        successAnim.play();
    }
}
