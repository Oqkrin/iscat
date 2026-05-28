package uni.gaben.iscat.utils;

// tiene conto durante il gioco dei valori che dovranno e potrebbero cambiare durante il game

public class SessionScoreTracker {
    private static SessionScoreTracker instance;

    private int damageDealt = 0;
    private int damageReceived = 0;
    private int score = 0;
    private int deaths = 0;

    private SessionScoreTracker() {}

    public static SessionScoreTracker getInstance() {
        if (instance == null) instance = new SessionScoreTracker();
        return instance;
    }

    public void reset() { damageDealt = 0; damageReceived = 0; score = 0; }

    public void addDamageDealt(int amount)    { damageDealt    += amount; }
    public void addDamageReceived(int amount) { damageReceived += amount; }
    public void addScore(int amount)          { score          += amount; }
    public void addDeaths(int amount)         { deaths         += amount; }

    public int getDamageDealt()    { return damageDealt;    }
    public int getDamageReceived() { return damageReceived; }
    public int getScore()          { return score;          }
    public int getDeaths()         { return deaths;         }
}