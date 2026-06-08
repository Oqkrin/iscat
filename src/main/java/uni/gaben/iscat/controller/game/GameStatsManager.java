package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.EnemyDAO;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.Map;

public class GameStatsManager {

    private final ScoreDAO scoreDAO = IscatDB.getInstance().getScoreDAO();
    private final EnemyDAO enemyDAO = IscatDB.getInstance().getEnemyDAO();
    private final SessionScoreTracker tracker = SessionScoreTracker.getInstance();

    public void saveStats(int elapsedSeconds, boolean gameWon) {
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        int userId = user.id();
        final int dealt      = tracker.getDamageDealt();
        final int received   = tracker.getDamageReceived();
        final int deaths     = tracker.getDeaths();
        final int kills      = tracker.getKills();
        final int boosts     = tracker.getBoosts();

        // Score accumulato in tempo reale + bonus tempo calcolato a fine partita
        // Il gioco premia chi finisce prima: più tempo = meno bonus
        final int timeBonus    = Math.max(0, 10000 - elapsedSeconds * 10);
        final int sessionScore = tracker.getScore() + timeBonus;

        // Snapshot della mappa enemy kills prima del reset
        final Map<String, Integer> enemyKills = Map.copyOf(tracker.getEnemyKills());

        tracker.reset();

        IscatDB.getInstance().executeAsync(() -> {
            ScoreModel current = scoreDAO.load(userId).orElse(new ScoreModel(userId));

            if (gameWon) {
                if (sessionScore > current.score())
                    scoreDAO.update(userId, "Score", sessionScore);
                if (elapsedSeconds < current.bestTime() || current.bestTime() == 0)
                    scoreDAO.update(userId, "BestTime", elapsedSeconds);
            }
            if (elapsedSeconds > current.longestTime())
                scoreDAO.update(userId, "LongestTime", elapsedSeconds);

            scoreDAO.increment(userId, "TotalDamageDealt",    dealt);
            scoreDAO.increment(userId, "TotalDamageReceived", received);
            scoreDAO.increment(userId, "Deaths",              deaths);
            scoreDAO.increment(userId, "TotalKills",          kills);
            scoreDAO.increment(userId, "BoostCollected",      boosts);
            scoreDAO.increment(userId, "TimesPlayed",         1);

            // Salva kill per tipo nemico in batch
            enemyKills.forEach((key, count) ->
                    enemyDAO.incrementKill(userId, key, count));

            scoreDAO.load(userId).ifPresent(data ->
                    Platform.runLater(() -> SessionManager.getInstance().setCurrentSaveData(data)));
        });
    }
}