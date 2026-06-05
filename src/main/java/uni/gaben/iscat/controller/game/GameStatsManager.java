package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

public class GameStatsManager {

    private final ScoreDAO scoreDAO = IscatDB.getInstance().getScoreDAO();
    private final SessionScoreTracker tracker = SessionScoreTracker.getInstance();

    public void saveStats(int elapsedSeconds) {
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        int userId = user.id();
        final int score = tracker.getScore();
        final int dealt = tracker.getDamageDealt();
        final int received = tracker.getDamageReceived();
        final int deaths = tracker.getDeaths();
        final int kills = tracker.getKills();
        final int boosts = tracker.getBoosts();

        tracker.reset();

        IscatDB.getInstance().executeAsync(() -> {
            ScoreModel current = scoreDAO.load(userId).orElse(new ScoreModel(userId));

            if (score > current.score()) scoreDAO.update(userId, "Score", score);
            if (elapsedSeconds < current.bestTime() || current.bestTime() == 0)
                scoreDAO.update(userId, "BestTime", elapsedSeconds);
            if (elapsedSeconds > current.longestTime())
                scoreDAO.update(userId, "LongestTime", elapsedSeconds);

            scoreDAO.increment(userId, "TotalDamageDealt", dealt);
            scoreDAO.increment(userId, "TotalDamageReceived", received);
            scoreDAO.increment(userId, "Deaths", deaths);
            scoreDAO.increment(userId, "TotalKills", kills);
            scoreDAO.increment(userId, "BoostCollected", boosts);
            scoreDAO.increment(userId, "TimesPlayed", 1);

            scoreDAO.load(userId).ifPresent(data ->
                    Platform.runLater(() -> SessionManager.getInstance().setCurrentSaveData(data)));
        });
    }
}