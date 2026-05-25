package uni.gaben.iscat.iscat_game.universe.enemies.iscat_master;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.WanderBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.ShooterBehaviour;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.CheckLineOfSight;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.SeekLineOfSightBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.DodgeProjectileBehavior;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseSpawnable;
import uni.gaben.iscat.iscat_game.universe.attacks.BurstArcAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.RadialNovaAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.SingleBurstAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.SummonAttack;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterSettings.ISCATMASTER;

public class IscatMasterController extends AiBehaviours<IscatMasterModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour<IscatMasterModel> shooterBehaviur;
    private SeekLineOfSightBehavior seekLineOfSight;

    public IscatMasterController(IscatMasterModel iscat) {
        super(iscat);

        // Evita assembramenti (parallelo, sempre attivo)
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), ISCATMASTER.force * 0.8));

        // Wander
        this.addBehavior(new WanderBehavior(
                ISCATMASTER.force,
                ISCATMASTER.rotationSpeed
        ));

        // Chase
        this.addBehavior(new ChaseBehavior(
                ISCATMASTER.force,
                ISCATMASTER.maxVelocity,
                ISCATMASTER.detectionRange,
                50.0,
                ISCATMASTER.rotationSpeed
        ));

        // Attacchi
        shooterBehaviur = new ShooterBehaviour<IscatMasterModel>(
                80.0,
                ISCATMASTER.combatRange,
                ISCATMASTER.preferredRange,
                ISCATMASTER.force,
                ISCATMASTER.rotationSpeed,
                ISCATMASTER.fireCooldownS,
                true,
                ProjectileType.ENEMY_BULLET,
                new SingleBurstAttack<>(5, 0.05),
                new RadialNovaAttack<>(20),
                new BurstArcAttack<>(10, 0.05, 15),
                new SummonAttack<>(5, UniverseSpawnable.EATER, 80.0)
        );
        this.addBehavior(shooterBehaviur);

        // Dodge
        this.addBehavior(new DodgeProjectileBehavior(ISCATMASTER.force * 1.2, 1.5));
    }

    /**
     * Blocca l'AI finché l'animazione di entrata non è terminata.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity.getAnimationState() == IscatMasterModel.AnimationState.DEATH) return;
        super.aiUpdate(universeModel, dt);
        
        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            seekLineOfSight = new SeekLineOfSightBehavior(ISCATMASTER.force, ISCATMASTER.maxVelocity);
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