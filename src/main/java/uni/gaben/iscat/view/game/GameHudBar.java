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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.universe.spawn.UniverseWaveController;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Rappresenta la barra dell'interfaccia utente (HUD) di gioco superiore.
 * Gestisce la visualizzazione del tempo, delle uccisioni, dell'ondata corrente,
 * del livello di minaccia e del numero di nemici rimanenti.
 */
public class GameHudBar extends StackPane {

    /** Famiglia di font utilizzata per i testi dell'HUD. */
    private static final String FONT_FAMILY  = "Miracode";

    /** Dimensione del font per i componenti principali come timer e uccisioni. */
    private static final double FONT_SIZE_LG = 22;

    /** Dimensione del font per il contatore dei nemici rimanenti. */
    private static final double FONT_SIZE_MD = 15;

    /** Dimensione del font per i dettagli secondari come ondata e livello di minaccia. */
    private static final double FONT_SIZE_SM = 12;

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

        controller.getGameModel().timerProperty()
                .addListener((obs, oldV, newV) -> updateTimer(newV.intValue()));
        updateTimer(controller.getGameModel().getTimer());
    }

    /**
     * Inizializza e dispone graficamente tutti i nodi e le etichette all'interno dell'HUD.
     *
     * @param wave Il controller delle ondate per l'inizializzazione iniziale del testo.
     */
    private void buildNodes(UniverseWaveController wave) {
        timerLabel = styledLabel(ICON_TIMER + "  00:00", FONT_SIZE_LG);
        timerLabel.setMinWidth(170);

        Label killsLabel = styledLabel(ICON_KILLS + "  0", FONT_SIZE_LG);
        killsLabel.setMinWidth(170);
        killsLabel.setAlignment(Pos.CENTER_RIGHT);

        killsLabel.textProperty().bind(
                UniverseWaveController.totalKillsProperty().asString(ICON_KILLS + "  %d"));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox headerRow = new HBox(timerLabel, topSpacer, killsLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(0, 4, 4, 4));

        enemiesCounterLabel = styledLabel("Nemici rimanenti: 0 / 0", FONT_SIZE_MD);
        enemiesCounterLabel.setMaxWidth(Double.MAX_VALUE);
        enemiesCounterLabel.setAlignment(Pos.CENTER);

        waveLabel = styledLabel("Wave 1", FONT_SIZE_SM);
        waveLabel.setOpacity(0.55);
        waveLabel.setMaxWidth(Double.MAX_VALUE);
        waveLabel.setAlignment(Pos.CENTER);

        threatLabel = styledLabel("", FONT_SIZE_SM);
        threatLabel.setOpacity(0.7);
        threatLabel.setMaxWidth(Double.MAX_VALUE);
        threatLabel.setAlignment(Pos.CENTER);

        VBox content = new VBox(6, headerRow, enemiesCounterLabel, waveLabel, threatLabel);
        content.setPadding(new Insets(12, 20, 10, 20));
        content.setAlignment(Pos.CENTER);
        content.setMaxHeight(VBox.USE_PREF_SIZE);
        content.setFillWidth(true);

        getChildren().add(content);

        setStyle("-fx-background-color: transparent;");
        setMaxHeight(USE_PREF_SIZE);
        setPickOnBounds(false);
        setFocusTraversable(false);
        setMouseTransparent(true);
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

        //@see UniverseWaveController#currentThreatLevelProperty()
        threatLabel.textProperty().bind(
                Bindings.concat("Minaccia: ", newWave.currentThreatLevelProperty())
        );

        refreshEnemiesLabel(newWave.getEnemiesRemaining(), newWave.getWaveTotal());
        waveLabel.setText("Wave " + newWave.getCurrentWave());
    }

    /**
     * Fabbrica e restituisce una Label pre-configurata secondo i canoni grafici del gioco.
     * Applica il font Miracode, i colori tematici del ThemeManager e un'ombra esterna (dropshadow).
     *
     * @param text  Il testo iniziale da assegnare all'etichetta.
     * @param size  La dimensione del font da impostare.
     * @return      Un oggetto {@link Label} configurato e pronto all'uso.
     */
    private Label styledLabel(String text, double size) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, size));
        lbl.setTextFill(ThemeManager.getInstance().getAccentPrimary());
        lbl.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 6, 0, 0, 0);");
        lbl.setFocusTraversable(false);
        lbl.setMouseTransparent(true);
        return lbl;
    }
}