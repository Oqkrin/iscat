package uni.gaben.iscat.game.components.entities.npcs;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.components.entities.player.projectile.ProjectileModel;
import uni.gaben.iscat.game.utils.interfaces.*;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.ThemeManager;

import java.util.Objects;

/**
 * CLASSE MONOLITICA: Contiene Nemico, Proiettile e Renderer.
 */
public class IscatCore extends NpcModel implements AI, HasRenderer, Spawnable, Collidable {

    // --- SETTINGS INTERNI ---
    private static final int HP_INIZIALI = 150;
    private static final double DIM_SPRITE = 64;
    private static final int NUMERO_FRAMES = 1;
    private static final double SCALE = 2.0;
    private static final double DRAW_SIZE = DIM_SPRITE * SCALE;
    private static final double VELOCITA_MOVIMENTO = 10;

    private static final double RAGGIO_COLLISIONE = (DIM_SPRITE / 2.0) * 0.9;
    private static final double DISTANZA_IDEALE = 250.0;
    private static final int COOLDOWN_SPARO_TICKS = 30;
    private static final double DIM_PROIETTILE = 16;
    private static final double VEL_PROIETTILE = 20.0;

    private static final Image SPRITE_SHEET = new Image(Objects.requireNonNull(
            IscatCore.class.getResourceAsStream("/uni/gaben/iscat/sprites/enemies/iscat_core.png")));

    private final Cooldown cooldownFuoco = new Cooldown();

    public IscatCore(double startX, double startY) {
        super(startX, startY);

        this.Spritesize = DIM_SPRITE * SCALE;
        this.hp = HP_INIZIALI;
        this.maxHp = HP_INIZIALI;
        this.spriteSize = DIM_SPRITE * SCALE;

        this.cooldownFuoco.start(COOLDOWN_SPARO_TICKS);
    }

    @Override
    public void updateAI(GameModel world, double dt) {
        cooldownFuoco.update(dt);

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
            shoot(world, myPos);

            // Calcolo hp dinamico
            double healthPercent = (double) this.hp / this.maxHp;
            int dynamicCooldown = (int) ((COOLDOWN_SPARO_TICKS) * healthPercent);
            cooldownFuoco.start(dynamicCooldown);
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

    private void shoot(GameModel world, Vec2 myPos) {
        double[] directions = {0, 90, 180, 270};
        double offsetSpacing = 24; // Distanza tra i proiettili della terzina

        for (double angleDeg : directions) {
            double rad = Math.toRadians(angleDeg);
            Vec2 dir = new Vec2(Math.cos(rad), Math.sin(rad));
            Vec2 velocity = new Vec2(dir.x * VEL_PROIETTILE, dir.y * VEL_PROIETTILE);

            // Vettore perpendicolare CORRETTO: se dir è (x,y), perp è (-y, x)
            Vec2 perp = new Vec2(-dir.y, dir.x);

            for (int i = -1; i <= 1; i++) {
                Vec2 bulletPos = new Vec2(
                        myPos.x + (perp.x * i * offsetSpacing),
                        myPos.y + (perp.y * i * offsetSpacing)
                );
                world.addEntity(new IscatCoreProjectile(bulletPos, velocity));
            }
        }

        IscatAudioManager.getInstance().playSFX("shoot");
        cooldownFuoco.start(COOLDOWN_SPARO_TICKS);
    }

    @Override
    public Drawable<IscatCore> getRenderer() {
        return (gc, iscatCore) -> {
            int frame = (int) ((System.nanoTime() / 1_000_000_000.0) / 0.4) % NUMERO_FRAMES;
            int sourceX = frame * (int) DIM_SPRITE;

            WritableImage frameImg = new WritableImage(
                    SPRITE_SHEET.getPixelReader(), sourceX, 0, (int) DIM_SPRITE, (int) DIM_SPRITE);

            Color tint = ThemeManager.getInstance().globalTintProperty().get();
            Image drawn = SpriteUtils.tinted(frameImg, tint);

            gc.save();
            gc.translate(
                    iscatCore.getX() + DRAW_SIZE / 2,
                    iscatCore.getY() + DRAW_SIZE / 2
            );

            gc.drawImage(
                    drawn,
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

    // =========================================================================
    // PROIETTILE
    // =========================================================================

    public static class IscatCoreProjectile extends ProjectileModel implements HasRenderer {

        public IscatCoreProjectile(Vec2 pos, Vec2 vel) {
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