package uni.gaben.iscat.game.view;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.IscatSceneAbstract;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.view.hud.PauseMenu;

/**
 * Scena di gioco: posiziona il canvas e collega il controller.
 *
 * LIFECYCLE:
 * - onShow(): avvia game loop, carica audio
 * - onHide(): ferma game loop
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
        this.root = getContentRoot();
        initialize();
    }

    @Override
    protected void initStyles() {
        String css = getClass().getResource("/uni/gaben/iscat/styles/game/game.css").toExternalForm();
        getStylesheets().add(css);
    }

    @Override
    protected void initNodes() {
        pauseMenu = new PauseMenu(controller);
    }

    @Override
    protected void initLayout() {
        // Bind canvas to fill the root StackPane
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // Clip canvas to its own bounds so it never paints outside the content area
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(canvas.widthProperty());
        clip.heightProperty().bind(canvas.heightProperty());
        canvas.setClip(clip);

        root.getChildren().addAll(canvas, pauseMenu);
    }

    @Override
    protected void initBindings() {
        controller.setOnPauseToggle(pauseMenu::show);
    }

    @Override
    protected void initEventHandlers() {
        controller.attachInput(this);
        canvas.setOnMouseClicked(e -> canvas.requestFocus());
    }

    @Override
    public void onShow() {
        IscatAudioManager am = IscatAudioManager.getInstance();
        am.loadSFX("fart_alt1", "/uni/gaben/iscat/audio/SFX/fart3.wav");
        am.loadSFX("fart_alt2", "/uni/gaben/iscat/audio/SFX/fart8.wav");
        am.loadSFX("fart_alt3", "/uni/gaben/iscat/audio/SFX/fart7.wav");
        controller.setupPlayerAudio();
        controller.startLoop();
    }

    @Override
    public void onHide() {
        controller.stopLoop();
    }
}
