package uni.gaben.iscat.utils;

/**
 * Gestore del tempo di ricarica (cooldown) basato sul tempo di gioco.
 * Supporta due modalità di funzionamento distinte:
 * <ul>
 * <li><b>Variabile</b> – Inizializzato tramite {@code new Cooldown()};
 * richiede il passaggio esplicito del tempo ad ogni attivazione con {@link #start(double)}.</li>
 * <li><b>Fisso</b> – Inizializzato tramite {@code new Cooldown(double)};
 * può essere attivato rapidamente senza argomenti tramite {@link #start()} usando il valore preimpostato.</li>
 * </ul>
 */
public class Cooldown implements Updatable {

    private double maxDuration = 0;
    private double timeRemaining = 0;
    private double defaultDuration;

    /**
     * Costruttore per cooldown a durata variabile.
     * Pronto all'uso, ma richiede la specifica della durata al momento dello start.
     */
    public Cooldown() {
        this.defaultDuration = -1;  // Indicatore interno: nessuna durata di default impostata
    }

    /**
     * Costruttore per cooldown a durata fissa/predefinita.
     *
     * @param defaultDuration Durata standard del cooldown espressa in secondi.
     */
    public Cooldown(double defaultDuration) {
        this.defaultDuration = Math.max(0, defaultDuration);
    }

    /**
     * Avvia il cooldown utilizzando il tempo predefinito configurato nel costruttore o tramite setter.
     * * @throws IllegalStateException Se non è stata impostata alcuna durata di default.
     */
    public void start() {
        if (defaultDuration < 0) {
            throw new IllegalStateException("Nessuna durata di default impostata; utilizzare start(double)");
        }
        start(defaultDuration);
    }

    /**
     * Avvia il cooldown impostando una durata specifica in secondi.
     * Questo metodo aggiorna anche la durata massima per consentire il corretto calcolo della percentuale di progresso.
     *
     * @param durationInSeconds La durata della ricarica espressa in secondi.
     */
    public void start(double durationInSeconds) {
        this.maxDuration = Math.max(0, durationInSeconds);
        this.timeRemaining = this.maxDuration;
    }

    /**
     * Aggiorna lo stato del timer di ricarica. Deve essere invocato ad ogni frame del Game Loop.
     *
     * @param dt Il tempo trascorso dall'ultimo frame (Delta Time) espresso in secondi.
     */
    @Override
    public void update(double dt) {
        if (timeRemaining > 0) {
            timeRemaining = Math.max(0, timeRemaining - dt);
        }
    }

    /** Verifica se il cooldown è terminato e l'azione è nuovamente disponibile. */
    public boolean isReady() {
        return timeRemaining <= 0;
    }

    /** Verifica se il cooldown è ancora in esecuzione. */
    public boolean isCoolingDown() {
        return timeRemaining > 0;
    }

    /** Annulla istantaneamente il tempo rimanente, rendendo il cooldown subito pronto. */
    public void reset() {
        this.timeRemaining = 0;
    }

    public void setDefaultDuration(double defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public double getDefaultDuration() {
        return defaultDuration;
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Restituisce il progresso normalizzato del cooldown, ideale per mappare barre di ricarica visive (UI).
     * * @return Un valore double compreso tra {@code 0.0} (cooldown appena iniziato) e {@code 1.0} (cooldown completato / pronto).
     */
    public double getProgress() {
        if (maxDuration <= 0) {
            return 1.0;
        }
        return 1.0 - (timeRemaining / maxDuration);
    }
}