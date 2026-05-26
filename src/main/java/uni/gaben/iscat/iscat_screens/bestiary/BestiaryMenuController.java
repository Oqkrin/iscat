package uni.gaben.iscat.iscat_screens.bestiary;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSettings;
import uni.gaben.iscat.iscat_m_view_c.AnimatedCanvas;
import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.iscat_mv_controller.IscatFxmlController;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static uni.gaben.iscat.universe.enemies.dasher.IscatDasherSettings.ISCATDASHER;
import static uni.gaben.iscat.universe.enemies.eater.IscatEaterSettings.ISCATEATER;
import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;

public class BestiaryMenuController implements IscatFxmlController {

    private record Enemy(
            String name,
            String sprite,
            int frameW,
            int frameH,
            String description
    ) {}

    private static final String BASE = "/uni/gaben/iscat/sprites/enemies/";
    private static final double DISPLAY_SIZE = 160.0;

    private static final Map<String, Enemy> ENEMIES = Map.ofEntries(

            Map.entry("iscat_mob", new Enemy(
                    "Iscat Mob",
                    BASE + "iscat_mob.png",
                    (int) ISCATMOB.dimSprite,
                    (int) ISCATMOB.dimSprite,
                    """
                    Figlio di Iscat Mother, ama mangiare pancakes a colazione
                    e pizza per pranzo e cena.

                    ...Aspetta questa era la descrizione sbagliata.

                    Iscat Mob è un nemico che naviga per lo spazio in cerca di cibo,
                    solitamente in gruppo.

                    Ha poca vita e poca potenza d'attacco.
                    """
            )),

            Map.entry("iscat_bomber", new Enemy(
                    "Iscat Bomber",
                    BASE + "iscat_bomber.png",
                    32,
                    32,
                    """
                    Iscat Bomber vive con un solo obiettivo:
                    esplodere nel modo più spettacolare possibile.

                    Nessuno sa se sia nato così
                    oppure se abbia semplicemente fatto una scelta di vita discutibile.
                    """
            )),

            Map.entry("iscat_core", new Enemy(
                    "Iscat Core",
                    BASE + "iscat_core.png",
                    64,
                    64,
                    """
                    Iscat, Macchina e Roccia che cos'è quest'abominio.
                    Sembra sprizzare magia ma non c'è nulla naturale in lui,
                    Un omuncolo frutto del malsano ingegno del maestro nella sua ricerca perpetua dell'immortalità
                    la spiraleggiante effimerezza dei golem è riuscito ad imbrigliare la creazione di questa nuova specie molti pensano sia solo il bisogno logistico di un esercito, 
                    ma da dove nasce un iscat cosa proteggono i golem domande che un giovane maestro ha risolto, 
                    ora in zone remote dell'universo le madri degli iscat vengono sfruttate curate e torturate e con esse la loro prole, 
                    e da cotanto dolore i cometa golem arrivano da ogni dove in massa; parte della loro linfa magica estratta per sostenere il maestro; 
                    ormai vacui e deboli vengono ammassati e compressati insieme agli iscat che volevano proteggere fusi nelle immonde creature che sono gli iscat core 
                    chimere magiche meccaniche ma vive, questo barlume di vita e magia sprizza via nei suoi ultimi momenti
                    """
            )),

            Map.entry("iscat_mother", new Enemy(
                    "Iscat Mother",
                    BASE + "iscat_mother.png",
                    128,
                    128,
                    """
                    Una gigantesca creatura biologica
                    responsabile della nascita degli Iscat.

                    Molti Space Explorer credono che
                    gli Iscat Mother siano state create artificialmente.

                    Altri sostengono che esistessero
                    molto prima dell'arrivo del Maestro.
                    """
            )),

            Map.entry("fake_iscat", new Enemy(
                    "Fake Iscat",
                    BASE + "fake_iscat.png",
                    32,
                    32,
                    """
                    Era una tranquilla giornata d'autunno
                    quando Fake passeggiava tra i campi ricoperti di brina del suo paese.

                    Improvvisamente,
                    una luce intensa lo avvolse completamente,
                    trascinandolo in un luogo sconosciuto.

                    Al suo risveglio si ritrovò
                    all'interno di una struttura simile a un uovo.

                    Una volta uscito,
                    il suo corpo era cambiato:
                    una fusione anomala tra un Iscat
                    e una forma di vita aliena.

                    Fake Iscat è considerato
                    un'entità potenzialmente pericolosa
                    e deve essere mantenuto sotto stretta osservazione.

                    Deve essere contenuto
                    in una cella rinforzata d'acciaio,
                    monitorata 24 ore su 24,
                    7 giorni su 7.

                    Nel suo arsenale possiede
                    tre differenti tipi di attacco,
                    rendendolo un avversario imprevedibile.

                    Molti Space Explorer lo classificano
                    come un mini-boss,
                    anche se ufficialmente tale denominazione
                    non è riconosciuta.
                    """
            )),

            Map.entry("fallen_star_golem", new Enemy(
                    "Fallen Star Golem",
                    BASE + "fallen_star_golem.png",
                    64,
                    64,
                    """
                    Spiraleggianti creature...
                    o rocce?

                    Sembrano arrivare come stelle cadenti
                    dallo spazio profondo
                    quando gli Iscat sono in pericolo.

                    perché?
                    """
            )),

            Map.entry("eater", new Enemy(
                    "Eater",
                    BASE + "eater.png",
                    (int) ISCATEATER.dimSprite,
                    (int) ISCATEATER.dimSprite,
                    """
                            Descrizione Eater
                            Creatura classificata come ENTITÀ DI LIVELLO: [REDACTED].
                            Dimensioni corporee instabili. Fame: apparentemente infinita.
                            Comportamento: estremamente aggressivo.
                            Eater consuma qualsiasi forma di materia trovata nelle vicinanze.
                            Carne, metallo, energia, rocce, tecnologia, [REDACTED], perfino oggetti normalmente impossibili da ingerire.
                            Non sembra possedere un reale limite biologico.
                            Osservazioni dirette riportano: denti [REDACTED], rumori corporei [REDACTED],
                            odore classificato come [REDACTED], fluidi non identificati di origine [REDACTED].
                            È severamente sconsigliato avvicinarsi oltre il raggio di sicurezza.
                            Incidenti registrati: [REDACTED]
                            Vittime confermate: [REDACTED]
                            Tentativi di contenimento riusciti: 0
                            Ultima frase trasmessa da uno Space Explorer:
                            OH NO, STA [REDACTED] MANGIANDO IL MIO [REDACTED] AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.
                            [NUOVE MISURE ANTI EATER SONO INSTALLATE NEGLI ULTIMI MODELLI DI ASTRONAVE]
                            """
            )),

            Map.entry("iscat_worm_head", new Enemy(
                    "Iscat Worm Head",
                    BASE + "iscat_worm_head.png",
                    (int) IscatWormSettings.DIM_SPRITE,
                    (int) IscatWormSettings.DIM_SPRITE,
                    """
                    Il comandante del sistema ISCAT_WORM.EXE.

                    Iscat Worm Head dirige tutte le parti del corpo
                    e decide la direzione da percorrere.
                    
                    Viaggia per lo spazio senza meta,
                    attaccando chiunque definisca
                    una possibile fonte di cibo.

                    Purtroppo per lui,
                    è costretto a dividere il cibo
                    con tutti gli altri membri del sistema.
                    """
            )),

            Map.entry("iscat_worm_body_part", new Enemy(
                    "Iscat Worm Body Part",
                    BASE + "iscat_worm_body_part.png",
                    (int) IscatWormSettings.DIM_SPRITE,
                    (int) IscatWormSettings.DIM_SPRITE,
                    """
                    Vive una vita orribile.

                    Iscat Worm Body Part aspetta ogni giorno
                    che la testa e i fratelli davanti a lui muoiano,
                    così da poter finalmente prendere il comando.

                    Quando non ha più una testa da seguire,
                    viene promosso a Iscat Worm Head.

                    Ai suoi fratelli e sorelle
                    questa cosa non piace molto.
                    """
            )),

            Map.entry("iscat_worm_tail", new Enemy(
                    "Iscat Worm Tail",
                    BASE + "iscat_worm_tail.png",
                    (int) IscatWormSettings.DIM_SPRITE,
                    (int) IscatWormSettings.DIM_SPRITE,
                    """
                    Iscat Worm Tail NON È UNA TAIL.

                    Quando Iscat Worm Body Part vede un ##@€#
                    libero e felice,
                    lo cattura e lo trasforma
                    nella tail del sistema.

                    Una volta intrappolato,
                    ##@€# spara in tutte le direzioni.

                    Questi non sono normali proiettili.

                    Sono le sue agonie,
                    le sue lacrime,
                    i suoi dolori.

                    È l'ultimo membro del sistema
                    a ricevere il cibo.

                    E il cibo non basta mai.
                    """
            )),

            Map.entry("iscat_master", new Enemy(
                    "Iscat Master",
                    BASE + "iscat_master.png",
                    128,
                    128,
                    """
                    Il creatore di Iscat Mother.

                    Questa creatura misteriosa
                    ha studiato per anni
                    come diventare DIO.

                    Durante le sue ricerche
                    ha creato una nuova forma di vita:
                    gli Iscat.

                    Con il loro potere
                    Iscat Master ha iniziato
                    a portare distruzione e caos
                    in tutto il mondo.
                    """
            )),

            Map.entry("iscat_dasher", new Enemy(
                    "Iscat Dasher",
                    BASE + "iscat_dasher_S.png",
                    (int) ISCATDASHER.dimSprite,
                    (int) ISCATDASHER.dimSprite,
                    """
                    Nessuno riesce a capire
                    se Iscat Dasher stia attaccando
                    oppure abbia semplicemente dimenticato
                    come frenare.
                    """
            ))
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
        previewCanvas.loadSkin(
                enemy.sprite(),
                enemy.frameW(),
                enemy.frameH()
        );

        previewCanvas.resize(DISPLAY_SIZE);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance()
                .navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML
    private void selectRandom() {
        var validIds = ENEMIES.keySet().stream()
                .filter(id ->
                        !ENEMIES.get(id)
                                .name()
                                .toUpperCase()
                                .equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(
                ThreadLocalRandom.current().nextInt(validIds.size())
        );

        showEnemyById(randomId);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}
