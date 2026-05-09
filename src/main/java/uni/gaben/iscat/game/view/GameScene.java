package uni.gaben.iscat.game.view;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.IscatSceneAbstract;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.controller.GameController;

import java.util.Objects;

/**
 * Scena di gioco: posiziona il canvas e collega il controller.
 * LIFECYCLE:
 * - onShow(): avvia game loop, carica audio
 * - onHide(): ferma game loop
 */
public class GameScene extends IscatSceneAbstract {

    private final GameController controller;
    private final GameCanvas canvas;
    private StackPane root;
    private PauseMenu pauseMenu;

    public GameScene(GameController controller, GameModel model) {
        super(new StackPane());
        this.controller = controller;
        this.canvas = new GameCanvas(model);
        this.controller.setCanvas(this.canvas);
        this.root = getContentRoot();
        initialize();
    }

    @Override
    protected void initStyles() {
        String css = Objects.requireNonNull(getClass().getResource("/uni/gaben/iscat/styles/game.css")).toExternalForm();
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
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
    }

    @Override
    protected void initEventHandlers() {
        controller.setOnPauseToggle(pauseMenu::show);
        controller.attachInput(this);
        canvas.setOnMouseClicked(e -> canvas.requestFocus());
    }

    @Override
    public void onShow() {
        IscatAudioManager am = IscatAudioManager.getInstance();
        am.loadAllSFX("/uni/gaben/iscat/audio/SFX");
        controller.setupPlayerAudio();
        controller.startLoop();
    }

    @Override
    public void onHide() {
        controller.stopLoop();
    }
}
