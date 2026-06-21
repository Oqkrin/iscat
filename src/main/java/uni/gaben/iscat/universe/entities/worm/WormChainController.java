package uni.gaben.iscat.universe.entities.worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityBrain;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.brain.IEntityController;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller logico e cinematico per entità composte a catena (Worm Chain Controller).
 * <p>
 * Gestisce il movimento solidale e la fisica ripartita di un verme segmentato multi-entità.
 * Implementa un vincolo di spaziatura costante per simulare il comportamento di una corda (Rope Physics Spacing)
 * e governa la logica di scissione dinamica (Chain Break): se un segmento centrale o la testa muore,
 * la catena si frammenta promuovendo automaticamente i segmenti interni rimasti in nuove teste autonome o vermi indipendenti.
 * </p>
 */
public class WormChainController implements IEntityController {

    /**
     * Contenitore dati per il tracciamento dei singoli nodi strutturali del verme.
     */
    public static class WormSegmentData {
        public EntityModel model;
        public EntityBrain brain;
        public boolean isHead;
        public boolean isTail;

        public WormSegmentData(EntityModel model, EntityBrain brain, boolean isHead, boolean isTail) {
            this.model = model;
            this.brain = brain;
            this.isHead = isHead;
            this.isTail = isTail;
        }
    }

    private final List<WormSegmentData> segments = new ArrayList<>();
    private final UniverseController universeController;
    private final String headKey;
    private final String bodyKey;
    private final String tailKey;

    /** Spaziatura metrica di vincolo fissa mantenuta tra due segmenti consecutivi ($1.5\text{ m}$). */
    private static final double SEGMENT_SPACING = 1.5;

    /**
     * Costruisce il gestore cinematico della catena definendo i codici identificativi (Registry Keys)
     * per discriminare i ruoli di testa, corpo e coda durante le mutazioni.
     */
    public WormChainController(UniverseController universeController, String headKey, String bodyKey, String tailKey) {
        this.universeController = universeController;
        this.headKey = headKey;
        this.bodyKey = bodyKey;
        this.tailKey = tailKey;
    }

    /**
     * Inserisce un nuovo segmento in coda alla struttura.
     * Se il segmento non è designato come testa, il suo cervello autonomo viene disattivato per
     * cedere il controllo cinetico esclusivo alle routine del ciclo della catena.
     */
    public void addSegment(EntityModel model, EntityBrain brain, boolean isHead, boolean isTail) {
        if (!isHead && brain != null) {
            brain.setEnabled(false);
        }
        segments.add(new WormSegmentData(model, brain, isHead, isTail));
    }

    /**
     * @return L'istanza di {@link EntityBrain} dell'ultimo segmento registrato nella catena.
     */
    public EntityBrain getLatestBrain() {
        if (segments.isEmpty()) return null;
        return segments.getLast().brain;
    }

    /**
     * Esegue l'aggiornamento logico e cinematico della catena ad ogni tick dell'universo.
     * <p>
     * Nella prima fase scansiona la catena per intercettare la distruzione di nodi (flag {@code shouldRemove}).
     * Nella seconda fase applica l'algoritmo di accoppiamento a corda: calcola la distanza dal segmento precedente
     * e riallinea forzatamente la posizione lungo il vettore di direzione proiettato, ereditandone la velocità lineare.
     * </p>
     */
    @Override
    public void update(UniverseModel universe, double dt) {
        if (segments.isEmpty()) return;

        // Fase 1: Rilevamento lineare delle interruzioni strutturali
        int deadIndex = -1;
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).model.shouldRemove()) {
                deadIndex = i;
                break;
            }
        }

        if (deadIndex != -1) {
            handleChainBreak(universe, deadIndex);
        }

        if (segments.isEmpty()) return;

        // Fase 2: Risolutore di vincolo geometrico per il corpo e la coda (Rope Constraint Solver)
        for (int i = 1; i < segments.size(); i++) {
            WormSegmentData current = segments.get(i);
            EntityModel leader = segments.get(i - 1).model;

            Vector2 currentPos = current.model.getTransform().getTranslation();
            Vector2 leaderPos = leader.getTransform().getTranslation();

            Vector2 toLeader = leaderPos.copy().subtract(currentPos);
            double distance = toLeader.getMagnitude();

            if (distance > 0.001) {
                Vector2 direction = toLeader.getNormalized();
                // Determina la posizione desiderata arretrandola della costante di spaziatura dal leader
                Vector2 desiredPos = leaderPos.copy().subtract(direction.multiply(SEGMENT_SPACING));

                current.model.getTransform().setTranslation(desiredPos.x, desiredPos.y);
                current.model.getLinearVelocity().set(leader.getLinearVelocity());
            }
        }
    }

    /**
     * Gestisce la scissione della catena in base all'indice del nodo rimosso.
     * <ul>
     * <li><b>Decapitazione (deadIndex = 0):</b> Rimuove la vecchia testa e promuove il segmento successivo.
     * Se la nuova cima è una coda, viene isolata attivandone il comportamento nativo.</li>
     * <li><b>Sezione Centrale (deadIndex > 0):</b> Tronca il verme in due parti; i segmenti superstiti posteriori
     * vengono isolati per tentare di generare un nuovo organismo indipendente (Mitosis Spawn).</li>
     * </ul>
     */
    private void handleChainBreak(UniverseModel universe, int deadIndex) {
        WormSegmentData deadSegment = segments.get(deadIndex);

        // CASO 1: Morte del nodo di testa
        if (deadIndex == 0 && deadSegment.isHead) {
            segments.removeFirst();

            if (!segments.isEmpty()) {
                WormSegmentData newHead = segments.getFirst();
                String newHeadKey = newHead.model.getEntityRecord().entityKey();

                if (newHeadKey.equals(bodyKey)) {
                    promoteToHead(universe, newHead);
                } else if (newHeadKey.equals(tailKey)) {
                    activateTailBrain(newHead);
                    segments.clear(); // Scioglie definitivamente il controller della catena rimasta
                }
            }
            return;
        }

        // CASO 2: Taglio a metà della catena
        if (deadIndex > 0 && deadIndex < segments.size() - 1) {
            List<WormSegmentData> backPart = new ArrayList<>(segments.subList(deadIndex, segments.size()));

            backPart.removeFirst(); // Rimuove l'elemento effettivamente distrutto
            segments.subList(deadIndex, segments.size()).clear(); // Isola la porzione frontale accorciandola

            if (!backPart.isEmpty()) {
                handleBackPart(universe, backPart);
            }
        }
    }

    /**
     * Sostituisce l'entità Corpo specificata con una nuova istanza di tipo Testa.
     * Conserva i vettori fisici di posizione, rotazione e velocità lineare per non interrompere
     * il flusso cinematico, agganciando poi un nuovo {@link EntityBrain} impostato sull'inseguimento del giocatore.
     */
    private void promoteToHead(UniverseModel universe, WormSegmentData segment) {
        if (!segment.model.getEntityRecord().entityKey().equals(bodyKey)) {
            return;
        }

        // Snapshot dei parametri cinetici del frame corrente
        Vector2 currentPos = segment.model.getTransform().getTranslation().copy();
        double currentRotation = segment.model.getTransform().getRotationAngle();
        Vector2 currentVelocity = segment.model.getLinearVelocity().copy();

        segment.model.completeKill();
        universe.removeEntity(segment.model);

        EntityRecord headRecord = EntityFactory.getCache().get(headKey.toLowerCase().trim());
        if (headRecord != null) {
            EntityModel newHeadModel = new EntityModel(currentPos.x, currentPos.y, headRecord);
            EntityBrain newHeadBrain = EntityBrain.fromRecord(newHeadModel);

            newHeadModel.getTransform().setTranslation(currentPos.x, currentPos.y);
            newHeadModel.getTransform().setRotation(currentRotation);
            newHeadModel.getLinearVelocity().set(currentVelocity);

            // Iniezione degli obiettivi di sterzata IA per la caccia al player
            newHeadBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 2.0));

            universe.addEntity(newHeadModel);
            universeController.addEntityController(newHeadBrain);

            // Aggiornamento dei puntatori di riferimento della struttura dati
            segment.model = newHeadModel;
            segment.brain = newHeadBrain;
            segment.isHead = true;
            segment.isTail = false;
        }
    }

    /**
     * Ripristina e riattiva l'intelligenza artificiale incorporata nel segmento di coda,
     * consentendogli di muoversi in autonomia nel mondo a seguito del distacco dal verme principale.
     */
    private void activateTailBrain(WormSegmentData tailSegment) {
        EntityBrain newBrain = EntityBrain.fromRecord(tailSegment.model);
        tailSegment.brain.setSteeringGoal(newBrain.getSteeringGoal());
        tailSegment.brain.setRotationGoal(newBrain.getRotationGoal());
        tailSegment.brain.setEnabled(true);
        tailSegment.isHead = false;
        tailSegment.isTail = true;
    }

    /**
     * Analizza la sezione posteriore rimossa dal verme a seguito di un taglio.
     * Se il primo nodo orfano è un corpo, innesca la generazione di un nuovo verme,
     * se invece è una coda isolata, attiva la sua IA indipendente e distrugge gli eventuali nodi residui orfani.
     */
    private void handleBackPart(UniverseModel universe, List<WormSegmentData> backPart) {
        WormSegmentData firstSegment = backPart.getFirst();
        String firstKey = firstSegment.model.getEntityRecord().entityKey();

        if (firstKey.equals(bodyKey)) {
            createNewWormFromSegments(universe, backPart);
        } else if (firstKey.equals(tailKey)) {
            activateTailBrain(firstSegment);

            // Pulizia di sicurezza: elimina i segmenti orfani bloccati dietro una coda autonoma
            for (int i = 1; i < backPart.size(); i++) {
                WormSegmentData segment = backPart.get(i);
                segment.model.completeKill();
                universe.removeEntity(segment.model);
            }
        }
    }

    /**
     * Alloca e registra un nuovo controllore {@link WormChainController} indipendente,
     * convertendo la lista di segmenti orfani in una nuova entità verme completa (Mitosi Computazionale).
     */
    private void createNewWormFromSegments(UniverseModel universe, List<WormSegmentData> newSegments) {
        if (newSegments.isEmpty()) return;

        WormChainController newWormChain = new WormChainController(universeController, headKey, bodyKey, tailKey);

        WormSegmentData firstSegment = newSegments.getFirst();
        String firstKey = firstSegment.model.getEntityRecord().entityKey();

        if (firstKey.equals(bodyKey)) {
            promoteToHead(universe, firstSegment);
        } else {
            return;
        }

        newWormChain.addSegment(firstSegment.model, firstSegment.brain, true, false);

        // Ricatena ricorsivamente tutti i nodi successivi ereditati
        for (int i = 1; i < newSegments.size(); i++) {
            WormSegmentData segment = newSegments.get(i);
            boolean isTail = segment.model.getEntityRecord().entityKey().equals(tailKey);
            newWormChain.addSegment(segment.model, segment.brain, false, isTail);
        }

        universeController.addEntityController(newWormChain);
    }
}