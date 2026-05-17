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
public class FallenStarGolem extends NpcModel implements AI, HasRenderer, Spawnable, Collidable {

    // --- SETTINGS INTERNI ---
    private static final int    HP_INIZIALI         = 200;
    private static final double DIM_SPRITE          = 64.0;
    private static final int NUMERO_FRAMES          = 25;
    private static final double SCALE = 2.0;
    private static final double DRAW_SIZE = DIM_SPRITE * SCALE;
    private static final double VELOCITA_MOVIMENTO = 15;

    private static final double RAGGIO_COLLISIONE   = (DIM_SPRITE / 2.0) * 0.9;
    private static final double DISTANZA_IDEALE     = 250.0;
    private static final int    COOLDOWN_SPARO_TICKS = 60;
    private static final double DIM_PROIETTILE      = 16.0;
    private static final double VEL_PROIETTILE      = 20.0;

    private static final Image SPRITE_SHEET = new Image(Objects.requireNonNull(
            FallenStarGolem.class.getResourceAsStream("/uni/gaben/iscat/sprites/fallen_star_golem.png")));

    private final Cooldown cooldownFuoco = new Cooldown();

    public FallenStarGolem(double startX, double startY) {
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
        double dist = Math.hypot(playerPos.x - myPos.x, playerPos.y - myPos.y);

        // 1. Movimento
        maintainDistance(playerPos, myPos, dist);

        // 2. Sparo
        if (dist >= 90 && dist <= 420 && cooldownFuoco.isReady()) {
            shoot(world, myPos);
        }
    }

    private void maintainDistance(Vec2 playerPos, Vec2 myPos, double dist) {
        double dx = playerPos.x - myPos.x;
        double dy = playerPos.y - myPos.y;
        double angle = Math.atan2(dy, dx);

        if (dist < DISTANZA_IDEALE - 20) {
            this.applyForce(new Vec2(-Math.cos(angle) * VELOCITA_MOVIMENTO, -Math.sin(angle) * VELOCITA_MOVIMENTO));
        } else if (dist > DISTANZA_IDEALE + 30) {
            this.applyForce(new Vec2(Math.cos(angle) * VELOCITA_MOVIMENTO, Math.sin(angle) * VELOCITA_MOVIMENTO));
        }

        this.setDirectionAngle(Math.toDegrees(angle));
    }

    private void shoot(GameModel world, Vec2 myPos) {
        int numProiettili = 12;
        // Calcoliamo l'angolo tra un proiettile e l'altro (360 / 12 = 30 gradi)
        double step = (2 * Math.PI) / numProiettili;

        for (int i = 0; i < numProiettili; i++) {
            // L'angolo attuale per questo proiettile
            double currentRad = i * step;

            // Calcoliamo la velocità basata sull'angolo
            Vec2 velocity = new Vec2(
                    Math.cos(currentRad) * VEL_PROIETTILE,
                    Math.sin(currentRad) * VEL_PROIETTILE
            );

            // Spawna il proiettile partendo dal centro del Golem
            world.addEntity(new GolemProjectile(myPos, velocity));
        }

        // Effetto sonoro (eseguito una volta sola per la raffica)
        IscatAudioManager.getInstance().playSFX("shoot");

        cooldownFuoco.start(COOLDOWN_SPARO_TICKS);
    }

    @Override
    public Drawable<FallenStarGolem> getRenderer() {
        return (gc, fallenStarGolem) -> {
            int frame = (int) ((System.nanoTime() / 1_000_000_000.0) / 0.4) % NUMERO_FRAMES;
            int sourceX = frame * (int) DIM_SPRITE;

            WritableImage frameImg = new WritableImage(
                    SPRITE_SHEET.getPixelReader(), sourceX, 0, (int) DIM_SPRITE, (int) DIM_SPRITE);

            Color tint = ThemeManager.getInstance().globalTintProperty().get();
            Image drawn = SpriteUtils.tinted(frameImg, tint);

            gc.save();
            gc.translate(
                    fallenStarGolem.getX() + DRAW_SIZE / 2,
                    fallenStarGolem.getY() + DRAW_SIZE / 2
            );

            //gc.rotate(fallenStarGolem.getDirectionAngle() + 270);

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
    // CLASSE INTERNA PROIETTILE
    // =========================================================================

    public static class GolemProjectile extends ProjectileModel implements HasRenderer {

        public GolemProjectile(Vec2 pos, Vec2 vel) {
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