package uni.gaben.iscat.universe.enemies.master;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.lib.implementations.attacks.*;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.master.IscatMasterSettings.ISCATMASTER;

public class IscatMasterController extends AiBehaviours<IscatMasterModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    Random rand = new Random();

    public IscatMasterController(IscatMasterModel iscat) {
        super(iscat, ISCATMASTER.force, ISCATMASTER.maxVelocity, ISCATMASTER.rotationSpeed);

        // Evita assembramenti (Passive track)
        this.addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATMASTER.force * 0.8));

        // Wander (Movement track)
        this.addMovement(new WanderBehavior(
                ISCATMASTER.maxVelocity,
                50.0,
                ISCATMASTER.detectionRange,
                ISCATMASTER.combatRange
        ));

        // Chase (Movement track)
        addMovement(new OrbitPlayerBehavior(
                ISCATMASTER.force,
                ISCATMASTER.maxVelocity,
                (ISCATMASTER.preferredRange+ISCATMASTER.preferredRange)/2,
                false
        ));

        // (Fixed stray FigureAttack missing array wrap)
        this.shooterBehaviour = new ShooterBehaviour(
                80.0,
                ISCATMASTER.combatRange,
                ISCATMASTER.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new RepeaterAttack(3,new SummonAttack(1, UniverseSpawnable.ISCAT_DASHER,0)),
                new RepeaterAttack(3,new SummonAttack(1, UniverseSpawnable.ISCAT_HEALER,0)),
                new RepeaterAttack(3,new SummonAttack(1, UniverseSpawnable.ISCAT_CORE,0)),
                new RepeaterAttack(5, new MultiDirectionAttack(3, rand.nextInt(90),
                        new SpreadAttack(rand.nextInt((int) ISCATMASTER.combatRange)/3, rand.nextInt(180)))),
                new RepeaterAttack(3, new FigureAttack(3, FigureAttack.FigureType.STAR))
        );

        this.addAttack(shooterBehaviour);

        // Dodge (Movement track)
        this.addMovement(new DodgeProjectileBehavior(ISCATMASTER.force * 1.2, ISCATMASTER.combatRange,1.5));
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        aiEntity.shockwave().update(dt);
        if (aiEntity.getAnimationState() == IscatMasterModel.AnimationState.DEATH) return;
        super.aiUpdate(universeModel, dt);

        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            seekLineOfSight = new SeekLineOfSightBehavior(ISCATMASTER.force, ISCATMASTER.maxVelocity);
            addPassive(checkLineOfSight);
        } else {
            if (checkLineOfSight.hasLineOfSightWithTarget()) {
                addAttack(shooterBehaviour);
                removeMovement(seekLineOfSight);
            } else {
                removeAttack(shooterBehaviour);
                addMovement(seekLineOfSight);
            }
        }
    }
}