package uni.gaben.iscat.controller.menus;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.view.components.AutoFittingInputBinder;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.model.login.LoginAuth;
import uni.gaben.iscat.model.login.LoginModel;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.List;

public class LoginMenuController implements IscatFxmlController {

    @FXML private StackPane rootPane;
    @FXML private VBox      contentBox;
    @FXML private StackPane headerStack;
    @FXML private Label     welcomeTitle;
    @FXML private Label     loggedInUserLabel;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private FontIcon  loginIcon;
    @FXML private FontIcon  passwdIcon;
    @FXML private Label     statusLabel;

    private LoginModel model;
    private LoginAuth  loginAuth;
    private ScoreDAO   scoreDAO;
    private SettingsDAO settingsDAO;

    private boolean  isErrorFlashing = false;

    @FXML
    private void initialize() {
    }

    public void setup() {
        if (this.model == null) {
            this.model = new LoginModel();
        }
        this.loginAuth   = new LoginAuth(IscatDB.getInstance().getUserDAO());
        this.scoreDAO    = IscatDB.getInstance().getScoreDAO();
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();

        final int MAX_INPUT_LENGTH = 30;

        AutoFittingInputBinder usernameAutofit = new AutoFittingInputBinder(
                usernameField, "Miracode", 24.0, 12.0, MAX_INPUT_LENGTH
        );
        AutoFittingInputBinder passwordAutofit = new AutoFittingInputBinder(
                passwordField, "Miracode", 24.0, 12.0, MAX_INPUT_LENGTH
        );

        var fieldLimit = rootPane.widthProperty().multiply(0.75).subtract(60);
        usernameAutofit.bindLimit(fieldLimit);
        passwordAutofit.bindLimit(fieldLimit);

        initBindings();
        initModelListeners();
    }

    private void initBindings() {
        contentBox.maxWidthProperty().bind(rootPane.widthProperty().multiply(ScalareAureo.IPHI_D));
        welcomeTitle.maxWidthProperty().bind(contentBox.maxWidthProperty());

        usernameField.textProperty().bindBidirectional(model.usernameProperty());
        passwordField.textProperty().bindBidirectional(model.passwordProperty());
        statusLabel.textProperty().bind(model.statusProperty());
    }

    private void initModelListeners() {
        usernameField.textProperty().addListener((obs, old, val) -> {
            updateUsernameStyle();
            checkUserExistenceAsync();
        });

        passwordField.textProperty().addListener((obs, old, val) -> updatePasswordStyle());
        model.userExistsProperty().addListener((obs, old, val) -> updateUsernameStyle());

        passwordField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                usernameField.requestFocus();
                event.consume();
            }
        });

        usernameField.focusedProperty().addListener((obs, old, hasFocus) -> {
            if (hasFocus) model.setLoginState(false);
        });
        passwordField.focusedProperty().addListener((obs, old, hasFocus) -> {
            if (hasFocus) model.setLoginState(true);
        });

        model.wrongCredentialsProperty().addListener((obs, old, triggered) -> {
            if (Boolean.TRUE.equals(triggered)) playErrorFlash();
        });

        model.isLoggedInProperty().addListener((obs, old, loggedIn) -> {
            if (Boolean.TRUE.equals(loggedIn)) playLoginSuccessAnimation();
        });

        updateUsernameStyle();
        updatePasswordStyle();
    }

    @FXML
    private void onUsernameAction() {
        if (!usernameField.getText().trim().isEmpty()) {
            passwordField.requestFocus();
        }
    }

    @FXML
    private void onPasswordAction() {
        submitLoginAsync();
    }

    private void checkUserExistenceAsync() {
        String u = usernameField.getText().trim();
        if (u.isEmpty()) {
            model.setUserExists(false);
            model.setStatus("");
            return;
        }

        loginAuth.exists(u).thenAccept(exists -> Platform.runLater(() -> {
            model.setUserExists(exists);
            boolean isTypingPassword = passwordField.isFocused();
            model.setStatus(!isTypingPassword
                    ? (exists ? "giocatore esistente (premi INVIO)" : "nuovo giocatore (premi INVIO)")
                    : (exists ? "inserisci password" : "crea nuova password"));
        }));
    }

    private void submitLoginAsync() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();

        if (p.isEmpty() || model.isLoggedIn()) return;
        model.setStatus("ELABORAZIONE IN CORSO...");

        loginAuth.exists(u).thenCompose(userExists -> {
            if (userExists) return loginAuth.login(u, p);
            else return loginAuth.register(u, p);
        }).thenCompose(sessionOpt ->
                IscatDB.getInstance().queryAsync(() -> {
                    if (sessionOpt.isEmpty()) return new LoginResult("Credenziali errate o errore di registrazione");

                    SessionUser user = sessionOpt.get();
                    UserSettings settings = settingsDAO.loadSettings(user.id()).orElseGet(() -> {
                        settingsDAO.createDefault(user.id());
                        return settingsDAO.loadSettings(user.id()).orElse(null);
                    });
                    scoreDAO.createIfNotExists(user.id());
                    ScoreModel scoreModel = scoreDAO.load(user.id()).orElse(new ScoreModel(user.id()));
                    String savedSkinKey = settingsDAO.loadPlayerSkin(user.id());

                    return new LoginResult(user, settings, scoreModel, savedSkinKey, "ACCESSO EFFETTUATO!");
                })
        ).thenAccept(result -> Platform.runLater(() -> {
            if (result.isSuccess()) {
                SessionManager.getInstance().setCurrentUser(result.user());
                SessionManager.getInstance().setCurrentSettings(result.settings());
                SessionManager.getInstance().setCurrentSaveData(result.scoreModel());

                Scene activeScene = rootPane.getScene();
                applyLoadedSettings(result.settings(), activeScene);

                EntityRecord skinRecord = EntityFactory.getCache().get(result.skinKey());
                if (skinRecord != null) {
                    SessionManager.setPlayerSkinKey(result.skinKey());
                    SessionManager.setPlayerSkin(skinRecord.spritePath());
                }

                model.setStatus(result.message());
                model.setLoggedIn(true);
            } else {
                handleError(result.message());
            }
        }));
    }

    private void handleError(String message) {
        model.setStatus(message);
        model.triggerError();
        passwordField.setText("");
    }

    public void reset() {
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");
        if (model != null) model.reset();
    }

    private void updateUsernameStyle() {
        if (isErrorFlashing || model == null) return;
        usernameField.getStyleClass().removeAll("login-text-empty", "login-text-exists", "login-text-missing");
        loginIcon.getStyleClass().removeAll("login-icon-empty", "login-icon-exists", "login-icon-missing");

        if (usernameField.getText().isEmpty()) {
            usernameField.getStyleClass().add("login-text-empty");
            loginIcon.getStyleClass().add("login-icon-empty");
            loginIcon.setIconColor(Color.WHITE);
        } else if (model.userExistsProperty().get()) {
            usernameField.getStyleClass().add("login-text-exists");
            loginIcon.getStyleClass().add("login-icon-exists");
            loginIcon.setIconColor(Color.LIMEGREEN);
        } else {
            usernameField.getStyleClass().add("login-text-missing");
            loginIcon.getStyleClass().add("login-icon-missing");
            loginIcon.setIconColor(Color.GOLD);
        }
    }

    private void updatePasswordStyle() {
        if (isErrorFlashing) return;
        passwordField.getStyleClass().removeAll("login-text-empty", "login-text-error");
        passwdIcon.getStyleClass().removeAll("login-icon-empty", "login-icon-error");
        passwordField.getStyleClass().add("login-text-empty");
        passwdIcon.getStyleClass().add("login-icon-empty");
        passwdIcon.setIconColor(Color.WHITE);
    }

    private void playErrorFlash() {
        Timeline errorFlash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    isErrorFlashing = true;
                    usernameField.getStyleClass().add("login-text-error");
                    passwordField.getStyleClass().add("login-text-error");
                    loginIcon.setIconColor(Color.TOMATO);
                    passwdIcon.setIconColor(Color.TOMATO);
                }),
                new KeyFrame(Duration.millis(300), e -> {
                    isErrorFlashing = false;
                    usernameField.getStyleClass().remove("login-text-error");
                    passwordField.getStyleClass().remove("login-text-error");
                    updateUsernameStyle();
                    updatePasswordStyle();
                })
        );
        errorFlash.playFromStart();
    }

    private void playLoginSuccessAnimation() {
        FadeTransition fadeOutU = new FadeTransition(Duration.millis(400), usernameField.getParent());
        fadeOutU.setToValue(0);
        FadeTransition fadeOutP = new FadeTransition(Duration.millis(400), passwordField.getParent());
        fadeOutP.setToValue(0);
        FadeTransition fadeOutS = new FadeTransition(Duration.millis(400), statusLabel);
        fadeOutS.setToValue(0);

        TranslateTransition moveTitle = new TranslateTransition(Duration.millis(600), welcomeTitle);
        moveTitle.setToY(-20);
        moveTitle.setInterpolator(Interpolator.EASE_BOTH);

        loggedInUserLabel.setText(model.getUsername());
        loggedInUserLabel.setManaged(true);

        TranslateTransition moveLabel = new TranslateTransition(Duration.millis(600), loggedInUserLabel);
        moveLabel.setToY(400);
        moveLabel.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeInUser = new FadeTransition(Duration.millis(500), loggedInUserLabel);
        fadeInUser.setFromValue(0);
        fadeInUser.setToValue(1);
        fadeInUser.setDelay(Duration.millis(200));

        ParallelTransition successAnim = new ParallelTransition(fadeOutU, fadeOutP, fadeOutS, moveTitle, moveLabel, fadeInUser);
        successAnim.setOnFinished(e -> IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU));
        successAnim.play();
    }

    public void onShow() {
        reset();

        if (usernameField != null && usernameField.getParent() != null) {
            usernameField.getParent().setOpacity(1);
        }
        if (passwordField != null && passwordField.getParent() != null) {
            passwordField.getParent().setOpacity(1);
        }
        statusLabel.setOpacity(1);
        loggedInUserLabel.setOpacity(0);
        loggedInUserLabel.setManaged(false);
        welcomeTitle.setTranslateY(0);
        loggedInUserLabel.setTranslateY(0);

        Platform.runLater(() -> usernameField.requestFocus());
    }

    public void onHide() {
    }

    @Override public void setPointerToView(StackPane pointer) {}
    public void setLoginModel(LoginModel model) { this.model = model; }

    private record LoginResult(SessionUser user, UserSettings settings, ScoreModel scoreModel, String skinKey, String message, boolean isSuccess) {
        public LoginResult(SessionUser u, UserSettings s, ScoreModel d, String skin, String m) {
            this(u, s, d, skin, m, true);
        }
        public LoginResult(String err) {
            this(null, null, null, "player1", err, false);
        }
    }

    private void applyLoadedSettings(UserSettings settings, Scene currentScene) {
        if (settings == null || currentScene == null) return;

        // Gestione Audio
        AudioManager audio = AudioManager.getInstance();
        audio.setMasterVolume(settings.getVolumeMaster());
        audio.setBgmVolume(settings.getVolumeBgm());
        audio.setSfxVolume(settings.getVolumeSfx());

        // Gestione Finestra/Fullscreen
        boolean goFullscreen = (settings.getFullscreen() == 1);
        IscatNavigator.getInstance().getModel().setFullscreen(goFullscreen);

        // Gestione Temi e Stati Visivi
        ThemeManager themeEngine = ThemeManager.getInstance();
        SessionManager.getInstance().isLightModeSelected = (settings.getLightmode() == 1);

        // Fermiamo la modalità arcobaleno precedente per evitare conflitti
        themeEngine.stopRainbowMode();

        // Prepariamo la palette recuperandola dalle impostazioni dell'utente
        List<String> savedPalette = List.of(
                settings.getPrimaryTheme() != null ? settings.getPrimaryTheme() : "#cbcbcb",
                settings.getSecondaryTheme() != null ? settings.getSecondaryTheme() : "#a9a9a9",
                settings.getTertiaryTheme() != null ? settings.getTertiaryTheme() : "#333333",
                settings.getBackgroundTheme() != null ? settings.getBackgroundTheme() : "#010203"
        );

        // Applichiamo sempre i colori di base del nuovo utente (così resetta il vecchio tema)
        themeEngine.applyHexColorsTheme(currentScene, savedPalette, 0.0);

        // Se l'utente appena loggato ha la modalità arcobaleno attiva, la avviamo sopra i suoi colori
        if (settings.getRainbowMode() == 1) {
            themeEngine.startRainbowMode(currentScene);
        }
    }
}