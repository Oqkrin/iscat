package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.Map;

/**
 * Gestisce il tracciamento e il salvataggio asincrono delle statistiche di fine partita.
 */
public class GameStatsManager {

    /** DAO per la gestione dei record dei punteggi. */
    private final ScoreDAO scoreDAO = IscatDB.getInstance().getScoreDAO();

    /** Tracker delle metriche della sessione corrente. */
    private final SessionScoreTracker tracker = SessionScoreTracker.getInstance();

    /**
     * Elabora e persiste su database le statistiche della sessione di gioco.
     *
     * @param elapsedSeconds Durata della sessione in secondi.
     * @param gameWon        Vero se il giocatore ha vinto la partita.
     * @param isDebugActive  Vero se sono stati attivati cheat o strumenti di debug.
     */
    public void saveStats(int elapsedSeconds, boolean gameWon, boolean isDebugActive) {
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

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