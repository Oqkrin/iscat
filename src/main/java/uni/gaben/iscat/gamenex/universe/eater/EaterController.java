package uni.gaben.iscat.gamenex.universe.eater;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;

public class EaterController extends AiBehaviours<EaterModel> {

    private final EaterModel eater;
    private Vector2 target = null;

    public EaterController(EaterModel eater) {
        super(eater);
        this.eater = eater;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (eater == null || eater.isConsumed())
            return;

        PlayerModel player = universeModel.getPlayer();
        if (player == null)
            return;

        Vector2 eaterPos = eater.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();

        // Aggiorna target sul player
        if (target == null || eaterPos.distanceSquared(target) < 10) {
            target = playerPos.copy();
        }

        Vector2 direction = target.copy().subtract(eaterPos);
        double distance = direction.getMagnitude();

        // Attacco
        double attackRadius = EaterSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE * 3;

        if (distance < attackRadius) {
            performAttack(player, universeModel);
            return;
        }

        // Inseguimento
        if (distance > 0.5) {
            // Forza
            if (eater.getLinearVelocity().getMagnitude() <= EaterSettings.MAX_VELOCITY_MS) {
                Vector2 force = direction.getNormalized()
                        .multiply(EaterSettings.FORCE * 1);

                eater.applyForce(force);
            } else {
                // Cap velocità
                Vector2 vel = eater.getLinearVelocity();
                eater.setLinearVelocity(vel.getNormalized()
                        .multiply(EaterSettings.MAX_VELOCITY_MS));
            }
        }
    }

    private void performAttack(PlayerModel player, UniverseModel universe) {
        // System.out.println("[EaterController] COLLISIONE! Attacco al player!");

        player.bleed(EaterSettings.ATTACK_POWER);

        eater.consume();
        eater.setLife(0); // This will trigger automatic removal in UniverseModel.update()
    }
}