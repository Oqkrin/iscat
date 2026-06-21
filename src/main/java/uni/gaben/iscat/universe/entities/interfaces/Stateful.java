package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per entità regolate da macchine a stati finiti (Stateful FSM Architecture).
 * <p>
 * Fornisce i metodi per leggere, sovrascrivere e tracciare l'indice dello stato logico corrente
 * dell'entità (es. IDLE, ATTACK, RUN). Include un contatore temporale dedicato ({@code stateTime})
 * per monitorare l'esatta durata di permanenza nello stato corrente, utile per sincronizzare
 * animazioni frame-by-frame o transizioni temporizzate.
 * </p>
 */
public interface Stateful {

    /**
     * @return L'identificativo numerico (ID) dello stato logico corrente in cui si trova l'entità.
     */
    int getState();

    /**
     * Aggiorna lo stato logico dell'entità.
     * Solitamente l'implementazione si occupa anche di azzerare il contatore {@code stateTime} ad ogni cambio di stato.
     *
     * @param state Il nuovo ID di stato da assegnare.
     */
    void setState(int state);

    /**
     * @return Il tempo accumulato, espresso in secondi, dall'inizio dell'attivazione dello stato corrente.
     */
    double getStateTime();

    /**
     * Imposta direttamente il tempo di permanenza nello stato attuale.
     *
     * @param stateTime Il nuovo valore temporale assoluto in secondi.
     */
    void setStateTime(double stateTime);

    /**
     * Incrementa il contatore temporale dello stato corrente applicando il delta time del frame attivo.
     * Viene invocata ciclicamente all'interno del loop di aggiornamento dell'entità ($stateTime = stateTime + dt$).
     *
     * @param dt Il delta time (tempo in secondi) trascorso dall'ultimo frame di simulazione.
     */
    void updateStateTime(double dt);
}