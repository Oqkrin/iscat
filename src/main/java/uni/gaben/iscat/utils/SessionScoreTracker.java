package uni.gaben.iscat.utils;

/**
 * Tiene traccia durante il gioco dei valori dinamici che cambiano durante la partita.
 * Questi valori verranno poi salvati nel database alla fine della partita.
 */
public class SessionScoreTracker {
    private static SessionScoreTracker instance;

    private int damageDealt = 0;
    private int damageReceived = 0;
    private int score = 0;
    private int deaths = 0;
    private int kills = 0;
    private int boosts = 0;

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
    }

    // Add methods
    public void addDamageDealt(int amount)    { damageDealt    += amount; }
    public void addDamageReceived(int amount) { damageReceived += amount; }
    public void addScore(int amount)          { score          += amount; }
    public void addDeaths(int amount)         { deaths         += amount; }
    public void addKill()                     { kills          += 1;      }
    public void addKills(int amount)          { kills          += amount; }
    public void addBoost()                    { boosts         += 1;      }
    public void addBoosts(int amount)         { boosts         += amount; }

    // Get methods
    public int getDamageDealt()    { return damageDealt;    }
    public int getDamageReceived() { return damageReceived; }
    public int getScore()          { return score;          }
    public int getDeaths()         { return deaths;         }
    public int getKills()          { return kills;          }
    public int getBoosts()         { return boosts;         }
}