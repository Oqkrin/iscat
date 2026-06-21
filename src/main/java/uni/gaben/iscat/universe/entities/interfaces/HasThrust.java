package uni.gaben.iscat.universe.entities.interfaces;

import uni.gaben.iscat.universe.effects.Thrust;

/**
 * Interfaccia per entità dotate di sistemi di propulsione o motori a spinta (Thrust Capability).
 * <p>
 * Espone il modulo di gestione degli effetti di spinta {@link Thrust}, utilizzato dal ciclo grafico
 * per emettere, renderizzare e orientare flussi di particelle (es. scie di fumo, fiamme o postbruciatori)
 * in corrispondenza delle accelerazioni lineari applicate all'entità nel mondo di gioco.
 * </p>
 */
public interface HasThrust {

    /**
     * Restituisce il controller o l'effetto grafico della spinta associato a questa entità.
     *
     * @return L'istanza di {@link Thrust} configurata per gestire l'effetto visivo di propulsione.
     */
    Thrust thrust();
}