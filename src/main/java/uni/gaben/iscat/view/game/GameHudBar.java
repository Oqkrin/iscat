package uni.gaben.iscat.view.game;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.Objects;

/**
 * Rappresenta la barra dell'interfaccia utente (HUD) di gioco superiore.
 * Gestisce la visualizzazione del tempo, delle uccisioni, dell'ondata corrente,
 * del livello di minaccia e del numero di nemici rimanenti.
 */
public class GameHudBar extends StackPane {

    /** Icona testuale utilizzata per identificare il timer di gioco. */
    private static final String ICON_TIMER   = "⏱";

    /** Icona testuale utilizzata per identificare il contatore delle uccisioni. */
    private static final String ICON_KILLS   = "☠";

    /** Etichetta per la visualizzazione del tempo di gioco trascorso. */
    private Label timerLabel;

    /** Etichetta per mostrare il rapporto tra nemici rimanenti e nemici totali dell'ondata. */
    private Label enemiesCounterLabel;

    /** Etichetta per indicare il numero dell'ondata corrente. */
    private Label waveLabel;

    /** Etichetta dinamica per il livello di minaccia attuale. */
    private Label threatLabel;

    /**
     * Costruisce una nuova barra dell'HUD e inizializza i binding con i controller necessari.
     *
     * @param controller Il controller principale del gioco da cui ricavare i modelli e i sotto-controller.
     */
    public GameHudBar(GameController controller) {
        buildNodes(controller.getUniverseWaveController());
        bindToWaveController(controller.getUniverseWaveController());
        applyStyles();

        controller.getGameModel().timerProperty()
                .addListener((obs, oldV, newV) -> updateTimer(newV.intValue()));
        updateTimer(controller.getGameModel().getTimer());
    }

    /** Dispone geometricamente i componenti dell'HUD (allineamento righe e box verticali). */
    private void buildNodes(UniverseWaveController wave) {
        final double SU = IscatSettings.STANDARD_UNIT;

        timerLabel = styledLabel(ICON_TIMER + "  00:00", "large");

        Label killsLabel = styledLabel(ICON_KILLS + "  0", "large");
        killsLabel.setAlignment(Pos.CENTER_RIGHT);
        killsLabel.textProperty().bind(
                UniverseWaveController.totalKillsProperty().asString(ICON_KILLS + "  %d"));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        // ---- Header row (timer + kills) ----
        HBox headerRow = new HBox(timerLabel, topSpacer, killsLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(4));
        headerRow.setMaxHeight(SU * 3.5);
        headerRow.setPrefHeight(SU * 3.5);

        // ---- Label inferiori (nemici, ondata, minaccia) ----
        enemiesCounterLabel = styledLabel("Nemici rimanenti: 0 / 0", "medium");
        enemiesCounterLabel.setMaxWidth(Double.MAX_VALUE);
        enemiesCounterLabel.setAlignment(Pos.CENTER);

        waveLabel = styledLabel("Wave 1", "small");
        waveLabel.setMaxWidth(Double.MAX_VALUE);
        waveLabel.setAlignment(Pos.CENTER);

        threatLabel = styledLabel("", "threat");
        threatLabel.setMaxWidth(Double.MAX_VALUE);
        threatLabel.setAlignment(Pos.CENTER);

        VBox labelsBox = new VBox(SU / 2, enemiesCounterLabel, waveLabel, threatLabel);
        labelsBox.setAlignment(Pos.CENTER);
        labelsBox.setFillWidth(true);

        // ---- Contenitore principale ----
        VBox content = new VBox(0, headerRow, labelsBox);
        content.setPadding(new Insets(0, SU, SU, SU)); // top padding = 0
        content.setAlignment(Pos.TOP_CENTER);
        content.setFillWidth(true);

        getChildren().add(content);

        setMaxHeight(USE_PREF_SIZE);
        setPickOnBounds(false);
        setFocusTraversable(false);
        setMouseTransparent(true);
    }

    /** Carica il foglio di stile CSS esterno dedicato all'HUD di gioco. */
    private void applyStyles() {
        getStyleClass().add("game-hud-container");
        try {
            String cssPath = Objects.requireNonNull(getClass().getResource("/uni/gaben/iscat/styles/screens/game/game-hud.css")).toExternalForm();
            getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("[GameHudBar] Impossibile caricare il foglio di stile: game-hud.css");
        }
    }

    /**
     * Collega le proprietà e i listener dell'HUD alle proprietà osservabili del controller delle ondate.
     *
     * @param wave Il controllore delle ondate a cui connettere i listener grafici.
     */
    private void bindToWaveController(UniverseWaveController wave) {
        wave.enemiesRemainingProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                newV.intValue(), wave.waveTotalProperty().get()));
        wave.waveTotalProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                wave.enemiesRemainingProperty().get(), newV.intValue()));

        wave.currentWaveProperty().addListener((obs, oldV, newV) ->
                waveLabel.setText("Wave " + newV.intValue()));

        threatLabel.textProperty().bind(
                Bindings.concat("Minaccia: ", wave.currentThreatLevelProperty())
        );

        refreshEnemiesLabel(wave.getEnemiesRemaining(), wave.getWaveTotal());
        waveLabel.setText("Wave " + wave.getCurrentWave());
    }

    /**
     * Aggiorna il testo dell'etichetta dei nemici rimanenti in base ai valori correnti.
     *
     * @param remaining Numero di nemici attualmente vivi nella mappa.
     * @param total     Numero totale di nemici previsti per l'ondata attuale.
     */
    private void refreshEnemiesLabel(int remaining, int total) {
        enemiesCounterLabel.setText("Nemici rimanenti: " + remaining + " / " + total);
    }

    /**
     * Converte il valore numerico del timer in stringa formattata HH:MM:SS o MM:SS e aggiorna la UI.
     *
     * @param val Il valore intero grezzo del timer proveniente dal GameModel.
     */
    public void updateTimer(int val) {
        int hours   = val / 10000;
        int minutes = (val % 10000) / 100;
        int seconds = val % 100;
        String time = hours > 0
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
        timerLabel.setText(ICON_TIMER + "  " + time);
    }

    /**
     * Disconnette i vecchi binding e riaggancia i listener grafici a una nuova istanza di gioco.
     * Utility fondamentale per resettare i componenti visivi in caso di Retry della partita.
     *
     * @param newWave La nuova istanza del controllore delle ondate da osservare.
     */
    public void rebindToWaveController(UniverseWaveController newWave) {
        threatLabel.textProperty().unbind();

        newWave.enemiesRemainingProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                newV.intValue(), newWave.waveTotalProperty().get()));
        newWave.waveTotalProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                newWave.enemiesRemainingProperty().get(), newWave.waveTotalProperty().get()));

        newWave.currentWaveProperty().addListener((obs, oldV, newV) -> {
            waveLabel.setText("Wave " + newV.intValue());
        });

        threatLabel.textProperty().bind(
                Bindings.concat("Minaccia: ", newWave.currentThreatLevelProperty())
        );

        refreshEnemiesLabel(newWave.getEnemiesRemaining(), newWave.getWaveTotal());
        waveLabel.setText("Wave " + newWave.getCurrentWave());
    }

    /** Instanzia una Label disabilitando il focus e applicando le classi di stile e i colori del tema. */
    private Label styledLabel(String text, String sizeStyleClass) {
        Label lbl = new Label(text);
        lbl.getStyleClass().addAll("hud-label", sizeStyleClass);
        lbl.setTextFill(ThemeManager.getInstance().getAccentPrimary());
        lbl.setFocusTraversable(false);
        lbl.setMouseTransparent(true);
        return lbl;
    }
}