package uni.gaben.iscat.utils;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Traccia le metriche e il punteggio in tempo reale durante la sessione di gioco.
 */
public class SessionScoreTracker {
    private static SessionScoreTracker instance;

    private int damageDealt = 0;
    private int damageReceived = 0;
    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private int deaths = 0;
    private int kills = 0;
    private final Map<String, Integer> enemyKills = new HashMap<>();

    private SessionScoreTracker() {}

    /**
     * Restituisce l'istanza unica del tracker di sessione.
     *
     * @return L'istanza di SessionScoreTracker.
     */
    public static SessionScoreTracker getInstance() {
        if (instance == null) instance = new SessionScoreTracker();
        return instance;
    }

    /**
     * Resetta i contatori di gioco escluso il punteggio, che viene preservato per la UI.
     */
    public void reset() {
        damageDealt = 0;
        damageReceived = 0;
        deaths = 0;
        kills = 0;
        enemyKills.clear();
    }

    /**
     * Ripristina a zero il punteggio della sessione.
     */
    public void resetScore() {
        score.set(0);
    }

    public void addDamageDealt(int amount)    { damageDealt    += amount; }
    public void addDamageReceived(int amount) { damageReceived += amount; }
    public void addScore(int amount)          { score.set(score.get() + amount); }
    public void addDeaths(int amount)         { deaths         += amount; }
    public void addDeath()                    { deaths         += 1;      }
    public void addKill()                     { kills          += 1;      }
    public void addKills(int amount)          { kills          += amount; }

    /**
     * Incrementa il contatore delle uccisioni per una specifica tipologia di nemico.
     *
     * @param enemyKey Chiave identificativa del nemico.
     */
    public void addEnemyKill(String enemyKey) {
        if (enemyKey == null || enemyKey.isEmpty()) return;
        enemyKills.merge(enemyKey, 1, Integer::sum);
    }

    public int getDamageDealt()                 { return damageDealt;    }
    public int getDamageReceived()              { return damageReceived; }
    public int getScore()                       { return score.get();    }
    public IntegerProperty scoreProperty()      { return score;          }
    public int getDeaths()                      { return deaths;         }
    public int getKills()                       { return kills;          }
    public Map<String, Integer> getEnemyKills() { return enemyKills;     }
}