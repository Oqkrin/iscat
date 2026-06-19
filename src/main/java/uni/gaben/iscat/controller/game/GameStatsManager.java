package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.Map;

public class GameStatsManager {

    private final ScoreDAO scoreDAO = IscatDB.getInstance().getScoreDAO();
    private final SessionScoreTracker tracker = SessionScoreTracker.getInstance();

    /**
     * Salva le statistiche di fine partita nel Database.
     * Se l'utente ha barato o usato i tool di debug, il salvataggio viene bypassato.
     */
    public void saveStats(int elapsedSeconds, boolean gameWon, boolean isDebugActive) {
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        // Se il debug è stato attivato, puliamo i dati correnti ma non scriviamo sul DB
        if (isDebugActive) {
            System.out.println("[STATS] Partita terminata con Debug Mode attivo. Progressi non salvati.");
            tracker.reset();
            return;
        }

        int userId = user.id();
        final int dealt      = tracker.getDamageDealt();
        final int received   = tracker.getDamageReceived();
        final int deaths     = tracker.getDeaths();
        final int kills      = tracker.getKills();
        final int boosts     = tracker.getBoosts();

        final int timeBonus    = Math.max(0, 10000 - elapsedSeconds * 10);
        final int sessionScore = tracker.getScore() + timeBonus;

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

            enemyKills.forEach((key, count) -> {
                if (count > 0) {
                    IscatDB.getInstance().getBestiaryDAO().incrementKill(userId, key, count);
                }
            });

            scoreDAO.load(userId).ifPresent(data ->
                    Platform.runLater(() -> SessionManager.getInstance().setCurrentSaveData(data)));
        });
    }
}