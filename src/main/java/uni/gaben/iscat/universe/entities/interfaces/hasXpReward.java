package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per entità che rilasciano punti esperienza in seguito alla loro distruzione (XP Reward Capability).
 * <p>
 * Fornisce i metodi standard di lettura e scrittura (getter/setter) per determinare quanti punti esperienza (XP)
 * debbano essere accreditati al giocatore (o all'entità uccidente) quando l'oggetto viene rimosso dal mondo di gioco.
 * </p>
 */
public interface hasXpReward {

    /**
     * Restituisce l'ammontare di punti esperienza concessi come ricompensa.
     *
     * @return Il valore numerico dei punti XP rilasciati dall'entità.
     */
    double getXpReward();

    /**
     * Configura dinamicamente il valore della ricompensa in punti esperienza.
     * Permette ai moduli di bilanciamento o ai modificatori di livello (scaling) di alterare i punti XP base dell'entità.
     *
     * @param xp Il nuovo ammontare di punti esperienza da assegnare all'entità.
     */
    void setXpReward(double xp);
}