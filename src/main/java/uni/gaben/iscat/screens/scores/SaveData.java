package uni.gaben.iscat.screens.scores;

public record SaveData(
        int userId,
        int score,
        int deaths,
        int totalDamageDealt,
        int totalDamageReceived,
        int bestTime
) {}