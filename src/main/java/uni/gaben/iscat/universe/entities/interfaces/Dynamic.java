package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per la definizione dei vincoli e delle proprietà cinematiche delle entità (Dynamic Motion Profiling).
 * <p>
 * Fornisce i metodi di lettura per le soglie limite necessarie ai controller di movimento (es. Steering ed Evitamento)
 * per calcolare correttamente le forze di spinta (Torque) e le accelerazioni lineari senza violare la stabilità della simulazione fisica.
 * </p>
 */
public interface Dynamic {

    /**
     * @return Il valore nominale di accelerazione lineare massima sacrificabile dall'entità per i calcoli di sterzata.
     */
    double getAcceleration();

    /**
     * @return La velocità lineare di regime (velocità terminale), ovvero il limite asintotico superiore oltre il quale il corpo non può accelerare.
     */
    double getTerminalVelocity();

    /**
     * @return La velocità angolare massima consentita in radianti al secondo per le routine di rotazione e orientamento del corpo rigido.
     */
    double getMaxAngularVelocity();
}