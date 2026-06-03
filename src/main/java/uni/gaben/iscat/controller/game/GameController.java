package uni.gaben.iscat.controller.game;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.model.game.GameState;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

public class GameController {

    private final GameModel   gameModel;
    private final GameInputsHandler gameInputsHandler = new GameInputsHandler();
    private final ScoreDAO    scoreDAO;

    private UniverseController    universeController;
    private UniverseWaveController waveController;
    private AnimationTimer        gameLoop;
    private Runnable              drawCall;
    private Runnable              onUniverseResetCallback;
    private StackPane             contentRoot;

    private boolean showFps = false;
    private final BooleanProperty showDebugMode  = new SimpleBooleanProperty(false);
    private final BooleanProperty optionsMenuOpen = new SimpleBooleanProperty(false);

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public GameController(GameModel gameModel) {
        this.gameModel        = gameModel;
        this.scoreDAO         = IscatDB.getInstance().getScoreDAO();
        this.universeController = new UniverseController(gameModel.getUniverseModel());
        this.waveController   = new UniverseWaveController();

        UniverseSpawner.getInstance().init(getUniverseModel(), universeController, waveController);

        double midX = UniverseModel.DEFAULT_SPAWN_CENTER;
        double midY = UniverseModel.DEFAULT_SPAWN_CENTER;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);
        getUniverseModel().getPlayer().setOnDeathCallback(this::onPlayerDeath);

        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);

        buildGameLoop();
    }

    // -------------------------------------------------------------------------
    // Game loop
    // -------------------------------------------------------------------------

    private void buildGameLoop() {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameModel.getStart() == -1) gameModel.startProperty().set(now);
                if (gameModel.getLastUpdate() == 0) {
                    gameModel.setLastUpdate(now);
                    return;
                }

                gameModel.setNow(now);

                double totalSec = (now - gameModel.getStart()) / GameModel.ONE_SECOND_IN_NANOS;
                gameModel.setTotalElapsedSeconds(totalSec);

                int h = (int) (totalSec / 3600);
                int m = (int) ((totalSec % 3600) / 60);
                int s = (int) (totalSec % 60);
                gameModel.timerProperty().set(h * 10000 + m * 100 + s);

                double dt = Math.min(gameModel.getDt(), GameModel.ACCUMULATORUNIT);
                tick(dt);

                if (drawCall != null) drawCall.run();

                gameModel.setLastUpdate(now);
            }
        };
    }

    private void tick(double dt) {
        if (gameInputsHandler.consumePause()) togglePause();
        if (!gameModel.isPaused()) {
            universeController.updatev(dt, gameInputsHandler, getCameraModel());
            if (waveController != null && gameModel.isWaveActive())
                waveController.update(dt, getCameraModel(), gameModel);
        }
    }

    public void startGameLoop() { gameLoop.start(); }
    public void stopGameLoop()  { gameLoop.stop();  }

    // -------------------------------------------------------------------------
    // Game state control
    // -------------------------------------------------------------------------

    public void togglePause() {
        if (gameModel.getGameState() == GameState.PLAYING)
            gameModel.setGameState(GameState.IN_PAUSE);
        else if (gameModel.getGameState() == GameState.IN_PAUSE)
            gameModel.setGameState(GameState.PLAYING);
    }

    public void resetUniverse() {
        AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav", true);
        gameModel.setGameOver(false);
        gameModel.setPaused(false);
        SessionScoreTracker.getInstance().reset();

        double canvasW = getCameraModel().getScreenWidth();
        double canvasH = getCameraModel().getScreenHeight();

        if (canvasW <= 0 || canvasH <= 0) {
            System.err.println("!!! resetUniverse called with invalid canvas dimensions: " + canvasW + "x" + canvasH);
            System.err.println("!!! Using default dimensions");
            canvasW = UniverseSettings.DEFAULT_WIDTH;
            canvasH = UniverseSettings.DEFAULT_HEIGHT;
        }

        gameModel.resetUniverse();
        this.universeController = new UniverseController(getUniverseModel());
        this.waveController     = new UniverseWaveController();

        getUniverseModel().setDimensions(canvasW, canvasH);

        UniverseSpawner.getInstance().init(getUniverseModel(), universeController, waveController);
        getUniverseModel().getStarfieldModel().generate(canvasW, canvasH);

        double midX = canvasW / 2.0;
        double midY = canvasH / 2.0;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);
        getUniverseModel().getPlayer().setOnDeathCallback(this::onPlayerDeath);
        UniverseSpawner.getInstance().spawnInitialAsteroidBelts(midX, midY);

        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);

        gameModel.startProperty().set(-1);
        gameModel.setLastUpdate(0);
        gameModel.setTotalElapsedSeconds(0.0);

        if (onUniverseResetCallback != null) onUniverseResetCallback.run();
    }

    public void retryGame() {
        stopGameLoop();
        resetUniverse();
        startGameLoop();
    }

    public void quitToMainMenu() {
        stopGameLoop();
        gameModel.setPaused(false);
        saveStats();
        resetUniverse();
        AudioManager.getInstance().stopBGM();
        showDebugMode.set(false);
        if (contentRoot != null)
            IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    public void quitGame() { Platform.exit(); }

    public void debugSpawn(String spawnableId) {
        double x = getCameraModel().getX() + ((Math.random() - 0.5) * 400);
        double y = getCameraModel().getY() + ((Math.random() - 0.5) * 400);
        UniverseSpawner.getInstance().spawn(spawnableId, x, y);
    }

    // -------------------------------------------------------------------------
    // Callbacks
    // -------------------------------------------------------------------------

    private void onPlayerDeath() {
        Platform.runLater(() -> {
            AudioManager.getInstance().stopBGM();
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/gameover.wav", true);
            gameModel.setGameOver(true);
            gameModel.setPaused(true);
            saveStats();
        });
    }

    private void saveStats() {
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        int userId = user.id();
        SessionScoreTracker tracker = SessionScoreTracker.getInstance();

        final int score    = tracker.getScore();
        final int elapsed  = (int) gameModel.getTotalElapsedSeconds();
        final int dealt    = tracker.getDamageDealt();
        final int received = tracker.getDamageReceived();
        final int deaths   = tracker.getDeaths();
        final int kills    = tracker.getKills();
        final int boosts   = tracker.getBoosts();

        tracker.reset();

        IscatDB.getInstance().executeAsync(() -> {
            ScoreModel current = scoreDAO.load(userId)
                    .orElse(new ScoreModel(userId));

            // Aggiorna Score se maggiore
            if (score > current.score()) {
                scoreDAO.update(userId, "Score", score);
            }

            // Aggiorna BestTime se migliore
            if (elapsed < current.bestTime() || current.bestTime() == 0) {
                scoreDAO.update(userId, "BestTime", elapsed);
            }

            // Aggiorna LongestTime se maggiore
            if (elapsed > current.longestTime()) {
                scoreDAO.update(userId, "LongestTime", elapsed);
            }

            // Incrementa le statistiche cumulative
            scoreDAO.increment(userId, "TotalDamageDealt",    dealt);
            scoreDAO.increment(userId, "TotalDamageReceived", received);
            scoreDAO.increment(userId, "Deaths",              deaths);
            scoreDAO.increment(userId, "TotalKills",          kills);
            scoreDAO.increment(userId, "BoostCollected",      boosts);
            scoreDAO.increment(userId, "TimesPlayed",         1);

            // Ricarica i dati aggiornati e li mette in sessione
            scoreDAO.load(userId).ifPresent(data ->
                    Platform.runLater(() -> SessionManager.getInstance().setCurrentSaveData(data)));
        });
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public void setOnUniverseResetCallback(Runnable callback) { this.onUniverseResetCallback = callback; }
    public void setDrawCall(Runnable drawCall)                { this.drawCall = drawCall; }
    public void setContentRoot(StackPane root)               { this.contentRoot = root; }

    public GameModel           getGameModel()          { return gameModel; }
    public GameInputsHandler getInputManager()       { return gameInputsHandler; }
    public UniverseModel       getUniverseModel()      { return gameModel.getUniverseModel(); }
    public UniverseController  getUniverseController() { return universeController; }
    public CameraModel         getCameraModel()        { return gameModel.getCameraModel(); }

    public boolean             isFpsOn()               { return showFps; }
    public void                setShowFps(boolean v)   { this.showFps = v; }

    public boolean             isDebugModeOn()         { return showDebugMode.get(); }
    public void                setShowDebugMode(boolean v) { showDebugMode.set(v); }
    public BooleanProperty     debugModeProperty()     { return showDebugMode; }

    public boolean             isOptionsMenuOpen()     { return optionsMenuOpen.get(); }
    public void                setOptionsMenuOpen(boolean v) { optionsMenuOpen.set(v); }
    public BooleanProperty     optionsMenuOpenProperty() { return optionsMenuOpen; }
}