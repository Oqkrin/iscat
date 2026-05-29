package uni.gaben.iscat.universe.brain.actions;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.*;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Set;

public class ShootAction extends Action {
    private final Cooldown cooldown;
    private final double combatRange;
    private final AttackPattern pattern;
    private final ProjectileType bulletType;

    public ShootAction(double combatRange, double cooldownSec,
                       ProjectileType bulletType, AttackPattern pattern) {
        super("shoot", ActionCategory.ATTACK, Set.of());
        this.combatRange = combatRange;
        this.cooldown = new Cooldown(cooldownSec);
        this.pattern = pattern;
        this.bulletType = bulletType;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        cooldown.update(dt);
        if (cooldown.isCoolingDown()) return false;
        PlayerModel player = world.getPlayer();
        if (player == null) return false;
        double dist = self.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());
        return dist <= combatRange;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        PlayerModel player = world.getPlayer();
        double angle = brain.angleToTarget(player.getTransform().getTranslation());
        pattern.execute(brain.getShooter(), new Projectile(bulletType), angle, null);
        cooldown.start();
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false; // instant
    }
}