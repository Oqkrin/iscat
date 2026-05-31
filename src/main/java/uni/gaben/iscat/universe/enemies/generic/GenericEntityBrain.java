package uni.gaben.iscat.universe.enemies.generic;

import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.brain.goals.RotationGoal;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.modifiers.flocking.AlignmentModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.CohesionModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.SeparationModifier;

import java.util.stream.Collectors;

/**
 * Controller logico unificato per l'Intelligenza Artificiale delle entità generiche.
 * Sostituisce i vecchi controller rigidi cablati (hardcoded) guidando le routine decisionali
 * della CPU e i comportamenti cinematici interamente sulla base del parametro dinamico
 * {@link GenericEntitySettings#behaviorType} estratto dal database SQLite.
 * Modella gli obiettivi di movimento, orientamento rotazionale e applica modificatori di forza correttivi.
 */
public class GenericEntityBrain extends Brain<GenericEntityModel> {

    /**
     * Costruisce e cabla l'albero decisionale (Brain Wiring) per l'entità specificata.
     * Configura i vettori di spinta primari e, a seconda del profilo comportamentale estratto,
     * inietta i relativi sotto-obiettivi o algoritmi di navigazione collettiva (Flocking).
     *
     * @param entity Il modello fisico e strutturale dell'entità da pilotare.
     */
    public GenericEntityBrain(GenericEntityModel entity) {
        super(entity,
                MovementGoal.idle(),
                entity.getSettings().force,
                entity.getSettings().maxVelocity,
                entity.getSettings().rotationSpeed);

        GenericEntitySettings s = entity.getSettings();

        // Configurazione procedurale della logica IA basata sul profilo del database
        switch (s.behaviorType) {

            case WANDER_SHOOT -> {
                // Comportamento: Pattugliamento/Mantenimento della distanza e fuoco a distanza
                setMovementGoal(MovementGoal.wanderAroundTarget(
                        s.force,
                        s.combatRange,
                        s.detectionRange));

                setRotationGoal(RotationGoal.target(Target.ofPlayer()));

                /*
                 * Algoritmo di Flocking (Sotto-sistema dello Sciame):
                 * Estrae e isola in tempo reale le entità sorelle appartenenti alla stessa categoria (stessa EntityKey)
                 * presenti nel raggio d'azione visivo per applicare le tre forze di Reynolds.
                 */
                var flock = Target.ofEntities(world ->
                        world.getEntities().stream()
                                .filter(e -> e instanceof GenericEntityModel && e != entity)
                                .map(e -> (GenericEntityModel) e)
                                .filter(e -> e.getSettings().entityKey.equals(s.entityKey))
                                .collect(Collectors.toList()));

                // Coesione: Spinge i membri a raggrupparsi verso il baricentro dello sciame
                addModifier(new CohesionModifier(flock,   s.detectionRange,       1.3));

                // Allineamento: Sincronizza i vettori di velocità e direzione dei membri vicini
                addModifier(new AlignmentModifier(flock,  s.detectionRange,       1.0));

                // Separazione: Forza una repulsione locale per evitare la sovrapposizione fisica dei corpi
                addModifier(new SeparationModifier(flock, s.detectionRange / 2,  1.5));
            }

            case RAM -> {
                // Comportamento: Carica frontale e impatto kamikaze diretto sul bersaglio (Player)
                setMovementGoal(MovementGoal.wanderAroundTarget(
                        s.force,
                        0.0, // Cerca l'impatto a distanza zero
                        s.detectionRange));

                setRotationGoal(RotationGoal.target(Target.ofPlayer()));

                /*
                 * Flocking Eterogeneo Minore:
                 * Per i nemici da carica ravvicinata, la separazione viene valutata globalmente su QUALSIASI
                 * entità generica circostante per distribuire i flussi di assalto ed evitare imbottigliamenti.
                 */
                var genericFlock = Target.ofEntities(world ->
                        world.getEntities().stream()
                                .filter(e -> e instanceof GenericEntityModel && e != entity)
                                .map(e -> (GenericEntityModel) e)
                                .collect(Collectors.toList()));

                addModifier(new SeparationModifier(genericFlock, s.detectionRange / 3, 2.0));
            }

            case IDLE -> {
                // Comportamento: Stato di quiete vegetativa. Il MovementGoal.idle() è già applicato dal supercostruttore.
            }
        }
    }
}