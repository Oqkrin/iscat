package uni.gaben.iscat.universe.entity.enemies.generic;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.modifiers.BoundaryAvoidanceModifier;
import uni.gaben.iscat.universe.entity.brain.modifiers.ObstaclesAvoidanceModifier;
import uni.gaben.iscat.universe.entity.brain.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.entity.brain.modifiers.flocking.AlignmentModifier;
import uni.gaben.iscat.universe.entity.brain.modifiers.flocking.CohesionModifier;
import uni.gaben.iscat.universe.entity.brain.modifiers.flocking.SeparationModifier;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;

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
        super(entity, MovementGoal.idle(),
                entity.getSettings().maxForce,
                entity.getSettings().maxVelocity,
                entity.getSettings().maxAngularVelocity);

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

        // Flocking modifiers with balanced weights (sum ≈ 0.5, leaving room for other behaviors)
        addModifier(new CohesionModifier(flockTarget, 1));      // Gentle pull toward group center
        addModifier(new AlignmentModifier(flockTarget, 1));     // Match group velocity
        addModifier(new SeparationModifier(flockTarget, s.detectionRange/2, 1.8));  // Higher priority: avoid crowding
        
        // Environmental modifiers with higher priorities (sum ≈ 0.5)
        addModifier(new ObstaclesAvoidanceModifier(obstacleTarget));  // Uses maxForce directly
        addModifier(new ProjectileAvoidanceModifier(s.detectionRange, s.maxForce));  // High priority: dodge bullets
        addModifier(new BoundaryAvoidanceModifier());  // Uses maxForce directly
    }
}