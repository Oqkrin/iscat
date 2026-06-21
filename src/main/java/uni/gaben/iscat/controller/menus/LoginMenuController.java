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
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.List;

/**
 * Controller per la gestione della schermata di Autenticazione (Login/Registrazione).
 * Coordina l'interfaccia di accesso consentendo sia il login di utenti esistenti sia
 * la creazione automatica di nuovi profili, integrando meccanismi asincroni di verifica,
 * ridimensionamento adattivo dei caratteri di input e animazioni di successo o errore.
 */
public class LoginMenuController implements IscatFxmlController {

    /** Pannello contenitore principale della vista. */
    @FXML private StackPane rootPane;
    /** Box di disposizione verticale contenente i form di input e la messaggistica. */
    @FXML private VBox      contentBox;
    /** StackPane superiore adibito all'header grafico del menu. */
    @FXML private StackPane headerStack;
    /** Etichetta testuale di benvenuto primaria della schermata. */
    @FXML private Label     welcomeTitle;
    /** Etichetta di notifica visiva per l'utente correntemente loggato durante le transizioni. */
    @FXML private Label     loggedInUserLabel;

    /** Campo di testo per l'inserimento dello username del giocatore. */
    @FXML private TextField usernameField;
    /** Campo protetto per la digitazione della password. */
    @FXML private PasswordField passwordField;

    /** Icona glifo associata al campo dello username. */
    @FXML private FontIcon  loginIcon;
    /** Icona glifo associata al campo della password. */
    @FXML private FontIcon  passwdIcon;
    /** Label descrittiva per la visualizzazione dinamica dello stato o degli avvisi di sistema. */
    @FXML private Label     statusLabel;

    /** Modello di stato logico delegato a contenere le proprietà osservabili del login. */
    private LoginModel model;
    /** Componente di business logic per la validazione, crittografia e verifica delle credenziali. */
    private LoginAuth  loginAuth;
    /** Data Access Object per la manipolazione e persistenza delle statistiche (punteggi). */
    private ScoreDAO   scoreDAO;
    /** Data Access Object per il caricamento e salvataggio delle impostazioni utente. */
    private SettingsDAO settingsDAO;

    /** Flag di controllo per prevenire la sovrapposizione dell'animazione di flash di errore. */
    private boolean  isErrorFlashing = false;

    /**
     * Inizializzazione standard del ciclo di vita FXML.
     * I setup strutturali e i binding logici sono delegati al metodo esplicito {@link #setup()}.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Configura l'architettura logica del controller inizializzando i DAO e i componenti di autenticazione.
     * Configura gli helper {@link AutoFittingInputBinder} per l'auto-adattamento dinamico della dimensione
     * del font rispetto alla lunghezza del testo immesso, applicando vincoli geometrici basati sulla larghezza del root.
     */
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

    /**
     * Stabilisce i binding bidirezionali e monodirezionali tra le componenti grafiche (JavaFX)
     * e le proprietà reattive definite nel rispettivo {@link LoginModel}, applicando le costanti
     * della sezione aurea per il dimensionamento del box dei contenuti.
     */
    private void initBindings() {
        contentBox.maxWidthProperty().bind(rootPane.widthProperty().multiply(ScalareAureo.IPHI_D));
        welcomeTitle.maxWidthProperty().bind(contentBox.maxWidthProperty());

        usernameField.textProperty().bindBidirectional(model.usernameProperty());
        passwordField.textProperty().bindBidirectional(model.passwordProperty());
        statusLabel.textProperty().bind(model.statusProperty());
    }

    /**
     * Registra i listener sulle proprietà reattive e intercetta le variazioni di focus e tastiera.
     * Gestisce i cambi di stile in tempo reale e mappa eventi speciali (es. il tasto ESCAPE per muovere
     * il focus all'indietro dal campo password al campo username).
     */
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

    /**
     * Intercetta l'azione di invio (Action) sul campo username,
     * spostando il focus sul campo password qualora il testo non sia vuoto.
     */
    @FXML
    private void onUsernameAction() {
        if (!usernameField.getText().trim().isEmpty()) {
            passwordField.requestFocus();
        }
    }

    /**
     * Intercetta l'azione di invio (Action) sul campo password,
     * avviando la procedura asincrona di sottomissione delle credenziali.
     */
    @FXML
    private void onPasswordAction() {
        submitLoginAsync();
    }

    /**
     * Verifica in tempo reale e in modo non bloccante se lo username digitato è già presente nel DB.
     * Al rientro della chiamata asincrona, aggiorna lo stato del modello e adegua il testo del messaggio
     * della label di stato indicando all'utente se la pressione di INVIO comporterà un accesso o una registrazione.
     */
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

    /**
     * Coordina la pipeline asincrona di autenticazione e inizializzazione del profilo (Login/Register Pipeline).
     * <p>
     * Esegue in successione su thread asincroni le seguenti operazioni:
     * <ol>
     * <li>Controllo di esistenza utente: effettua il login se presente, altrimenti ne esegue la registrazione.</li>
     * <li>Inizializzazione Account: carica (o crea se assenti) le configurazioni in `SettingsDAO` e il record statistiche in `ScoreDAO`.</li>
     * <li>Risoluzione Risorse: estrae la skin memorizzata caricandone le coordinate grafiche dalla cache delle entità.</li>
     * <li>Popolamento Sessione: al rientro sul thread grafico UI, setta il {@link SessionManager} e applica i parametri globali audio/video/temi.</li>
     * </ol>
     */
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
        })).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> handleError("Errore di connessione"));
            return null;
        });
    }

    /**
     * Intercetta e normalizza lo stato di errore del login.
     * Aggiorna la label dei messaggi, notifica il modello per scatenare il feedback visivo e svuota il form password.
     *
     * @param message Il messaggio di errore testuale da mostrare a schermo.
     */
    private void handleError(String message) {
        model.setStatus(message);
        model.triggerError();
        passwordField.setText("");
    }

    /**
     * Ripristina i campi di immissione del form di login e azzera le variabili interne del rispettivo modello.
     */
    public void reset() {
        if (usernameField != null) usernameField.setText("");
        if (passwordField != null) passwordField.setText("");
        if (model != null) model.reset();
    }

    /**
     * Modifica dinamicamente le classi CSS applicate al campo username e all'icona glifo.
     * Varia le colorazioni a seconda dello stato di compilazione: vuoto (Bianco),
     * utente esistente (Verde) o nuovo utente in fase di registrazione (Oro).
     */
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

    /**
     * Modifica e ripristina lo stile standard per il campo di inserimento password e la relativa icona.
     */
    private void updatePasswordStyle() {
        if (isErrorFlashing) return;
        passwordField.getStyleClass().removeAll("login-text-empty", "login-text-error");
        passwdIcon.getStyleClass().removeAll("login-icon-empty", "login-icon-error");
        passwordField.getStyleClass().add("login-text-empty");
        passwdIcon.getStyleClass().add("login-icon-empty");
        passwdIcon.setIconColor(Color.WHITE);
    }

    /**
     * Riproduce una traccia temporale (Timeline) di feedback per segnalare l'inserimento di credenziali errate.
     * Colora temporaneamente i bordi dei componenti e le icone di rosso (Tomato) per 300 millisecondi
     * prima di forzare il ripristino delle colorazioni standard dei widget.
     */
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

    /**
     * Avvia e orchestra la coreografia di animazioni parallele (ParallelTransition) in caso di login completato con successo.
     * Effettua la dissolvenza in uscita (FadeOut) dei form e l'allineamento geometrico traslatorio dei titoli,
     * mostrando il nome dell'utente autenticato prima di invocare il navigatore per passare al menu principale.
     */
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

    /**
     * Metodo di callback invocato dal ciclo di attivazione della vista quando lo schermo viene mostrato.
     * Resetta le opacità dei nodi grafici, azzera i testi e richiede asincronamente il focus sul campo username.
     */
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

    /**
     * Metodo di callback invocato dal ciclo del navigatore quando lo schermo viene nascosto.
     */
    public void onHide() {
    }

    /** {@inheritDoc} */
    @Override public void setPointerToView(StackPane pointer) {}

    /** @param model L'istanza di {@link LoginModel} da iniettare nel controller. */
    public void setLoginModel(LoginModel model) { this.model = model; }

    /**
     * Record interno di utilità adibito a incapsulare l'esito aggregato della pipeline di login.
     */
    private record LoginResult(SessionUser user, UserSettings settings, ScoreModel scoreModel, String skinKey, String message, boolean isSuccess) {
        /** Costruttore per esito positivo. */
        public LoginResult(SessionUser u, UserSettings s, ScoreModel d, String skin, String m) {
            this(u, s, d, skin, m, true);
        }
        /** Costruttore per esito negativo (fallimento). */
        public LoginResult(String err) {
            this(null, null, null, "player1", err, false);
        }
    }

    /**
     * Applica a livello globale ed engine del gioco i parametri configurati nel profilo utente appena estratto.
     * Imposta i volumi di {@link AudioManager}, allinea lo stato dello schermo intero e configura i motori
     * grafici cromatici di {@link ThemeManager} (compresa la gestione dell'effetto Rainbow Mode e delle palette Hex).
     *
     * @param settings     L'oggetto {@link UserSettings} contenente le configurazioni dell'utente.
     * @param currentScene La scena JavaFX corrente su cui applicare gli stili e fogli di stile.
     */
    private void applyLoadedSettings(UserSettings settings, Scene currentScene) {
        if (settings == null || currentScene == null) return;

        // Regolazione volumi
        AudioManager audio = AudioManager.getInstance();
        audio.setMasterVolume(settings.getVolumeMaster());
        audio.setBgmVolume(settings.getVolumeBgm());
        audio.setSfxVolume(settings.getVolumeSfx());

        // Regolazione Fullscreen
        boolean goFullscreen = (settings.getFullscreen() == 1);
        IscatNavigator.getInstance().getModel().setFullscreen(goFullscreen);

        // Configurazione engine temi grafici
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
                themeEngine.switchTheme(currentScene, "/uni/gaben/iscat/styles/iscat-color-theme.css", Color.WHITE, 0.0);
            }
        }
    }
}