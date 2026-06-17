package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.model.login.LoginAuth;
import uni.gaben.iscat.model.login.LoginModel;
import uni.gaben.iscat.model.login.LoginState;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerSettings;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.List;

public class LoginController {

    private static final int MAX_INPUT_LENGTH = 20;

    private final LoginModel model;
    private final LoginAuth loginAuth;
    private final ScoreDAO scoreDAO;
    private final SettingsDAO settingsDAO;

    private final StringBuilder usernameBuffer = new StringBuilder();
    private final StringBuilder passwordBuffer = new StringBuilder();
    private LoginState currentLoginState = LoginState.USERNAME;

    public LoginController(LoginModel model, LoginAuth loginAuth) {
        this.model = model;
        this.loginAuth = loginAuth;
        this.scoreDAO = IscatDB.getInstance().getScoreDAO();
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();

        model.loginStateProperty().addListener((obs, old, isTypingPass) ->
                this.currentLoginState = Boolean.TRUE.equals(isTypingPass) ? LoginState.PASSWORD : LoginState.USERNAME);
    }

    public void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case BACK_SPACE -> onBackspace();
            case ENTER      -> onEnter();
            case ESCAPE     -> onEscape();
            default         -> {}
        }
    }

    public void onKeyTyped(KeyEvent e) {
        String character = e.getCharacter();
        if (character == null || character.isEmpty() || character.charAt(0) < 32 || character.charAt(0) == 127 || character.charAt(0) == 27) return;

        if (currentLoginState == LoginState.USERNAME) {
            if (usernameBuffer.length() < MAX_INPUT_LENGTH) usernameBuffer.append(character);
        } else {
            if (passwordBuffer.length() < MAX_INPUT_LENGTH) passwordBuffer.append(character);
        }
        updateDisplay();
        checkUserExistenceAsync();
    }

    private void onBackspace() {
        if (currentLoginState == LoginState.USERNAME) {
            if (!usernameBuffer.isEmpty()) usernameBuffer.setLength(usernameBuffer.length() - 1);
        } else {
            if (!passwordBuffer.isEmpty()) passwordBuffer.setLength(passwordBuffer.length() - 1);
            else { backToUsername(); return; }
        }
        updateDisplay();
        checkUserExistenceAsync();
    }

    private void onEnter() {
        if (currentLoginState == LoginState.USERNAME) {
            if (!usernameBuffer.toString().trim().isEmpty()) {
                model.setLoginState(true);
                checkUserExistenceAsync();
            }
        } else {
            submitLoginAsync();
        }
    }

    private void onEscape() {
        if (currentLoginState == LoginState.PASSWORD) backToUsername();
    }

    private void backToUsername() {
        passwordBuffer.setLength(0);
        model.setLoginState(false);
        updateDisplay();
        checkUserExistenceAsync();
    }

    private void checkUserExistenceAsync() {
        String u = usernameBuffer.toString().trim();
        if (u.isEmpty()) {
            model.setUserExists(false);
            model.setStatus("");
            return;
        }

        loginAuth.exists(u).thenAccept(exists -> Platform.runLater(() -> {
            model.setUserExists(exists);
            model.setStatus(currentLoginState == LoginState.USERNAME
                    ? (exists ? "giocatore esistente (premi INVIO)" : "nuovo giocatore (premi INVIO)")
                    : (exists ? "inserisci password" : "crea nuova password"));
        }));
    }

    private void submitLoginAsync() {
        String u = usernameBuffer.toString().trim();
        String p = passwordBuffer.toString();

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

                Scene activeScene = Window.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .map(Window::getScene)
                        .orElse(null);

                applyLoadedSettings(result.settings(), activeScene);

                EntityRecord skinRecord = EntityFactory.getCache().get(result.skinKey());
                if (skinRecord != null) {
                    PlayerSettings.setPlayerSkinKey(result.skinKey());
                    PlayerSettings.setPlayerSkin(skinRecord.spritePath());
                }

                model.setStatus(result.message());
                model.setLoggedIn(true);
            } else {
                handleError(result.message());
            }
        })).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> handleError("Errore di connessione"));
            return null;
        });
    }

    private void handleError(String message) {
        model.setStatus(message);
        model.triggerError();
        passwordBuffer.setLength(0);
        updateDisplay();
    }

    private void updateDisplay() {
        model.setUsername(usernameBuffer.toString());
        model.setPassword("*".repeat(passwordBuffer.length()));
    }

    public void reset() {
        usernameBuffer.setLength(0);
        passwordBuffer.setLength(0);
        currentLoginState = LoginState.USERNAME;
        model.reset();
    }

    public LoginModel getLoginModel() {
        return this.model;
    }

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

        AudioManager audio = AudioManager.getInstance();
        audio.setMasterVolume(settings.getVolumeMaster());
        audio.setBgmVolume(settings.getVolumeBgm());
        audio.setSfxVolume(settings.getVolumeSfx());

        boolean goFullscreen = (settings.getFullscreen() == 1);
        IscatNavigator.getInstance().getModel().setFullscreen(goFullscreen);

        ThemeManager themeEngine = ThemeManager.getInstance();
        SessionManager.getInstance().isLightModeSelected = (settings.getLightmode() == 1);

        if (settings.getRainbowMode() == 1) {
            themeEngine.startRainbowMode(currentScene);
        } else {
            themeEngine.stopRainbowMode();
        }

        if (settings.getRainbowMode() != 1) {
            if (settings.getPrimaryTheme() != null && !settings.getPrimaryTheme().equalsIgnoreCase("#FFFFFF")) {
                List<String> savedPalette = List.of(
                        settings.getPrimaryTheme(),
                        settings.getSecondaryTheme(),
                        settings.getTertiaryTheme(),
                        settings.getBackgroundTheme()
                );
                themeEngine.applyHexColorsTheme(currentScene, savedPalette, 0.0);
            } else {
                themeEngine.switchTheme(
                        currentScene,
                        "/uni/gaben/iscat/styles/iscat-color-theme.css",
                        javafx.scene.paint.Color.WHITE,
                        0.0
                );
            }
        }
    }
}