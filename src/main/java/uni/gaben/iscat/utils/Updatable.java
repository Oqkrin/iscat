package uni.gaben.iscat.utils;

/**
 * Interfaccia funzionale che definisce un componente aggiornabile nel tempo.
 * Viene implementata da tutti gli oggetti del motore di gioco che richiedono un aggiornamento
 * logico, fisico o grafico continuo all'interno del Game Loop principale.
 * * <p>Essendo un'interfaccia funzionale, può essere utilizzata come target per espressioni lambda
 * o riferimenti a metodi (method references).</p>
 */
@FunctionalInterface
public interface Updatable {

    /**
     * Aggiorna lo stato interno del componente in base al tempo trascorso.
     * Questo metodo viene invocato automaticamente a ogni iterazione (frame) del Game Loop.
     *
     * @param dt Il "Delta Time", ovvero il tempo trascorso dall'ultimo fotogramma
     * rispetto a quello corrente, espresso in secondi (o frazioni di secondo).
     */
    void update(double dt);
}