package uni.gaben.iscat.menus.bestiary_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.game.universe.enemies.iscat_eater.IscatEaterSettings;
import uni.gaben.iscat.game.universe.enemies.iscat_mob.IscatMobSettings;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.IscatWormSettings;
import uni.gaben.iscat.utils.components.AnimatedCanvas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BestiaryMenuController implements IscatFxmlController {

    private record Enemy(String name, String sprite, int frameW, int frameH, String description) { }

    private static final String BASE = "/uni/gaben/iscat/sprites/enemies/";
    private static final double DISPLAY_SIZE = 160.0;

    private static final Map<String, BestiaryMenuController.Enemy> ENEMIES = Map.ofEntries(
            Map.entry("iscat_mob", new BestiaryMenuController.Enemy("Iscat Mob", BASE + "iscat_mob.png",
                    IscatMobSettings.DIM_SPRITE, IscatMobSettings.DIM_SPRITE,
                    "Descrizione Iscat\nFiglio di Iscat Mother, ama mangiare pankakes a colazione e pizza per pranzo e cena. ASPETTA QUESTA E' LA DESCRIZIONE! SORRY!! Iscat è un nemico che naviga per lo spazio in cerca di cibo solitamente in gruppo. Ha poca vita e poca potenza d'attacco.")),
            Map.entry("iscat_bomber", new BestiaryMenuController.Enemy("Iscat Bomber", BASE + "iscat_bomber.png",
                    32, 32, "Descrizione Iscat Bomber")),
            Map.entry("iscat_core", new BestiaryMenuController.Enemy("Iscat Core", BASE + "iscat_core.png",
                    64, 64, "Descrizione Iscat Core\n Iscat, Macchina e Roccia che cos'è quest'abominio\nSembra sprizzare magia ma non c'è nulla naturale in lui, un omuncolo frutto del malsano ingegno del maestro nella sua ricerca perpetua dell'immortalità la spiraleggiante effimerezza dei golem è riuscito ad imbrigliare la creazione di questa nuova specie molti pensano sia solo il bisogno logistico di un esercito, ma da dove nasce un iscat cosa proteggono i golem domande che un giovane maestro ha risolto, ora in zone remote dell'universo le madri degli iscat vengono sfruttate curate e torturate e con esse la loro prole, e da cotanto dolore i cometa golem arrivano da ogni dove in massa; parte della loro linfa magica estratta per sostenere il maestro; ormai vacui e deboli vengono ammassati e compressati insieme agli iscat che volevano proteggere fusi nelle immonde creature che sono gli iscat core chimere magiche meccaniche ma vive, questo barlume di vita e magia sprizza via nei suoi ultimi momenti")),
            Map.entry("iscat_mother", new BestiaryMenuController.Enemy("Iscat Mother", BASE + "iscat_mother.png",
                    128, 128, "Descrizione Iscat Mother")),
            Map.entry("fake_iscat", new BestiaryMenuController.Enemy("Fake Iscat", BASE + "fake_iscat.png",
                    32, 32, "Descrizione Fake Iscat")),
            Map.entry("fallen_star_golem", new BestiaryMenuController.Enemy("Fallen Star Golem", BASE + "fallen_star_golem.png",
                    64, 64, "Descrizione Fallen Star Golem\nSpiraleggianti creature? o rocce?\nsembrano arrivare come una stella cadente dallo spazio profondo quando gli iscat sono in pericolo ma perché?")),
            Map.entry("eater", new BestiaryMenuController.Enemy("Eater", BASE + "eater.png",
                    IscatEaterSettings.DIM_SPRITE, IscatEaterSettings.DIM_SPRITE,
                    "Descrizione Eater")),
            Map.entry("iscat_worm_head", new BestiaryMenuController.Enemy("Iscat Worm Head", BASE + "iscat_worm_head.png",
                    (int) IscatWormSettings.DIM_SPRITE, (int) IscatWormSettings.DIM_SPRITE,
                    "Descrizione Iscat Worm Head\nIl comandante del sistema ISCAT_WORM.EXE, Iscat Worm Head si occupa di dirigere le parti del corpo e la coda nella direzione che vuole percorrere. Viaggia per lo spazio senza meta, e attacca chi definisce come possibile fonte di cibo, purtroppo per lui, è costretto di dividere il cibo con tutti i membri del sistema")),
            Map.entry("iscat_worm_body_part", new BestiaryMenuController.Enemy("Iscat Worm Body Part", BASE + "iscat_worm_body_part.png",
                    (int) IscatWormSettings.DIM_SPRITE, (int) IscatWormSettings.DIM_SPRITE,
                    "Descrizione Iscat Worm Body Part\nVive una vita orribile, costretto ad aspettare di prendere il comando del sistema, Iscat Worm Body part prega ogni giorno che la testa e i suoi fratelli e sorelle che stanno davanti a lui muoiano per far si che possa prendere il comando che tanto desidera. Quando Iscat Worm Body Part non ha più una testa da seguire, viene promosso in Iscat Worm Head. (ai suoi fratelli e sorelle Iscat Worm Body Part non piace molto la cosa)")),
            Map.entry("iscat_worm_tail", new BestiaryMenuController.Enemy("Iscat Worm Tail", BASE + "iscat_worm_tail.png",
                    (int) IscatWormSettings.DIM_SPRITE, (int) IscatWormSettings.DIM_SPRITE,
                    "Descrizione Iscat Worm Tail\nIscat Worm Tail NON E' UNA TAIL! Quando Iscat Worm Body Part vede un ##@€# libero e felice, lo cattura e lo fa diventare la tail del sistema. ##@€# una vola libero spara in tutte le direzioni, questi non sono normali proiettili, sono le sue agonie e lacrime, i dolori che ha provato etc etc...\n##@€# è l'ultimo membro del sistema che riceve il cibo, pero il cibo non è mai abbastanza, a causa di questo è magro")),
            Map.entry("iscat_master", new BestiaryMenuController.Enemy("Iscat Master", BASE + "iscat_master.png",
                    128, 128, "Descrizione Iscat Master\nIl creatore di IscatMother, questa creatura misteriosa ha studiato per anni come diventare DIO, durante le sue ricerche ha creato una nuova forma di vita: gli Iscat.\nCon il loro potere IscatMaster ha inziato a portare distruzione e chaos in tutto il mondo"))
    );

    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private TextArea description;

    private StackPane contentRoot;
    private AnimatedCanvas previewCanvas;

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(DISPLAY_SIZE);
        previewContainer.getChildren().add(previewCanvas);
        description.setEditable(false);
        description.setWrapText(true);
        showEnemyById("iscat_mob");
    }

    @FXML
    private void showEnemy(ActionEvent event) {
        if (event.getSource() instanceof Button btn) {
            showEnemyById(btn.getId());
        }
    }

    private void showEnemyById(String id) {
        Enemy enemy = ENEMIES.get(id);
        if (enemy == null) return;
        skinNameLabel.setText(enemy.name().toUpperCase());
        description.setText(enemy.description());
        previewCanvas.setFrameDuration(0.20);
        previewCanvas.loadSkin(enemy.sprite(), enemy.frameW(), enemy.frameH());
        previewCanvas.resize(DISPLAY_SIZE);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, contentRoot);
    }

    @FXML
    private void selectRandom() {
        var validIds = ENEMIES.keySet().stream()
                .filter(id -> !ENEMIES.get(id).name().toUpperCase().equals(skinNameLabel.getText()))
                .toList();
        if (validIds.isEmpty()) return;
        String randomId = validIds.get(ThreadLocalRandom.current().nextInt(validIds.size()));
        showEnemyById(randomId);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}