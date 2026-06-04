package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;

/**
 * Controller logico unificato per l'Intelligenza Artificiale delle entità generiche.
 * Sostituisce i vecchi controller rigidi cablati (hardcoded) guidando le routine decisionali
 * della CPU e i comportamenti cinematici interamente sulla base del parametro dinamico
 * {@link GenericEntitySettings} estratto dal database SQLite.
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
        super(entity, MovementGoal.pursuit(Target.ofPlayer()),
                entity.getSettings().maxForce,
                entity.getSettings().maxVelocity,
                entity.getSettings().maxAngularVelocity,
                entity.getSettings().mass
        );

        GenericEntitySettings s = entity.getSettings();

        // Inside GenericEntityBrain constructor
// Raw target: all entities except projectiles (they are handled by ProjectileAvoidanceModifier)
// and excluding self.
        Target rawNearby = Target.neighboursCached(entity, s.detectionRange,
                b -> !(b instanceof Projectile) && b != entity);

// Flocking view: only LivingEntityModel
        Target flockTarget = rawNearby.filtered(b -> b instanceof LivingEntityModel);

// Obstacle view: non‑living (and also not self, but self already excluded in raw)
        Target obstacleTarget = rawNearby.filtered(b -> !(b instanceof LivingEntityModel));

    }
}