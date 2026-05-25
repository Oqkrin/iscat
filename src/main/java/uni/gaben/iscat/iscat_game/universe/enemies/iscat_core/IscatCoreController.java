package uni.gaben.iscat.iscat_game.universe.enemies.iscat_core;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.*;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.attacks.MultiDirectionAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.ParallelLineAttack;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
/**
 * Controller di Intelligenza Artificiale per l'IscatCore (Boss Quadrato).
 * Gestisce il movimento e l'attacco rotatorio tramite il sistema a comportamenti compositi (AiBehavior).
 */
import uni.gaben.iscat.iscat_game.universe.UniverseModel;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_core.IscatCoreSettings.ISCATCORE;

public class IscatCoreController extends AiBehaviours<IscatCoreModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour<IscatCoreModel> shooterBehaviur;
    private SeekLineOfSightBehavior seekLineOfSight;

    public IscatCoreController(IscatCoreModel iscat) {
        super(iscat);

        // Evita assembramenti
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), ISCATCORE.force * 0.8));

        // Rotazione a 45°
        this.addBehavior(new RotationBehavior(
                IscatCoreSettings.ROTATION_INTERVAL,
                45.0,
                ISCATCORE.rotationSpeed,
                10
        ));

        // Wander
        this.addBehavior(new WanderBehavior(
                ISCATCORE.force,
                ISCATCORE.rotationSpeed
        ));

        // Chase
        this.addBehavior(new ChaseBehavior(
                ISCATCORE.force,
                ISCATCORE.maxVelocity,
                ISCATCORE.detectionRange,
                50.0,
                ISCATCORE.rotationSpeed
        ));

        // Attacco
        shooterBehaviur = new ShooterBehaviour<IscatCoreModel>(
                80.0,
                ISCATCORE.combatRange,
                ISCATCORE.preferredRange,
                ISCATCORE.force,
                ISCATCORE.rotationSpeed,
                () -> {
                    if (iscat.shouldRemove() || iscat.getLife() <= 0) return Double.MAX_VALUE;
                    double healthPercent = iscat.getLife() / iscat.getMaxLife();
                    return ISCATCORE.fireCooldownS * Math.max(0.1, healthPercent);
                },
                false,
                ProjectileType.ENEMY_BULLET,
                new MultiDirectionAttack<>(4, 0.0,
                        new ParallelLineAttack<>(3, IscatCoreSettings.BULLET_SPACING_M)
                )
        );
        this.addBehavior(shooterBehaviur);

        // Dodge e Slam
        this.addBehavior(new DodgeProjectileBehavior(ISCATCORE.force * 1.5, 2.0));
        this.addBehavior(new DirectionalSlamBehavior(5.0, ISCATCORE.force * 4.0, 3.0, 1.2));
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (!aiEntity.isAlive()) return;
        super.aiUpdate(universeModel, dt);
        
        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            seekLineOfSight = new SeekLineOfSightBehavior(ISCATCORE.force, ISCATCORE.maxVelocity);
            addBehavior(checkLineOfSight);
        } else {
            if (checkLineOfSight.hasLineOfSightWithTarget()) {
                addBehavior(shooterBehaviur);
                removeBehavior(seekLineOfSight);
            } else {
                removeBehavior(shooterBehaviur);
                addBehavior(seekLineOfSight);
            }
        }
    }
}