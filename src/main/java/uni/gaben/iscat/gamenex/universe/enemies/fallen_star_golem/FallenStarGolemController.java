package uni.gaben.iscat.gamenex.universe.enemies.fallen_star_golem;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.gamenex.universe.projectiles.ProjectileType;
import uni.gaben.iscat.gamenex.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class FallenStarGolemController extends AiBehaviours<FallenStarGolemModel> {

    private enum State { WANDER, CHASE, COMBAT }
    private State state = State.WANDER;

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    private final Shooter<FallenStarGolemModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    public FallenStarGolemController(FallenStarGolemModel golem) {
        super(golem);
        shooter = new Shooter<>(golem);

        bulletTemplate = new Projectile();
        bulletTemplate.setType(ProjectileType.ENEMY_BULLET);
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        fireCooldown.update(dt);
        PlayerModel player = universeModel.getPlayer();

        double distToPlayer = player == null
                ? Double.MAX_VALUE
                : aiEntity.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());

        if (player == null || distToPlayer > FallenStarGolemSettings.DETECTION_RANGE) {
            state = State.WANDER;
        } else if (distToPlayer <= FallenStarGolemSettings.COMBAT_RANGE) {
            state = State.COMBAT;
        } else {
            state = State.CHASE;
        }

        switch (state) {
            case WANDER -> updateWander(dt);
            case CHASE -> updateChase(player, dt);
            case COMBAT -> updateCombat(player, dt);
        }
    }

    private void updateWander(double dt) {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI)
            );
        }
        aiEntity.applyForce(wanderTarget.getNormalized().multiply(FallenStarGolemSettings.FORCE));
        if (aiEntity.contains(wanderTarget)) wanderTarget = null;
    }

    private void updateChase(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);
        aiEntity.applyForce(toPlayer.getNormalized().multiply(FallenStarGolemSettings.FORCE));
    }

    // ── COMBAT MODIFICATO: ATTACCO RADIALE A 12 DIREZIONI ─────────────────────
    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;

        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        if (dist < FallenStarGolemSettings.PREFERRED_RANGE) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-FallenStarGolemSettings.FORCE * 0.6));
        } else if (dist > FallenStarGolemSettings.PREFERRED_RANGE * 1.2) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(FallenStarGolemSettings.FORCE * 0.4));
        }

        // Controllo del Cooldown di fuoco
        if (!fireCooldown.isCoolingDown()) {

            // 1. Salviamo l'angolo di rotazione originale del Golem per non perderlo
            double originalAngle = aiEntity.getTransform().getRotationAngle();

            // 2. Definiamo quante direzioni vogliamo (12)
            int totalDirections = 12;

            // 3. Calcoliamo lo spazio in radianti tra un proiettile e l'altro.
            // Un cerchio completo è 2 * PI. Diviso 12 significa uno sparo ogni 30 gradi (PI / 6).
            double angleIncrement = (2 * Math.PI) / totalDirections;

            // 4. Ciclo per sparare nelle 12 direzioni
            for (int i = 0; i < totalDirections; i++) {
                // Calcola l'angolo per questo specifico proiettile
                double currentSliceAngle = i * angleIncrement;

                // Ruota momentaneamente il modello fisico verso questo angolo
                aiEntity.getTransform().setRotation(currentSliceAngle);

                // Lo shooter legge la rotazione appena impostata e lancia il proiettile in questa direzione
                shooter.shoot(bulletTemplate);
            }

            // 5. Ripristiniamo l'angolo originale del Golem così graficamente non impazzisce
            aiEntity.getTransform().setRotation(originalAngle);

            // Fatto! Avvia il cooldown dello sparo globale
            fireCooldown.start(FallenStarGolemSettings.FIRE_COOLDOWN_S);
        }
    }

    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0);
        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current,
                current + diff,
                Math.min(FallenStarGolemSettings.ROTATION_SPEED * dt, 1.0)
        );
        aiEntity.getTransform().setRotation(next);
    }
}