package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.RotationGoal;
import uni.gaben.iscat.universe.entity.brain.actions.shoot.ShootAction;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.SingleShotShooter;

/**
 * Controller logico unificato per l'Intelligenza Artificiale delle entità genericamente configurate.
 * Guida le routine decisionali della CPU e i comportamenti cinematici sulla base dei parametri
 * dinamici estratti dal database SQLite tramite {@link GenericEntitySettings}.
 */
public class GenericEntityBrain extends Brain<GenericEntityModel> {

    public GenericEntityBrain(GenericEntityModel entity) {
        // NOTA: Rimosso il parametro 'mass' finale per allinearsi perfettamente
        // alla firma del costruttore a 5 parametri della classe base Brain.
        super(entity,
                SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity),
                entity.getSettings().maxForce,
                entity.getSettings().maxVelocity,
                entity.getSettings().maxAngularVelocity,
                entity.getSettings().mass
        );

        setRotationGoal(RotationGoal.target(Target.ofPlayer()));

        GenericEntitySettings settings = entity.getSettings();

        addAction(new ShootAction(
                settings.detectionRange,
                settings.actionCooldownMS /1000,
                ProjectileType.ENEMY_BULLET,
                new SingleShotShooter(),
                Target.ofPlayer(),
                false//,
                //Math.PI/4,
                //entity
                ));
    }
}