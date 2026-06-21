package uni.gaben.iscat.model;

/**
 * Record immutabile che rappresenta il modello delle statistiche e dei punteggi di un utente.
 * Memorizza in modo centralizzato tutte le metriche di gioco cumulative, i record di tempo,
 * i dati analitici di combattimento e i contatori di attività necessari per la leaderboard e il profilo.
 *
 * @param userId              L'identificativo univoco numerico dell'utente associato alle statistiche.
 * @param score               Il punteggio cumulativo totale ottenuto dal giocatore.
 * @param totalKills          Il numero complessivo di nemici sconfitti.
 * @param deaths              Il numero totale di morti/sconfitte subite dal giocatore.
 * @param totalDamageDealt    La quantità totale di danni inflitti ai nemici durante le sessioni.
 * @param totalDamageReceived La quantità totale di danni subiti dalle entità ostili o trappole.
 * @param bestTime            Il miglior tempo di completamento (espresso in secondi) registrato.
 * @param longestTime         La sessione di gioco singola più lunga registrata (espressa in secondi).
 * @param timesPlayed         Il numero totale di partite/sessioni di gioco avviate.
 * @param timesLogged         Il numero complessivo di accessi (login) effettuati all'applicazione.
 * @param lastUpdated         Il timestamp UNIX (in millisecondi) che traccia l'ultimo aggiornamento del record.
 */
public record ScoreModel(
        int userId,
        int score,
        int totalKills,
        int deaths,
        int totalDamageDealt,
        int totalDamageReceived,
        int bestTime,
        int longestTime,
        int timesPlayed,
        int timesLogged,
        long lastUpdated
) {

    /**
     * Costruttore secondario per l'inizializzazione di un nuovo utente.
     * Genera un'istanza azzerata con i valori di default predefiniti e imposta il timestamp corrente.
     *
     * @param userId L'identificativo univoco numerico del nuovo utente.
     */
    public ScoreModel(int userId) {
        this(userId, 0, 0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis());
    }

    /**
     * Costruttore secondario adibito al caricamento dei dati persistiti dal database.
     * Inizializza le statistiche storiche valorizzando automaticamente il timestamp di sistema corrente.
     *
     * @param userId              L'identificativo univoco numerico dell'utente.
     * @param score               Il punteggio cumulativo salvato.
     * @param totalKills          Il numero di uccisioni salvato.
     * @param deaths              Il numero di morti salvato.
     * @param totalDamageDealt    I danni inflitti complessivi.
     * @param totalDamageReceived I danni subiti complessivi.
     * @param bestTime            Il record del miglior tempo di completamento.
     * @param longestTime         Il record della sessione più lunga.
     * @param timesPlayed         Il conteggio delle partite giocate.
     * @param timesLogged         Il conteggio dei login effettuati.
     */
    public ScoreModel(int userId, int score, int totalKills, int deaths,
                      int totalDamageDealt, int totalDamageReceived, int bestTime,
                      int longestTime, int timesPlayed, int timesLogged) {
        this(userId, score, totalKills, deaths, totalDamageDealt, totalDamageReceived,
                bestTime, longestTime, timesPlayed, timesLogged, System.currentTimeMillis());
    }
}