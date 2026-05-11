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
public class IscatMother extends NpcModel implements AI, HasRenderer, Spawnable, Collidable {

    // --- SETTINGS INTERNI ---
    private static final int    HP_INIZIALI         = 500;
    private static final double DIM_SPRITE          = 128.0;
    private static final int NUMERO_FRAMES          = 2;
    private static final double SCALE = 4.0;                    // ← Scala principale
    private static final double DRAW_SIZE = DIM_SPRITE * SCALE;
    private static final double VELOCITA_MOVIMENTO = 13;

    private static final double RAGGIO_COLLISIONE   = (DIM_SPRITE / 2.0) * 0.9;
    private static final double DISTANZA_IDEALE     = 250.0;
    private static final int    COOLDOWN_SPARO_TICKS = 60;
    private static final double DIM_PROIETTILE      = 32.0;
    private static final double VEL_PROIETTILE      = 20.0; // px/tick

    private static final Image SPRITE_SHEET = new Image(Objects.requireNonNull(
            IscatMother.class.getResourceAsStream("/uni/gaben/iscat/sprites/iscat_mother.png")));

    private final Cooldown cooldownFuoco = new Cooldown();
    private boolean hasSpawnedMinions = false;

    private GameModel currentWorld = null;

    public void setWorld(GameModel world) {
        this.currentWorld = world;
    }

    public IscatMother(double startX, double startY) {
        super(startX, startY);
        this.Spritesize = DIM_SPRITE * SCALE;
        this.hp = HP_INIZIALI;
        this.maxHp = HP_INIZIALI;
        this.spriteSize = DIM_SPRITE * SCALE;
        this.cooldownFuoco.set(COOLDOWN_SPARO_TICKS);
    }

    @Override
    public void updateAI(GameModel world, double dt) {
        cooldownFuoco.tick();

        // Aggiorniamo il riferimento world (backup)
        if (currentWorld == null) currentWorld = world;

        checkMinionSpawn(world);

        if (isDead()) return;

        Vec2 playerPos = world.getPlayer().getColliderCenter();
        Vec2 myPos = this.getColliderCenter();
        double dist = Math.hypot(playerPos.x - myPos.x, playerPos.y - myPos.y);

        maintainDistance(playerPos, myPos, dist);

        if (dist >= 90 && dist <= 420 && cooldownFuoco.isReady()) {
            shoot(world, myPos);
        }
    }

    // ====================== MORTE + ORDA ======================
    @Override
    public void die() {

        System.out.println("=====================================");
        System.out.println("ISCAT MOTHER HA DATO INIZIO AD UN'ORDA!");
        System.out.println("=====================================");

        if (currentWorld != null) {
            spawnHorde(currentWorld);
        } else {
            System.err.println("ERRORE: currentWorld è null in die()");
        }

        super.die();
    }

    /** Controlla se IscatMother vuole spawnare i rinforzi */
    private void checkMinionSpawn(GameModel world) {
        if (!hasSpawnedMinions && this.hp <= HP_INIZIALI * 0.5) {
            spawnMinions(world);
            hasSpawnedMinions = true;
        }
    }

    /** Spawna 5 FakeIscat intorno alla madre */
    private void spawnMinions(GameModel world) {
        Vec2 myPos = this.getColliderCenter();
        double radius = 80.0;   // distanza dal centro della madre

        for (int i = 0; i < 5; i++) {
            double angle = (i * 72.0); // 360° / 5 = 72°
            double rad = Math.toRadians(angle);

            double x = myPos.x + Math.cos(rad) * radius;
            double y = myPos.y + Math.sin(rad) * radius;

            Iscat isc = new Iscat(x, y);
            world.spawnEnemyLater(isc);
        }

        FakeIscat fake1 = new FakeIscat(x, y);
        FakeIscat fake2 = new FakeIscat(x, y);
        world.spawnEnemyLater(fake1);
        world.spawnEnemyLater(fake2);

        System.out.println("IscatMother ha chiamato rinforzi!");
        // TODO: suono di spawn / effetto particelle
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
        double baseAngle = this.getDirectionAngle();
        double spreadAngle = 15.0;

        for (int i = -1; i <= 1; i++) {
            double angle = baseAngle + (i * spreadAngle);

            double rad = Math.toRadians(angle);
            Vec2 velocity = new Vec2(
                    Math.cos(rad) * VEL_PROIETTILE,
                    Math.sin(rad) * VEL_PROIETTILE
            );

            world.addEntity(new MotherProjectile(myPos, velocity));
        }

        IscatAudioManager.getInstance().playSFX("shoot");
        cooldownFuoco.set(COOLDOWN_SPARO_TICKS);
    }

    @Override
    public Drawable<IscatMother> getRenderer() {
        return (gc, mother) -> {
            int frame = (int) ((System.nanoTime() / 1_000_000_000.0) / 0.4) % NUMERO_FRAMES;
            int sourceX = frame * (int) DIM_SPRITE;

            // Ritaglia il frame corrente dallo spritesheet
            WritableImage frameImg = new WritableImage(
                    SPRITE_SHEET.getPixelReader(), sourceX, 0, (int) DIM_SPRITE, (int) DIM_SPRITE);

            Color tint = ThemeManager.getInstance().globalTintProperty().get();
            Image drawn = SpriteUtils.tinted(frameImg, tint);

            gc.save();
            gc.translate(mother.getX() + DRAW_SIZE / 2, mother.getY() + DRAW_SIZE / 2);
            gc.rotate(mother.getDirectionAngle() + 270);
            gc.drawImage(drawn, -DRAW_SIZE / 2, -DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
            gc.restore();
        };
    }

    @Override public double getCollisionRadius() { return RAGGIO_COLLISIONE * SCALE; }
    @Override public int getCollisionLayer() { return LAYER_ENEMY; }
    @Override public int getCollisionMask() { return LAYER_PLAYER | LAYER_PROJECTILE | LAYER_ENEMY; }

    private void spawnHorde(GameModel world) {
        Vec2 center = this.getColliderCenter();
        double radius = 130.0;

        // 20 Iscat
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 360;
            double rad = Math.toRadians(angle);
            double dist = radius + Math.random() * 60;

            double x = center.x + Math.cos(rad) * dist;
            double y = center.y + Math.sin(rad) * dist;

            Iscat isc = new Iscat(x, y);
            world.spawnEnemyLater(isc);
        }
    }

    // =========================================================================
    // CLASSE INTERNA PER IL PROIETTILE
    // =========================================================================
    public static class MotherProjectile extends ProjectileModel implements HasRenderer {

        public MotherProjectile(Vec2 pos, Vec2 vel) {
            super(pos, vel);
            this.mass = 1.0;
        }

        @Override
        public double getCollisionRadius() { return DIM_PROIETTILE / 2.0; }

        @Override
        public int getCollisionMask() { return LAYER_PLAYER; }

        @Override
        public Drawable<ProjectileModel> getRenderer() {
            return (gc, p) -> {
                double r = DIM_PROIETTILE / 2.0;
                Color tint = ThemeManager.getInstance().globalTintProperty().get();

                gc.save();
                gc.setFill(tint);                          // era ORANGERED
                gc.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2);

                gc.setGlobalAlpha(0.3);
                gc.setFill(tint.darker());                 // era RED
                gc.fillOval(p.getX() - r * 1.5, p.getY() - r * 1.5, r * 3, r * 3);
                gc.restore();
            };
        }
    }
}