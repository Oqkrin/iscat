package uni.gaben.iscat.gamenex.universe.enemies.iscat_mother;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.gamenex.universe.UniverseSpawnable;
import uni.gaben.iscat.gamenex.universe.UniverseSpawner;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.utils.Cooldown;

public class IscatMotherModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Cooldown cooldownFuoco = new Cooldown();
    private boolean hasSpawnedMinions = false;

    private static final double BASEMAXLIFE = 300;


    public IscatMotherModel(double x, double y) {
        this(x, y, BASEMAXLIFE, BASEMAXLIFE);
    }
    public IscatMotherModel(double x, double y, double life, double maxLife) {
        super(x, y, life, maxLife);
    }

    @Override
    public void onDeath() {
        super.onDeath();
        spawnHorde();
    }

    /** Controlla se IscatMother vuole spawnare i rinforzi */
    private void checkMinionSpawn() {
        if (!hasSpawnedMinions && getLife() <= maxLife * 0.5) {
            spawnMinions();
            hasSpawnedMinions = true;
        }
    }

    /** Spawna 5 FakeIscat intorno alla madre */
    private void spawnMinions() {
        Vector2 myPos = getTransform().getTranslation();
        double radius = 80.0;   // distanza dal centro della madre

        for (int i = 0; i < 7; i++) {
            double angle = (i * 72.0); // 360° / 5 = 72°
            double rad = Math.toRadians(angle);

            double x = myPos.x + Math.cos(rad) * radius;
            double y = myPos.y + Math.sin(rad) * radius;

            if (i < 5) UniverseSpawner.getInstance().spawn(UniverseSpawnable.ISCAT_MOB.name(),  x, y);
            else UniverseSpawner.getInstance().spawn(UniverseSpawnable.EATER.name(),  x, y);
        }

        System.out.println("IscatMother ha chiamato rinforzi!");
    }

    private void maintainDistance(Vec2 playerPos, Vec2 myPos, double dist) {
        double dx = playerPos.x - myPos.x;
        double dy = playerPos.y - myPos.y;
        double angle = Math.atan2(dy, dx);

        if (dist < 100 - 20) {
            this.applyForce(new Vector2(-Math.cos(angle) * getBaseAccelerationPerTick(), -Math.sin(angle) *  getBaseAccelerationPerTick()));
        } else if (dist > 100 + 30) {
            this.applyForce(new Vector2(Math.cos(angle) * getBaseAccelerationPerTick(), Math.sin(angle) * getBaseAccelerationPerTick()));
        }
        getTransform().setRotation(angle);
    }

    private void shoot(GameModel world, Vec2 myPos) {
        double rad = getTransform().getRotationAngle();
        double spreadAngle = Math.toRadians(15.0);

        for (int i = -1; i <= 1; i++) {
            rad += (i * spreadAngle);
            Vector2 velocity = new Vector2(
                    Math.cos(rad) * getBaseAccelerationPerTick()*10,
                    Math.sin(rad) * getBaseAccelerationPerTick()*10
            );

            Projectile p = new Projectile();
            p.setLinearVelocity(velocity);
            UniverseSpawner.getInstance().spawnProjectile(p);
        }

        IscatAudioManager.getInstance().playSFX("shoot");
        cooldownFuoco.start(15);
    }


    private void spawnHorde() {
        Vector2 center = getTransform().getTranslation();
        double radius = 130.0;

        // 20 Iscat
        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * 360;
            double rad = Math.toRadians(angle);
            double dist = radius + Math.random() * 60;

            double x = center.x + Math.cos(rad) * dist;
            double y = center.y + Math.sin(rad) * dist;

            if(i %2 ==0) UniverseSpawner.getInstance().spawn(UniverseSpawnable.HEARTH.name(), x, y);
            else UniverseSpawner.getInstance().spawn(UniverseSpawnable.EATER.name(), x, y);
        }
    }

    @Override
    public Projectile getProjectile() {
        return null;
    }

    @Override
    public boolean hasAmmo() {
        return false;
    }

    @Override
    public Cooldown projectileCooldown() {
        return null;
    }

    @Override
    public int getProjectileCooldownTickCount() {
        return 0;
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {

    }

    // =========================================================================
    // CLASSE INTERNA PER IL PROIETTILE
    // =========================================================================

}