package uni.gaben.iscat.game.components.entities.npcs;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.components.entities.npcs.NpcModel;
import uni.gaben.iscat.game.components.entities.player.projectile.ProjectileModel;
import uni.gaben.iscat.game.utils.interfaces.*;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Objects;

/**
 * CLASSE MONOLITICA: Contiene Nemico, Proiettile e Renderer.
 */
public class FakeIscat extends NpcModel implements AI, HasRenderer, Spawnable, Collidable {

    // --- SETTINGS INTERNI ---
    private static final int HP_INIZIALI = 50;
    private static final double DIM_SPRITE = 32.0;
    private static final int NUMERO_FRAMES = 19;
    private static final double SCALE = 2.0;
    private static final double DRAW_SIZE = DIM_SPRITE * SCALE;
    private static final double VELOCITA_MOVIMENTO = 50;

    private static final double RAGGIO_COLLISIONE = (DIM_SPRITE / 2.0) * 0.9;
    private static final double DISTANZA_IDEALE = 250.0;
    private static final int COOLDOWN_SPARO_TICKS = 60;
    private static final double DIM_PROIETTILE = 32.0;
    private static final double VEL_PROIETTILE = 10.0;

    private static final Image SPRITE_SHEET = new Image(Objects.requireNonNull(
            FakeIscat.class.getResourceAsStream("/uni/gaben/iscat/sprites/fake_iscat.png")));

    private final Cooldown cooldownFuoco = new Cooldown();

    public FakeIscat(double startX, double startY) {
        super(startX, startY);

        this.hp = HP_INIZIALI;
        this.maxHp = HP_INIZIALI;
        this.spriteSize = DIM_SPRITE * SCALE;

        this.cooldownFuoco.set(COOLDOWN_SPARO_TICKS);
    }

    @Override
    public void updateAI(GameModel world, double dt) {
        cooldownFuoco.tick();

        if (isDead()) return;

        Vec2 playerPos = world.getPlayer().getColliderCenter();
        Vec2 myPos = this.getColliderCenter();

        double dist = Math.hypot(
                playerPos.x - myPos.x,
                playerPos.y - myPos.y
        );

        // Movimento
        maintainDistance(playerPos, myPos, dist);

        // Sparo
        if (dist >= 90 && dist <= 420 && cooldownFuoco.isReady()) {
            shoot(world, playerPos, myPos);
        }
    }

    private void maintainDistance(Vec2 playerPos, Vec2 myPos, double dist) {
        double dx = playerPos.x - myPos.x;
        double dy = playerPos.y - myPos.y;

        double angle = Math.atan2(dy, dx);

        if (dist < DISTANZA_IDEALE - 20) {
            this.applyForce(new Vec2(
                    -Math.cos(angle) * VELOCITA_MOVIMENTO,
                    -Math.sin(angle) * VELOCITA_MOVIMENTO
            ));
        }
        else if (dist > DISTANZA_IDEALE + 30) {
            this.applyForce(new Vec2(
                    Math.cos(angle) * VELOCITA_MOVIMENTO,
                    Math.sin(angle) * VELOCITA_MOVIMENTO
            ));
        }

        this.setDirectionAngle(Math.toDegrees(angle));
    }

    private void shoot(GameModel world, Vec2 playerPos, Vec2 myPos) {
        double rad = Math.toRadians(this.getDirectionAngle());

        Vec2 velocity = new Vec2(
                Math.cos(rad) * VEL_PROIETTILE,
                Math.sin(rad) * VEL_PROIETTILE
        );

        world.addEntity(new FakeIscatProjectile(myPos, velocity));

        IscatAudioManager.getInstance().playSFX("shoot");

        cooldownFuoco.set(COOLDOWN_SPARO_TICKS);
    }

    @Override
    public Drawable<FakeIscat> getRenderer() {
        return (gc, fakeIscat) -> {

            int frame = (int) ((System.nanoTime() / 1_000_000_000.0) / 0.4) % NUMERO_FRAMES;
            int sourceX = frame * (int) DIM_SPRITE;

            gc.save();

            gc.translate(
                    fakeIscat.getX() + DRAW_SIZE / 2,
                    fakeIscat.getY() + DRAW_SIZE / 2
            );

            // a FakeIscat non serve la rotazione:
            //gc.rotate(fakeIscat.getDirectionAngle());

            gc.drawImage(
                    SPRITE_SHEET,
                    sourceX,
                    0,
                    (int) DIM_SPRITE,
                    (int) DIM_SPRITE,
                    -DRAW_SIZE / 2,
                    -DRAW_SIZE / 2,
                    DRAW_SIZE,
                    DRAW_SIZE
            );

            gc.restore();
        };
    }

    @Override
    public double getCollisionRadius() {
        return RAGGIO_COLLISIONE * SCALE;
    }

    @Override
    public int getCollisionLayer() {
        return LAYER_ENEMY;
    }

    @Override
    public int getCollisionMask() {
        return LAYER_PLAYER | LAYER_PROJECTILE | LAYER_ENEMY;
    }

    @Override
    public void resetAI() {}

    // =========================================================================
    // PROIETTILE
    // =========================================================================

    public static class FakeIscatProjectile extends ProjectileModel implements HasRenderer {

        public FakeIscatProjectile(Vec2 pos, Vec2 vel) {
            super(pos, vel);
            this.mass = 1.0;
        }

        @Override
        public double getCollisionRadius() {
            return DIM_PROIETTILE / 2.0;
        }

        @Override
        public int getCollisionMask() {
            return LAYER_PLAYER;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Drawable<ProjectileModel> getRenderer() {

            return (gc, p) -> {

                double r = DIM_PROIETTILE / 2.0;

                gc.save();

                gc.setFill(Color.ORANGERED);

                gc.fillOval(
                        p.getX() - r,
                        p.getY() - r,
                        r * 2,
                        r * 2
                );

                gc.setGlobalAlpha(0.3);

                gc.setFill(Color.RED);

                gc.fillOval(
                        p.getX() - r * 1.5,
                        p.getY() - r * 1.5,
                        r * 3,
                        r * 3
                );

                gc.restore();
            };
        }
    }
}