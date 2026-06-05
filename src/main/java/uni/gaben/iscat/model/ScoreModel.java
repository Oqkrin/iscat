package uni.gaben.iscat.model;

public record ScoreModel(
        int userId,
        int score,
        int totalKills,
        int deaths,
        int totalDamageDealt,
        int totalDamageReceived,
        int bestTime,
        int boostCollected,
        int longestTime,
        int timesPlayed,
        int timesLogged,
        long lastUpdated
) {
    // Costruttore per nuovo utente (valori di default)
    public ScoreModel(int userId) {
        this(userId, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis());
    }

    // Costruttore per caricamento dal DB (senza lastUpdated)
    public ScoreModel(int userId, int score, int totalKills, int deaths,
                      int totalDamageDealt, int totalDamageReceived, int bestTime,
                      int boostCollected, int longestTime, int timesPlayed, int timesLogged) {
        this(userId, score, totalKills, deaths, totalDamageDealt, totalDamageReceived,
                bestTime, boostCollected, longestTime, timesPlayed, timesLogged, System.currentTimeMillis());
    }
}