package uni.gaben.iscat.gamenex.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.view.camera.CameraController;
import uni.gaben.iscat.gamenex.view.camera.CameraModel;
import uni.gaben.iscat.gamenex.controller.InputManager;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerController;
import uni.gaben.iscat.gamenex.universe.starfield.StarfieldController;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiController;
import java.util.ArrayList;
import java.util.List;

/**
 * MVC Controller for the space world.
 * Owns: PlayerController, CameraController, suction-gravity logic.
 * The view (GamenexScene) reads camera position from here — never from the model.
 */
/**
 * Controller logico dell'universo di gioco.
 * Gestisce l'aggiornamento della fisica, la logica degli NPC,
 * lo spawning delle entità e il sistema di particelle (stelle).
 */
public class UniverseController {

    private final UniverseModel universeModel;
    private final PlayerController playerController = new PlayerController();
    private final List<AiController> aiControllers = new ArrayList<>();
    private final CameraController cameraController = new CameraController();
    private final StarfieldController starfieldController = new StarfieldController();

    private double viewW = 800;
    private double viewH = 600;

    // Gravity/Suction settings are now in EnvironmentSettings

    public UniverseController() {
        this(new UniverseModel());
    }

    public UniverseController(UniverseModel universeModel) {
        this.universeModel = universeModel;
    }

    public void addAiController(AiController controller) {
        this.aiControllers.add(controller);
    }

    public void setViewSize(double w, double h) {
        this.viewW = w;
        this.viewH = h;
    }

    public void update(double dt, InputManager input, CameraModel cameraModel) {
        PlayerModel player = universeModel.getPlayer();

        if (input != null) {
            playerController.processInput(player, input, cameraModel.getX(), cameraModel.getY(), dt);

            if (input.suction) {
                applyOrbitalGravity(player);
            }
        }

        for (AiController ai : aiControllers) {
            ai.aiUpdate(universeModel, dt);
        }

        universeModel.update(dt);

        // Camera update lives in the controller, not the model (MVC)
        double playerWorldX = player.getTransform().getTranslationX() * UniverseSettings.SCALE;
        double playerWorldY = player.getTransform().getTranslationY() * UniverseSettings.SCALE;
        cameraController.update(cameraModel, playerWorldX, playerWorldY, viewW, viewH, dt);
    }

    /**
     * Two-component orbital gravity:
     *
     * 1. RADIAL — inverse-square Newtonian gravity pulls the body toward the player.
     *    F_r = G / r²   (× mass so all bodies feel the same acceleration)
     *
     * 2. TANGENTIAL (circularization) — compute the ideal circular-orbit speed
     *    v_orb = sqrt(G / r), then apply a correction force toward that speed along
     *    the perpendicular axis.  This gradually converts a straight-line fall into
     *    a stable elliptical/circular orbit without any teleportation.
     *
     * Hold Q → asteroids spiral in and settle into a stable orbit around the player.
     */
    private void applyOrbitalGravity(PlayerModel player) {
        Vector2 pPos = player.getTransform().getTranslation();
        Vector2 pVel = player.getLinearVelocity();
        // Use zero velocity reference during dash to prevent "flinging" entities
        Vector2 effectivePlayerVel = player.isInScatto() ? new Vector2(0, 0) : pVel;

        for (int i = 0; i < universeModel.getBodyCount(); i++) {
            Body body = universeModel.getBody(i);
            if (body == player || body.getMass().isInfinite()) continue;

            Vector2 bPos = body.getTransform().getTranslation();
            Vector2 radial = pPos.copy().subtract(bPos); // body → player
            double dist = radial.getMagnitude();

            if (dist < 0.01 || dist > UniverseSettings.SUCTION_RANGE_M) continue;

            double mass = body.getMass().getMass();
            radial.normalize();

            // 1. Radial: inverse-square gravity toward player
            double grav = UniverseSettings.ORBITAL_G / (dist * dist);
            Vector2 radialForce = radial.copy().multiply(grav * mass);

            // 2. Tangential: circularize the orbit
            double vOrbit = Math.sqrt(UniverseSettings.ORBITAL_G / dist);
            Vector2 tangent = new Vector2(-radial.y, radial.x);

            // Velocity relative to player (ignored during dash to prevent flinging)
            Vector2 relVel = body.getLinearVelocity().copy().subtract(effectivePlayerVel);
            double currentTangential = relVel.dot(tangent);

            double error = vOrbit - currentTangential;
            Vector2 tangentialForce = tangent.multiply(error * UniverseSettings.CIRCULARIZE_GAIN * mass);

            body.applyForce(radialForce.add(tangentialForce));
        }
    }

    public UniverseModel getSpaceModel() { return universeModel; }
    public StarfieldController getStarfieldController() { return starfieldController; }
}
