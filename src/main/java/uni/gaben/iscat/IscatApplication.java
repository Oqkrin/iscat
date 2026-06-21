package uni.gaben.iscat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.controller.IscatViewController;
import uni.gaben.iscat.controller.IscatWindowController;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.utils.ExternalResourceResolver;
import uni.gaben.iscat.view.components.IscatTitleBar;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Radice dell'applicazione — puro bootstrap, nessuna logica di business.
 * Costruisce e collega tutte le triadi MVC, per poi passare il controllo a IscatNavigator.
 */
public class IscatApplication extends Application {

    private final IscatModel iscatModel = new IscatModel();
    private final StackPane iscatRootPane = new StackPane();
    private final Scene iscatRootScene = new Scene(iscatRootPane);
    private final StackPane iscatPointerToContent = new StackPane();
    private final IscatTitleBar iscatTitleBar = new IscatTitleBar();
    private final Region iscatWindowBorderOverlay = new Region();

    IscatViewController iscatViewController;
    IscatWindowController iscatWindowController;
    private IscatWindowController windowController;
    private IscatViewController viewController;

    /**
     * Inizializza le risorse globali dell'applicazione prima dell'avvio dell'interfaccia grafica.
     * Carica i font personalizzati, avvia il database, precarica la cache delle entità e configura il navigatore.
     */
    @Override
    public void init() {
        Font.loadFont(getClass().getResourceAsStream("/uni/gaben/iscat/fonts/Miracode.ttf"), 10);
        IscatDB.getInstance().init();
        Platform.runLater(EntityFactory::ensureCacheLoaded);
        IscatNavigator.getInstance().initialize(iscatModel);
        ExternalResourceResolver.init(Path.of("entities"));
    }

    /**
     * Avvia l'interfaccia grafica principale dell'applicazione.
     * Configura lo stage in modalità non decorata, istanzia i controller della finestra e delle viste,
     * applica gli stili globali e mostra la schermata iniziale.
     *
     * @param stage Lo stage primario fornito dalla piattaforma JavaFX
     */
    @Override
    public void start(Stage stage) {
        AudioManager.getInstance().loadDefaultAudio();


        iscatRootScene.setFill(ThemeManager.getInstance().getBgPrimary());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(iscatRootScene);

        // Il componente finestra prende il controllo dell'intera struttura visiva
        windowController = new IscatWindowController(iscatModel, stage);

        // Il controller delle viste utilizza il pannello del contenuto della finestra
        viewController = new IscatViewController(iscatModel, windowController.getWindow());
        viewController.showInitialView(IscatViews.LOGIN_MENU);

        // Applica gli stili alla radice della scena (che ora è la classe IscatWindow)
        addIscatStyles(iscatRootScene);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(iscatRootScene);
        stage.show();
        stage.centerOnScreen();
    }

    /**
     * Carica e associa i fogli di stile CSS globali (colori, tipografia e componenti condivisi) alla scena specificata.
     */
    private void addIscatStyles(Scene scene) {
        String colorTheme = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-color-theme.css")).toExternalForm();
        String typography = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-typography.css")).toExternalForm();
        String components = Objects.requireNonNull(
                IscatApplication.class.getResource("/uni/gaben/iscat/styles/iscat-components-shared.css")).toExternalForm();
        scene.getStylesheets().addAll(colorTheme, typography, components);
    }

    /**
     * Gestisce la chiusura in sicurezza dell'applicazione, disconnettendo le istanze attive del database.
     */
    @Override
    public void stop() throws Exception {
        IscatDB.getInstance().shutdown();
    }

}