package uni.gaben.iscat;

/**
 * Interfaccia per gestire il ciclo di vita delle scene in ISCAT.
 * Ogni scena ha fasi distinte:
 * 1. Costruzione (costruttore) - crea nodi UI ma NON avvia processi
 * 2. onLoad() - chiamato quando la scena sta per essere mostrata
 * 3. onShow() - chiamato quando la scena è visibile
 * 4. onHide() - chiamato quando la scena viene nascosta
 * 5. onUnload() - chiamato quando la scena viene distrutta
 * Questo previene che scene in background consumino risorse.
 */
public interface IscatViewLifecycleInterface {
    
    /**
     * Chiamato quando la scena sta per essere caricata e mostrata.
     * Qui si inizializzano risorse, si caricano dati, si preparano animazioni.
     * Esempio: caricare sprite, inizializzare audio, preparare dati.
     */
    default void onLoad() {}
    
    /**
     * Chiamato quando la scena diventa visibile all'utente.
     * Qui si avviano processi attivi: game loop, animazioni, timer, thread.
     * Esempio: avviare AnimationTimer, iniziare musica di sottofondo.
     */
    default void onShow() {}
    
    /**
     * Chiamato quando la scena viene nascosta (ma non distrutta).
     * Qui si mettono in pausa processi attivi per risparmiare risorse.
     * Esempio: fermare AnimationTimer, mettere in pausa musica.
     */
    default void onHide() {}
    
    /**
     * Chiamato quando la scena viene distrutta e non sarà più usata.
     * Qui si rilasciano risorse: chiudere file, liberare memoria, fermare thread.
     * Esempio: deallocare sprite, chiudere connessioni, fermare thread.
     */
    default void onUnload() {}
    
    /**
     * Indica se la scena è attualmente attiva (visibile e in esecuzione).
     * @return true se la scena è attiva
     */
    boolean isActive();
    
    /**
     * Imposta lo stato attivo della scena.
     * Chiamato automaticamente dal sistema di navigazione.
     * @param active true per attivare, false per disattivare
     */
    void setActive(boolean active);
}
