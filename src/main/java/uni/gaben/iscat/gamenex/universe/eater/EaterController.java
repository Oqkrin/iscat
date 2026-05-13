package uni.gaben.iscat.gamenex.universe.eater;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.hearth.HearthSettings;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class EaterController extends AiBehaviours<EaterModel> {

    private final EaterModel eater;

    Vector2 target = null;
    Random rand = new Random();
    Vector2 dirvec;
    double maxMagnitude = 40 / UniverseSettings.SCALE;
    double minMagnitude = 20 / UniverseSettings.SCALE;
    public EaterController(EaterModel eater) {
        super(eater);
        this.eater = eater;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        /**
        if (target == null) {
            double currentDir = npc.getTransform().getRotationAngle();
            target = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5*Math.PI + rand.nextDouble(1.5*Math.PI));
        } else {
            if (npc.getLinearVelocity().getMagnitude() <= EaterSettings.MAX_VELOCITY_MS) {
                dirvec = npc.getTransform().getTranslation().to(target);
                npc.getTransform().setRotation(
                        Interpolator.smootherStep(npc.getTransform().getRotationAngle(),dirvec.getDirection(),
                                1-(1/dirvec.getMagnitude())
                        ));
                npc.applyForce(dirvec.getNormalized().multiply(EaterSettings.FORCE));
            } else {
                npc.setLinearVelocity(npc.getLinearVelocity().setMagnitude(EaterSettings.MAX_VELOCITY_MS));
            }
            if(npc.contains(target)) {
                target = null;
            }
        } **/

        if (eater == null || eater.isConsumed()) return;

        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        Vector2 eaterPos = eater.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();

        double distanceSquared = eaterPos.distanceSquared(playerPos);

        double collisionRadius = HearthSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE * 3;

        if (distanceSquared < collisionRadius * collisionRadius) {
            attack(eater, player, universeModel);
        }
    }

    private void attack(EaterModel eater, PlayerModel player, UniverseModel universe) {
        // Attacca il giocatore
        System.out.println("[EaterController] Eater ha attaccato ed è morto! ");
        player.take_damage(EaterSettings.ATTACK_POWER);

        eater.consume();
        eater.onDeath();
        universe.removeEntity(eater);
    }
}