package uni.gaben.iscat.model.game;

/**
 * Enumerazione rappresentante gli stati macroscopici del ciclo di vita di una partita (FSM - Finite State Machine).
 * Regola il comportamento del loop di gioco, determina l'interruzione o la progressione del ticking della fisica
 * e definisce le regole di transizione dei menu al premere del tasto di annullamento (Escape).
 */
public enum GameState {

    /** Sessione di gioco attiva: la fisica, gli input, le IA e il loop principale vengono aggiornati regolarmente. */
    PLAYING,

    /** Sospensione temporanea del gameplay guidata dall'utente, con visualizzazione del menu di overlay di pausa. */
    IN_PAUSE,

    /** Sospensione del gameplay dovuta alla consultazione e modifica dei pannelli di impostazione (audio/video). */
    IN_SETTINGS,

    /** Stato terminale di sconfitta scatenato dal decesso del giocatore; la simulazione fisica è interrotta. */
    GAME_OVER,

    /** Stato terminale di vittoria scatenato dall'abbattimento del boss finale dell'ondata. */
    WIN;

    /**
     * Determina se lo stato corrente impone la sospensione logica del mondo di gioco.
     * <p>
     * Il gioco è considerato in pausa non solo durante la sospensione manuale ({@link #IN_PAUSE}, {@link #IN_SETTINGS}),
     * ma anche nelle schermate conclusive di fine partita ({@link #GAME_OVER}, {@link #WIN}) per congelare la fisica.
     *
     * @return {@code true} se le attività dinamiche dell'universo sono sospese, {@code false} altrimenti.
     */
    public boolean isPaused() {
        return this == IN_PAUSE || this == IN_SETTINGS || this == GAME_OVER || this == WIN;
    }

    /**
     * Verifica se la sessione si trova nella fase di gameplay attivo.
     *
     * @return {@code true} se lo stato è {@link #PLAYING}, {@code false} altrimenti.
     */
    public boolean isPlaying() { return this == PLAYING; }

    /**
     * Verifica se la sessione è terminata a causa della distruzione del giocatore.
     *
     * @return {@code true} se lo stato è {@link #GAME_OVER}, {@code false} altrimenti.
     */
    public boolean isGameOver() { return this == GAME_OVER; }

    /**
     * Regola la macchina a stati definendo la transizione logica da applicare alla ricezione del comando "Escape".
     * Permette l'apertura flessibile della pausa dal gioco, il ritorno al menu precedente dalle impostazioni,
     * o il blocco dello stato se la partita è già conclusa.
     *
     * @return Il prossimo {@link GameState} risultante dall'azione di escape.
     */
    public GameState onEscape() {
        return switch (this) {
            case PLAYING     -> IN_PAUSE;
            case IN_PAUSE    -> PLAYING;
            case IN_SETTINGS -> IN_PAUSE;
            case GAME_OVER   -> GAME_OVER;
            case WIN         -> WIN;
        };
    }
}