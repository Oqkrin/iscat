package uni.gaben.iscat.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Tiene traccia durante il gioco dei valori dinamici che cambiano durante la partita.
 * Questi valori verranno poi salvati nel database alla fine della partita.
 */
public class SessionScoreTracker {
    private static SessionScoreTracker instance;

    /** Il danno totale causato ai nemici **/
    private int damageDealt = 0;
    /** Il danno totale ricevuto dai nemici **/
    private int damageReceived = 0;
    /**
     * Lo score accumulato in tempo reale durante la partita.
     * Viene incrementato ad ogni kill (xp + bonus fisso).
     * A fine partita viene aggiunto il timeBonus da GameStatsManager.
     **/
    private int score = 0;
    /** le morti del player **/
    private int deaths = 0;
    /** le kills del player **/
    private int kills = 0;
    /** totale boosts raccolti **/
    private int boosts = 0;
    /** kill per tipo nemico (entityKey -> count), scritte in batch a fine partita **/
    private final Map<String, Integer> enemyKills = new HashMap<>();

    private SessionScoreTracker() {}

    public static SessionScoreTracker getInstance() {
        if (instance == null) instance = new SessionScoreTracker();
        return instance;
    }

    public void reset() {
        damageDealt = 0;
        damageReceived = 0;
        score = 0;
        deaths = 0;
        kills = 0;
        boosts = 0;
        enemyKills.clear();
    }

    // Add methods
    public void addDamageDealt(int amount)    { damageDealt    += amount; }
    public void addDamageReceived(int amount) { damageReceived += amount; }
    public void addScore(int amount)          { score          += amount; }
    public void addDeaths(int amount)         { deaths         += amount; }
    public void addDeath()                    { deaths         += 1;      }
    public void addKill()                     { kills          += 1;      }
    public void addKills(int amount)          { kills          += amount; }
    public void addBoost()                    { boosts         += 1;      }
    public void addBoosts(int amount)         { boosts         += amount; }

    /** Traccia una kill per tipo nemico, da scrivere in batch a fine partita **/
    public void addEnemyKill(String enemyKey) {
        if (enemyKey == null || enemyKey.isEmpty()) return;
        enemyKills.merge(enemyKey, 1, Integer::sum);
    }

    // Get methods
    public int getDamageDealt()              { return damageDealt;    }
    public int getDamageReceived()           { return damageReceived; }
    public int getScore()                    { return score;          }
    public int getDeaths()                   { return deaths;         }
    public int getKills()                    { return kills;          }
    public int getBoosts()                   { return boosts;         }
    public Map<String, Integer> getEnemyKills() { return enemyKills;  }
}