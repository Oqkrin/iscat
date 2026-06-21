package uni.gaben.iscat.universe.entities.worm;

import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityBrain;
import uni.gaben.iscat.universe.entities.brain.rotation.RotationGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entities.brain.target.Target;

/**
 * Assemblatore e costruttore procedurale per entità verme (Worm Composite Assembler).
 * <p>
 * Implementa un pattern Factory statico per istanziare, posizionare e concatenare i segmenti
 * strutturali di un verme (Testa, N corpi, Coda). Configura i filtri di collisione fisici unificati
 * e i target di inseguimento iniziali per l'Intelligenza Artificiale prima di iniettare l'organismo nel loop di gioco.
 * </p>
 */
public class WormAssembler {

    /**
     * Costruisce e assembla un intero verme segmentato nel mondo di gioco.
     * <ul>
     * <li>Istanzia la testa impostando come steering goal l'inseguimento attivo del giocatore ({@code Target.ofPlayer()}).</li>
     * <li>Genera sequenzialmente il numero richiesto di segmenti del corpo, applicando un offset spaziale sull'asse Y
     * e configurando l'IA di ogni nodo affinché punti e segua il segmento immediatamente precedente (Leader-Follower chain).</li>
     * <li>Posiziona e aggancia il segmento finale di coda.</li>
     * <li>Registra il {@link WormChainController} globale all'interno del gestore dell'universo.</li>
     * </ul>
     *
     * @param headKey    Chiave di registro per il modello della testa.
     * @param bodyKey    Chiave di registro per il modello del corpo.
     * @param tailKey    Chiave di registro per il modello della coda.
     * @param bodyCount  Numero totale di segmenti centrali (corpo) da interporre tra testa e coda.
     * @param startX     Coordinata X di origine per il posizionamento iniziale del verme.
     * @param startY     Coordinata Y di origine per il posizionamento iniziale del verme.
     * @return L'istanza {@link EntityModel} della testa del verme, utilizzabile come punto di riferimento principale.
     */
    public static EntityModel assemble(
            String headKey, String bodyKey, String tailKey,
            int bodyCount, double startX, double startY,
            UniverseModel universe, UniverseController controller) {

        WormChainController chain = new WormChainController(controller, headKey, bodyKey, tailKey);
        EntityModel previousModel = null;

        // Fase 1: Spawn e configurazione della TESTA
        EntityModel head = spawnSegment(headKey, startX, startY, universe, controller, chain, true, false);
        if (head != null) {
            EntityBrain headBrain = chain.getLatestBrain();
            if (headBrain != null) {
                // La testa punta e insegue direttamente la navicella del giocatore
                headBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 1.0));
            }
            previousModel = head;
        }

        // Fase 2: Spawn sequenziale dei segmenti del CORPO
        for (int i = 0; i < bodyCount; i++) {
            // Calcolo dell'offset progressivo lineare lungo l'asse Y per evitare la sovrapposizione iniziale dei corpi rigidi
            double offsetY = startY + ((i + 1) * 2.0);
            EntityModel body = spawnSegment(bodyKey, startX, offsetY, universe, controller, chain, false, false);

            if (body != null && previousModel != null) {
                EntityBrain bodyBrain = chain.getLatestBrain();
                if (bodyBrain != null) {
                    // Configurazione a catena: ogni nodo insegue e guarda il rispettivo "leader" precedente
                    bodyBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofEntity(previousModel), 0.5));
                    bodyBrain.setRotationGoal(RotationGoal.target(Target.ofEntity(previousModel)));
                }
                previousModel = body;
            }
        }

        // Fase 3: Spawn e configurazione della CODA
        double tailOffsetY = startY + ((bodyCount + 1) * 2.0);
        EntityModel tail = spawnSegment(tailKey, startX, tailOffsetY, universe, controller, chain, false, true);
        if (tail != null && previousModel != null) {
            EntityBrain tailBrain = chain.getLatestBrain();
            if (tailBrain != null) {
                tailBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofEntity(previousModel), 0.0));
            }
        }

        // Iniezione del controllore della catena nel ciclo di update principale dell'universo
        controller.addEntityController(chain);

        return head;
    }

    /**
     * Routine interna helper per l'estrazione dal registro, l'allocazione fisica e la registrazione del singolo nodo.
     * Garantisce l'applicazione uniforme del filtro di collisione dedicato ai vermi per gestire
     * correttamente i contatti di dyn4j ed evita sovrapposizioni interne dannose.
     */
    private static EntityModel spawnSegment(String key, double x, double y,
                                            UniverseModel universe, UniverseController controller,
                                            WormChainController chain, boolean isHead, boolean isTail) {

        EntityRecord record = EntityFactory.getCache().get(key.toLowerCase().trim());
        if (record == null) return null;

        EntityModel model = new EntityModel(x, y, record);
        EntityBrain brain = EntityBrain.fromRecord(model);

        // Applica il filtro di mascheramento WORM a tutti i corpi rigidi del modulo
        model.setCollisionFilter(UniverseCollisionLayers.WORM_BODY_FILTER);

        // Registrazione dell'entità e del suo cervello nei rispettivi sistemi dell'universo
        universe.addEntity(model);
        controller.addEntityController(brain);

        // Registrazione del segmento nel controller di catena per il vincolo di spaziatura
        chain.addSegment(model, brain, isHead, isTail, universe);

        return model;
    }
}