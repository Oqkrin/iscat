package uni.gaben.iscat.universe.spawn.waves;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import uni.gaben.iscat.universe.entities.ThreatLevel;

/**
 * Modello dati atomico e reattivo dello stato delle ondate (Wave State).
 * Centralizza le variabili di runtime e le proprietà JavaFX per il data-binding con l'HUD della UI.
 */
public final class WaveState {

    // --- Proprietà JavaFX per il Data-Binding ---
    private static final IntegerProperty totalKillsProperty = new SimpleIntegerProperty(0);
    public final IntegerProperty enemiesRemainingProperty = new SimpleIntegerProperty(0);
    public final IntegerProperty waveTotalProperty         = new SimpleIntegerProperty(0);
    public final IntegerProperty currentWaveProperty      = new SimpleIntegerProperty(1);
    public final StringProperty currentThreatLevelProperty = new SimpleStringProperty("LOW");

    public static IntegerProperty totalKillsProperty() { return totalKillsProperty; }

    // --- Stati Logici e Flag di Partita ---
    public boolean bossSpawned = false;
    public boolean bossDead    = false;
    public boolean gameWon     = false;
    public boolean victoryCallbackTriggered = false;
    public boolean inIntermission           = true;

    // --- Primitive di Configurazione e Progressione ---
    public int currentWave                  = 1;
    public ThreatLevel currentThreatLevel   = ThreatLevel.LOW;
    public int totalEnemiesToSpawnThisWave  = 0;
    public int enemiesSpawnedThisWave       = 0;

    // --- Timer di Runtime (in secondi) ---
    public double victoryTimer              = 0.0;
    public double intermissionTimer         = 5.0;
    public double spawnTimer                = 0.0;

    /**
     * Ripristina integralmente tutti i parametri, i timer e le proprietà osservabili allo stato iniziale.
     *
     * @param initialThreat Il livello di minaccia predefinito impostato per il primo round.
     */
    public void reset(ThreatLevel initialThreat) {
        bossSpawned = false;
        bossDead = false;
        gameWon = false;
        victoryTimer = 0.0;
        victoryCallbackTriggered = false;
        currentWave = 1;
        currentThreatLevel = initialThreat;
        currentThreatLevelProperty.set(initialThreat.name());

        enemiesSpawnedThisWave = 0;
        totalEnemiesToSpawnThisWave = 0;
        inIntermission = true;
        intermissionTimer = 5.0;
        spawnTimer = 0.0;

        totalKillsProperty.set(0);
        enemiesRemainingProperty.set(0);
        waveTotalProperty.set(0);
        currentWaveProperty.set(1);
    }
}