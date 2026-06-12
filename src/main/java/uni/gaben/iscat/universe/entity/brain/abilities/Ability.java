package uni.gaben.iscat.universe.entity.brain.abilities;

import uni.gaben.iscat.universe.entity.Data.BrainData;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.*;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.abilities.shoot.RandomizedShootAbility;
import uni.gaben.iscat.universe.entity.brain.abilities.shoot.ShootAbility;

import uni.gaben.iscat.universe.entity.projectiles.shooters.PatternShooter;
import uni.gaben.iscat.universe.entity.projectiles.shooters.SummonPatternShooter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Ability {
    protected final String name;
    protected final AbilityCategory category;
    protected final Set<AbilityCategory> blockedCategories;

    protected Ability(String name, AbilityCategory category, Set<AbilityCategory> blockedCategories) {
        this.name = name;
        this.category = category;
        this.blockedCategories = blockedCategories;
    }

    public AbilityCategory getCategory() { return category; }
    public Set<AbilityCategory> getBlockedCategories() { return blockedCategories; }

    public abstract boolean canActivate(GameEntity self, UniverseModel world, double dt);
    public abstract void onActivate(Brain<?> brain, UniverseModel world);
    /** @return true if still running, false when finished */
    public abstract boolean update(Brain<?> brain, UniverseModel world, double dt);

    public static Ability createAbility(BrainData.AbilityRecord ac, GameEntity entity) {
        Target target = Target.ofPlayer();
        switch (ac.type()) {
            case "shoot":
                PatternShooter shooter = PatternShooter.createPatternShooter(ac.pattern());
                return new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                        ac.bulletType().toLowerCase(), shooter,
                        target, ac.aimAtTarget(), ac.nerfPrediction());
            case "randomizedShoot":
                List<PatternShooter> patterns = new ArrayList<>();
                for (BrainData.PatternRecord pc : ac.patterns()) patterns.add(PatternShooter.createPatternShooter(pc));
                return RandomizedShootAbility.targetingPlayer(ac.combatRange(), ac.cooldownSec(),
                        ac.bulletType().toLowerCase(), ac.aimAtTarget(),
                        ac.nerfPrediction(), patterns.toArray(new PatternShooter[0]));
            case "heal":
                return new HealAbility(ac.cooldownSec(), ac.combatRange(), ac.healAmount());
            case "summon":
                // Use SummonPatternShooter inside a ShootAbility? Or dedicated ability?
                // For now, wrap in a ShootAbility with SummonPatternShooter.
                PatternShooter summonShooter = new SummonPatternShooter(ac.summonCount(), ac.summonEntityKey(), ac.summonRadiusPx(), ac.attackStateIndex());
                return new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                        ac.bulletType().toLowerCase(), summonShooter,
                        target, ac.aimAtTarget(), ac.nerfPrediction());
            case "spawnAndJoint":
                return new SpawnAndJointAbility(ac.summonEntityKey(), ac.summonRadiusPx());
            case "breakup":
                return new BreakupAbility(
                        ac.summonEntityKey(),
                        ac.summonCount(),
                        ac.summonRadiusPx()
                );
            case "gravityAura":
                return new GravityAuraAbility(ac.summonRadiusPx(), ac.combatRange());
            case "absorb":
                return new AbsorbAbility();
            case "healOnCollision":
                return new HealOnCollisionAbility(ac.healAmount());
            case "melee":
                return new MeleeAbility<>(ac.type(), entity, ac.combatRange(), ac.meleeDamage(), EntityFilters.IS_PLAYER);
            default:
                throw new IllegalArgumentException("Unknown ability type: " + ac.type());
        }
    }
}
