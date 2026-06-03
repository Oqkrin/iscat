package uni.gaben.iscat.model;

public record ScoreModel(
        int userId,
        int score,
        int deaths,
        int totalDamageDealt,
        int totalDamageReceived,
        int bestTime
) {}