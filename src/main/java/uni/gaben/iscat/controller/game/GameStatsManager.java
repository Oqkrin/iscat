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
 * Manager addetto al tracciamento, all'elaborazione e al salvataggio persistente delle statistiche
 * e dei record di fine partita nel database di gioco.
 * Estrae i dati accumulati dal {@link SessionScoreTracker}, calcola i bonus di tempo, aggiorna il bestiario
 * ed esegue le query di incremento e aggiornamento in modalità asincrona per non bloccare il thread grafico.
 * Include controlli di sicurezza per invalidare il salvataggio dei record nel caso in cui siano stati usati cheat.
 */
public class GameStatsManager {

    /** Data Access Object (DAO) per l'interfacciamento e la manipolazione dei record della tabella Score. */
    private final ScoreDAO scoreDAO = IscatDB.getInstance().getScoreDAO();

    /** Registro singleton di sessione per il recupero delle metriche di combattimento e dei punteggi correnti. */
    private final SessionScoreTracker tracker = SessionScoreTracker.getInstance();

    /**
     * Elabora e persiste nel database le statistiche accumulate durante l'ultima sessione di gioco.
     * <p>
     * Se la modalità debug o i trucchi risultano attivati, la procedura pulisce i buffer temporanei
     * e interrompe l'esecuzione per prevenire la corruzione delle classifiche.
     * In caso di salvataggio valido, calcola un bonus di punteggio inversamente proporzionale al tempo trascorso,
     * aggiorna asincronamente i record globali del profilo utente (vittorie, uccisioni, danni, tempi) e
     * incrementa i contatori dei singoli nemici abbattuti nel bestiario.
     *
     * @param elapsedSeconds Il tempo totale di gioco effettivo trascorso nella sessione, espresso in secondi.
     * @param gameWon        Flag che indica se la partita si è conclusa con la vittoria del giocatore.
     * @param isDebugActive  Flag di sicurezza che indica se i tool di debug o cheat sono stati usati nella sessione.
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

        // Calcolo del bonus di punteggio legato alla velocità di completamento
        final int timeBonus    = Math.max(0, 10000 - elapsedSeconds * 10);
        final int sessionScore = tracker.getScore() + timeBonus;

        // Clonazione immutabile della mappa dei nemici uccisi prima del reset del tracker
        final Map<String, Integer> enemyKills = Map.copyOf(tracker.getEnemyKills());

        // Resetta immediatamente il tracker per renderlo disponibile per la partita successiva
        tracker.reset();

        // Avvia il thread asincrono per le operazioni di scrittura su database (I/O)
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

            // Aggiorna le statistiche individuali dei nemici abbattuti nel Bestiario
            enemyKills.forEach((key, count) -> {
                if (count > 0) {
                    IscatDB.getInstance().getBestiaryDAO().incrementKill(userId, key, count);
                }
            });

            // Ricarica i dati aggiornati e notifica il SessionManager sul thread grafico di JavaFX
            scoreDAO.load(userId).ifPresent(data ->
                    Platform.runLater(() -> SessionManager.getInstance().setCurrentSaveData(data)));
        });
    }
}