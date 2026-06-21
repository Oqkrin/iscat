package uni.gaben.iscat.universe.entities.interfaces;

import uni.gaben.iscat.universe.effects.Shockwave;

/**
 * Interfaccia per entità capaci di generare o emettere onde d'urto (Shockwave Capability).
 * <p>
 * Espone il modulo di gestione degli effetti radiali {@link Shockwave}, utilizzato sia per il rendering di feedback visivi
 * sul canvas di gioco (es. anelli di cura o di esplosione), sia per l'applicazione di impulsi di spinta geometrici
 * alle entità circostanti nel mondo di gioco.
 * </p>
 */
public interface HasShockwave {

    /**
     * Restituisce il controller o l'effetto dell'onda d'urto associato a questa entità.
     *
     * @return L'istanza di {@link Shockwave} configurata per l'entità.
     */
    Shockwave shockwave();
}