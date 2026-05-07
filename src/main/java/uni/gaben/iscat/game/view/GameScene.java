package uni.gaben.iscat.game.view;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.IscatSceneAbstract;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.view.hud.PauseMenu;

/**
 * Scena di gioco: posiziona il canvas e collega il controller.
 * 
 * LIFECYCLE:
 * - Constructor: crea nodi UI
 * - onShow(): avvia game loop
 * - onHide(): ferma game loop
 * 
 * Il game loop viene avviato solo quando la scena è visibile,
 * evitando che il gioco giri in background.
 */
public class GameScene extends IscatSceneAbstract {

    private final GameController controller;
    private final GameCanvas canvas;

    private StackPane root;
    private PauseMenu pauseMenu;

    public GameScene(GameController controller, GameCanvas canvas) {
        super(new StackPane());
        this.controller = controller;
        this.canvas = canvas;
        this.root = (StackPane) getRoot();

        initialize();
    }

    @Override
    protected void initStyles() {
        // Single stylesheet for all game views (GameScene, PauseMenu, OptionsMenu)
        String css = getClass().getResource("/uni/gaben/iscat/styles/game/game.css").toExternalForm();
        getStylesheets().add(css);
    }

    @Override
    protected void initNodes() {
        pauseMenu = new PauseMenu(controller);
    }

    @Override
    protected void initLayout() {
        root.getChildren().addAll(canvas, pauseMenu);
    }

    @Override
    protected void initBindings() {
        controller.setOnPauseToggle(pauseMenu::show);
    }

    @Override
    protected void initEventHandlers() {
        // Attach input handlers to this scene
        controller.attachInput(this);
    }

    @Override
    public void onShow() {
        // Carica audio del giocatore (una volta sola, qui fuori dal model)
        IscatAudioManager am = IscatAudioManager.getInstance();
        am.loadSFX("fart_alt1", "/uni/gaben/iscat/audio/SFX/fart3.wav");
        am.loadSFX("fart_alt2", "/uni/gaben/iscat/audio/SFX/fart8.wav");
        am.loadSFX("fart_alt3", "/uni/gaben/iscat/audio/SFX/fart7.wav");

        // Collega il callback audio al player
        controller.setupPlayerAudio();

        // Avvia il game loop quando la scena viene mostrata
        controller.startLoop();
    }

    @Override
    public void onHide() {
        // Ferma il game loop quando la scena viene nascosta
        controller.stopLoop();
    }
}
