package uni.gaben.iscat.iscat_game.universe.enemies.iscat_master;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.*;
import uni.gaben.iscat.iscat_game.universe.UniverseSpawnable;
import uni.gaben.iscat.iscat_game.lib.implementations.attacks.*;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;

import java.util.Random;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterSettings.ISCATMASTER;

public class IscatMasterController extends AiBehaviours<IscatMasterModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    Random rand = new Random();

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
        addBehavior(new OrbitPlayerBehavior(
                ISCATMASTER.force,
                ISCATMASTER.maxVelocity,
                (ISCATMASTER.preferredRange+ISCATMASTER.preferredRange)/2,
                false
        ));

        this.shooterBehaviour = new ShooterBehaviour(
                80.0,
                ISCATMASTER.combatRange,
                ISCATMASTER.preferredRange,
                ISCATMASTER.force,
                ISCATMASTER.rotationSpeed,
                ISCATMASTER.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new RepeaterAttack(3,new SummonAttack(1, UniverseSpawnable.ISCAT_DASHER,0)),
                new RepeaterAttack(3,new SummonAttack(1, UniverseSpawnable.ISCAT_HEALER,0)),
                new RepeaterAttack(3,new SummonAttack(1, UniverseSpawnable.ISCAT_CORE,0)),

                new RepeaterAttack(5, new MultiDirectionAttack(3, rand.nextInt(90),
                        new SpreadAttack(rand.nextInt((int) ISCATMASTER.combatRange)/3, rand.nextInt(180))))
                );

        new RepeaterAttack(3, new FigureAttack(3, FigureAttack.FigureType.STAR));
        this.addBehavior(shooterBehaviour);

        // Dodge
        this.addBehavior(new DodgeProjectileBehavior(ISCATMASTER.force * 1.2, 1.5));
    }

    /**
     * Blocca l'AI finché l'animazione di entrata non è terminata.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        aiEntity.shockwave().update(dt);
        if (aiEntity.getAnimationState() == IscatMasterModel.AnimationState.DEATH) return;
        super.aiUpdate(universeModel, dt);

        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            seekLineOfSight = new SeekLineOfSightBehavior(ISCATMASTER.force, ISCATMASTER.maxVelocity);
            addBehavior(checkLineOfSight);
        } else {
            if (checkLineOfSight.hasLineOfSightWithTarget()) {
                addBehavior(shooterBehaviour);
                removeBehavior(seekLineOfSight);
            } else {
                removeBehavior(shooterBehaviour);
                addBehavior(seekLineOfSight);
            }
        }
    }
}