package uni.gaben.iscat.iscat_screens.login;

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
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;
import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.iscat_m_view_c.AutoFittingLabel;
import uni.gaben.iscat.iscat_screens.login.model.LoginModel;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.stream.Stream;

/**
 * Schermata di login con componenti tipografici ed animazioni custom.
 * Utilizza AutoFittingLabel al posto dei TextField per garantire uno stile uniforme.
 */
public class LoginView extends AbstractIscatStackPane {

    private static final String FONT = "Miracode";

    private final LoginModel model;
    private final LoginController controller;

    private final StackPane root;
    private VBox contentBox;

    // Elementi grafici Username
    private AutoFittingLabel welcomeTitle;
    private AutoFittingLabel usernameLabel;
    private AutoFittingLabel usernamePlaceholder;
    private FontIcon loginIcon;

    // Elementi grafici Password
    private AutoFittingLabel passwordLabel;
    private AutoFittingLabel passwordPlaceholder;
    private FontIcon passwdIcon;

    // Cursore stile Terminale Retro e Feedback di stato
    private AutoFittingLabel statusLabel;
    private Label blinkCursor;
    private Timeline blinkAnimation;

    private boolean isErrorFlashing = false;

    private AutoFittingLabel loggedInUserLabel;
    private HBox usernameField;
    private HBox passwordField;

    public LoginView(LoginController loginController) {
        super(new StackPane(), true); // Attiva lo sfondo animato stellato interattivo
        this.controller = loginController;
        this.model = loginController.getLoginModel();
        this.root = getContentRoot();

        root.setStyle("-fx-background-color: transparent;");
        initialize();
    }

    @Override
    protected void initStyles() {
        getStylesheets().add(getClass().getResource("/uni/gaben/iscat/styles/scenes/login-menu.css").toExternalForm());
    }

    @Override
    protected void initNodes() {
        welcomeTitle = new AutoFittingLabel(TipografiaAurea.DISPLAY[TipografiaAurea.LARGE], FONT, "login-title");
        welcomeTitle.setText("WELCOME TO ISCAT");

        blinkCursor = new Label("_");
        blinkCursor.getStyleClass().add("login-cursor");
        blinkCursor.setMouseTransparent(true);

        // Username Set
        usernameLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-text");
        usernamePlaceholder = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-placeholder");
        usernamePlaceholder.setText("Nome Utente");
        usernamePlaceholder.setMouseTransparent(true);

        loginIcon = new FontIcon("fas-id-card-alt");
        loginIcon.iconSizeProperty().bind(usernameLabel.baseFontSizeProperty());
        loginIcon.getStyleClass().add("login-icon");
        HBox.setMargin(loginIcon, new Insets(0, 16, 16, 16));

        // Password Set
        passwordLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], FONT, "login-text");
        passwordPlaceholder = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.MEDIUM], FONT, "login-placeholder");
        passwordPlaceholder.setText("Password");
        passwordPlaceholder.setMouseTransparent(true);

        passwdIcon = new FontIcon("fas-asterisk");
        passwdIcon.iconSizeProperty().bind(passwordLabel.baseFontSizeProperty());
        passwdIcon.getStyleClass().add("login-icon");
        HBox.setMargin(passwdIcon, new Insets(0, 16, 16, 16));

        statusLabel = new AutoFittingLabel(TipografiaAurea.LABEL[TipografiaAurea.LARGE], FONT, "login-text-status");

        loggedInUserLabel = new AutoFittingLabel(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE], FONT, "login-user-success");
        loggedInUserLabel.setOpacity(0);
        loggedInUserLabel.setManaged(false);
    }

    @Override
    protected void initLayout() {
        contentBox = new VBox();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setStyle("-fx-background-color: transparent;");

        // Layout Struttura Username
        HBox usernameTextBox = new HBox(usernameLabel);
        usernameTextBox.setAlignment(Pos.CENTER_LEFT);

        StackPane usernameStack = new StackPane(usernamePlaceholder, usernameTextBox);
        usernameStack.setAlignment(Pos.CENTER_LEFT);

        usernameField = new HBox(loginIcon, usernameStack);
        usernameField.setAlignment(Pos.CENTER_LEFT);
        usernameField.setPickOnBounds(true);
        usernameField.setOnMouseClicked(e -> model.setLoginState(false));

        // Layout Struttura Password
        HBox passwordTextBox = new HBox(passwordLabel);
        passwordTextBox.setAlignment(Pos.CENTER_LEFT);

        StackPane passwordStack = new StackPane(passwordPlaceholder, passwordTextBox);
        passwordStack.setAlignment(Pos.CENTER_LEFT);

        passwordField = new HBox(passwdIcon, passwordStack);
        passwordField.setAlignment(Pos.CENTER_LEFT);
        passwordField.setPickOnBounds(true);
        passwordField.setOnMouseClicked(e -> model.setLoginState(true));

        // Spostamento reattivo del cursore a schermo tra i due campi
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

        // Setup iniziale del cursore lampeggiante sul primo campo
        usernameTextBox.getChildren().add(blinkCursor);
        blinkCursor.fontProperty().bind(usernameLabel.fontProperty());
        blinkCursor.textFillProperty().bind(usernameLabel.textFillProperty());

        StackPane headerStack = new StackPane(welcomeTitle, loggedInUserLabel);
        headerStack.setAlignment(Pos.CENTER);

        contentBox.getChildren().addAll(headerStack, usernameField, passwordField, statusLabel);
        contentBox.maxWidthProperty().bind(this.widthProperty().multiply(ScalareAureo.IPHI_D));

        welcomeTitle.setAlignment(Pos.CENTER);
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

        usernamePlaceholder.visibleProperty().bind(model.usernameProperty().isEmpty());
        passwordPlaceholder.visibleProperty().bind(model.passwordProperty().isEmpty());

        model.usernameProperty().addListener((obs, old, val) -> updateUsernameStyle());
        model.userExistsProperty().addListener((obs, old, val) -> updateUsernameStyle());

        updateUsernameStyle();
        updatePasswordStyle();
    }

    @Override
    protected void initEventHandlers() {
        // Registrati via filtri di scena globali in onShow()
    }

    @Override
    protected void initAnimations() {
        // Animazione Intermittenza Cursore (_)
        blinkAnimation = new Timeline(
                new KeyFrame(Duration.millis(530), e -> blinkCursor.setVisible(!blinkCursor.isVisible()))
        );
        blinkAnimation.setCycleCount(Animation.INDEFINITE);

        // Flash cromatico in caso di errore
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
        controller.reset();
        usernameField.setOpacity(1);
        passwordField.setOpacity(1);
        statusLabel.setOpacity(1);
        loggedInUserLabel.setOpacity(0);
        welcomeTitle.setTranslateY(0);
        loggedInUserLabel.setTranslateY(0);
        super.onShow();

        if (blinkAnimation != null) blinkAnimation.play();
        if (getScene() != null) {
            getScene().addEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
            getScene().addEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        if (blinkAnimation != null) {
            blinkAnimation.pause();
        }
        if (getScene() != null) {
            getScene().removeEventFilter(KeyEvent.KEY_PRESSED, controller::onKeyPressed);
            getScene().removeEventFilter(KeyEvent.KEY_TYPED, controller::onKeyTyped);
        }
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

    private void playLoginSuccessAnimation() {
        // 1. Spariscono i vecchi nodi di inserimento dati
        FadeTransition fadeOutU = new FadeTransition(Duration.millis(400), usernameField);
        fadeOutU.setToValue(0);
        FadeTransition fadeOutP = new FadeTransition(Duration.millis(400), passwordField);
        fadeOutP.setToValue(0);
        FadeTransition fadeOutS = new FadeTransition(Duration.millis(400), statusLabel);
        fadeOutS.setToValue(0);

        // 2. Il titolo scorre verso l'alto
        TranslateTransition moveTitle = new TranslateTransition(Duration.millis(600), welcomeTitle);
        moveTitle.setToY(-20);
        moveTitle.setInterpolator(Interpolator.EASE_BOTH);

        // 3. Il nome utente compare scorrendo verso il centro (Estratto dal SessionManager)
        loggedInUserLabel.setText(model.getUsername());

        TranslateTransition moveLabel = new TranslateTransition(Duration.millis(600), loggedInUserLabel);
        moveLabel.setToY(400);
        moveLabel.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeInUser = new FadeTransition(Duration.millis(500), loggedInUserLabel);
        fadeInUser.setFromValue(0);
        fadeInUser.setToValue(1);
        fadeInUser.setDelay(Duration.millis(200));

        // 4. Esecuzione simultanea dei movimenti e navigazione finale coordinata
        ParallelTransition successAnim = new ParallelTransition(
                fadeOutU, fadeOutP, fadeOutS, moveTitle, moveLabel, fadeInUser
        );

        successAnim.setOnFinished(e -> IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU));
        successAnim.play();
    }
}