package uni.gaben.iscat.universe.entity.brain.abilities;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.*;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.target.Target;
import uni.gaben.iscat.universe.entity.brain.abilities.shoot.RandomizedShootAbility;
import uni.gaben.iscat.universe.entity.brain.abilities.shoot.ShootAbility;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.shooters.Pattern;
import uni.gaben.iscat.universe.entity.shooters.SummonPattern;

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

    public abstract boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt);
    public abstract void onActivate(Brain<?> brain, UniverseModel world);
    /** @return true if still running, false when finished */
    public abstract boolean update(Brain<?> brain, UniverseModel world, double dt);

    public static Ability createAbility(EntityRecord.AbilityRecord ac, EntityModel entity) {
        if (ac.type() == null) return null;
        Target target = Target.ofPlayer();
        return switch (ac.type()) {
            case SHOOT -> {
                Pattern shooter = Pattern.createPattern(ac.pattern());
                yield new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                        ProjectileType.valueOf(ac.bulletType()), shooter,
                        target, ac.aimAtTarget(), ac.nerfPrediction());
            }
            case RANDOMIZED_SHOOT -> {
                List<Pattern> patterns = new ArrayList<>();
                for (EntityRecord.PatternRecord pc : ac.patterns()) patterns.add(Pattern.createPattern(pc));
                yield RandomizedShootAbility.targetingPlayer(ac.combatRange(), ac.cooldownSec(),
                        ProjectileType.valueOf(ac.bulletType()), ac.aimAtTarget(),
                        ac.nerfPrediction(), patterns.toArray(new Pattern[0]));
            }
            case HEAL -> new HealAbility(ac.cooldownSec(), ac.combatRange(), ac.healAmount());
            case SUMMON -> new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                    ProjectileType.valueOf(ac.bulletType()), new SummonPattern(ac.summonCount(), ac.summonEntityKey(), ac.summonRadiusPx(), ac.attackStateIndex()),
                    target, ac.aimAtTarget(), ac.nerfPrediction());
            case MELEE -> new MeleeAbility<>(ac.type().jsonKey, entity, ac.cooldownSec(), ac.meleeDamage(), EntityFilters.IS_PLAYER);
            case KAMIKAZE -> new KamikazeAbility(entity, ac.meleeDamage(), EntityFilters.IS_PLAYER);
            case DASH -> new DodgeDashAbility(entity, ac.dashCooldownMS()/1000, ac.dashDurationMS()/1000, ac.dashPrediction(), ac.dashAvoidRange(), ac.dashImpulse(),
                    Target.neighboursCached(entity, ac.dashAvoidRange(), body -> body instanceof ProjectileModel pm && pm.getType() == ProjectileType.PLAYER_BULLET));
            case PLUNGE -> new PlungeAbility(
                    entity,
                    ac.plungeCooldownMS() / 1000,
                    ac.dashDurationMS() / 1000,
                    ac.dashPrediction(),
                    ac.dashImpulse(),
                    Target.ofPlayer()
            );
        };
    }
}