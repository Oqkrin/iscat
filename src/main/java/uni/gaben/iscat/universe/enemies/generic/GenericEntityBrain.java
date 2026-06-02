package uni.gaben.iscat.universe.enemies.generic;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.collision.CollisionItem;
import org.dyn4j.collision.Fixture;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.DetectResult;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.brain.goals.RotationGoal;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.modifiers.BoundaryAvoidanceModifier;
import uni.gaben.iscat.universe.brain.modifiers.ObstaclesAvoidanceModifier;
import uni.gaben.iscat.universe.brain.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.AlignmentModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.CohesionModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.SeparationModifier;
import uni.gaben.iscat.universe.brain.actions.HealAction;
import uni.gaben.iscat.universe.brain.actions.shoot.RandomizedShootAction;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.attacks.RepeaterAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SummonAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.MultiDirectionAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SpreadAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.FigureAttack;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.enemies.healer.IscatHealerSettings;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;

import java.util.ArrayList;
import java.util.List;
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


        addModifier(
                new CohesionModifier(Target.neighbours(entity, s.detectionRange, new DetectFilter<>(true, true, null)),
                        1));

        addModifier(
                new AlignmentModifier(Target.neighbours(entity, s.detectionRange, new DetectFilter<>(true, true, null)),
                        1));

        addModifier(
                new SeparationModifier(Target.neighbours(entity, s.detectionRange/2, new DetectFilter<>(true, true, null)),
                        1.7));

        addModifier(
                new ProjectileAvoidanceModifier(s.detectionRange, s.force));
        addModifier(
                new BoundaryAvoidanceModifier());
        addModifier(new ObstaclesAvoidanceModifier(Target.neighbours(entity,s.detectionRange,new DetectFilter<>(true, true, null))));
    }
}