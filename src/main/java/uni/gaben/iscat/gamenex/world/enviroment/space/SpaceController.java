package uni.gaben.iscat.gamenex.world.enviroment.space;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.camera.CameraController;
import uni.gaben.iscat.gamenex.camera.CameraModel;
import uni.gaben.iscat.gamenex.controller.InputManager;
import uni.gaben.iscat.gamenex.player.PlayerModel;
import uni.gaben.iscat.gamenex.player.controller.PlayerController;
import uni.gaben.iscat.gamenex.world.PhysicsSettings;
import uni.gaben.iscat.gamenex.world.enviroment.space.starfield.StarfieldController;
import uni.gaben.iscat.gamenex.world.enviroment.EnvironmentSettings;

/**
 * MVC Controller for the space world.
 * Owns: PlayerController, CameraController, suction-gravity logic.
 * The view (GamenexScene) reads camera position from here — never from the model.
 */
public class SpaceController {

    private final SpaceModel spaceModel;
    private final PlayerController playerController = new PlayerController();
    private final CameraController cameraController = new CameraController();
    private final StarfieldController starfieldController = new StarfieldController();

    private double viewW = 800;
    private double viewH = 600;

    // Gravity/Suction settings are now in EnvironmentSettings

    public SpaceController() {
        this(new SpaceModel());
    }

    public SpaceController(SpaceModel spaceModel) {
        this.spaceModel = spaceModel;
    }

    public void setViewSize(double w, double h) {
        this.viewW = w;
        this.viewH = h;
    }

    public void update(double dt, InputManager input, CameraModel cameraModel) {
        PlayerModel player = spaceModel.getPlayer();

        if (input != null) {
            playerController.processInput(player, input, cameraModel.getX(), cameraModel.getY());

            if (input.suction) {
                applyOrbitalGravity(player);
            }
        }

        spaceModel.update(dt);

        // Camera update lives in the controller, not the model (MVC)
        double playerWorldX = player.getTransform().getTranslationX() * PhysicsSettings.SCALE;
        double playerWorldY = player.getTransform().getTranslationY() * PhysicsSettings.SCALE;
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

        for (int i = 0; i < spaceModel.getBodyCount(); i++) {
            Body body = spaceModel.getBody(i);
            if (body == player || body.getMass().isInfinite()) continue;

            Vector2 bPos = body.getTransform().getTranslation();
            Vector2 radial = pPos.copy().subtract(bPos); // body → player
            double dist = radial.getMagnitude();

            if (dist < 0.01 || dist > EnvironmentSettings.SUCTION_RANGE_M) continue;

            double mass = body.getMass().getMass();
            radial.normalize();

            // 1. Radial: inverse-square gravity toward player
            double grav = EnvironmentSettings.ORBITAL_G / (dist * dist);
            Vector2 radialForce = radial.copy().multiply(grav * mass);

            // 2. Tangential: circularize the orbit
            //    Ideal speed at this radius: v = sqrt(G / r)
            double vOrbit = Math.sqrt(EnvironmentSettings.ORBITAL_G / dist);

            // Perpendicular (CCW) gives a consistent orbit direction
            Vector2 tangent = new Vector2(-radial.y, radial.x);

            // Velocity relative to player so orbit survives player movement
            Vector2 relVel = body.getLinearVelocity().copy().subtract(pVel);
            double currentTangential = relVel.dot(tangent);

            double error = vOrbit - currentTangential;
            Vector2 tangentialForce = tangent.multiply(error * EnvironmentSettings.CIRCULARIZE_GAIN * mass);

            body.applyForce(radialForce.add(tangentialForce));
        }
    }

    public SpaceModel getSpaceModel() { return spaceModel; }
    public StarfieldController getStarfieldController() { return starfieldController; }
}
