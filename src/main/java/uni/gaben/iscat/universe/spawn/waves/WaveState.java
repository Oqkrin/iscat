package uni.gaben.iscat.universe.spawn.waves;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import uni.gaben.iscat.universe.entities.ThreatLevel;

/**
 * Contenitore atomico dello stato logico e delle proprietà osservabili delle ondate.
 */
public final class WaveState {

    private static final IntegerProperty totalKillsProperty = new SimpleIntegerProperty(0);

    public final IntegerProperty enemiesRemainingProperty = new SimpleIntegerProperty(0);
    public final IntegerProperty waveTotalProperty         = new SimpleIntegerProperty(0);
    public final IntegerProperty currentWaveProperty      = new SimpleIntegerProperty(1);
    public final StringProperty currentThreatLevelProperty = new SimpleStringProperty("LOW");

    public static IntegerProperty totalKillsProperty() { return totalKillsProperty; }

    // Flag e primitive di tracciamento
    public boolean bossSpawned = false;
    public boolean bossDead    = false;
    public boolean gameWon     = false;

    public double victoryTimer              = 0.0;
    public boolean victoryCallbackTriggered = false;

    public int currentWave                  = 1;
    public ThreatLevel currentThreatLevel   = ThreatLevel.LOW;

    public int totalEnemiesToSpawnThisWave  = 0;
    public int enemiesSpawnedThisWave       = 0;

    public boolean inIntermission           = true;
    public double intermissionTimer         = 5.0;
    public double spawnTimer                = 0.0;

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