package uni.gaben.iscat.gamenex.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.view.camera.CameraModel;
import uni.gaben.iscat.gamenex.controller.GamenexInputs;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerController;
import uni.gaben.iscat.gamenex.universe.starfield.StarfieldController;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiController;
import uni.gaben.iscat.gamenex.lib.utils.UU;

import java.util.ArrayList;
import java.util.List;

public class UniverseController {

    private final UniverseModel universeModel;
    private final PlayerController playerController;
    private final List<AiController> aiControllers = new ArrayList<>();
    private final StarfieldController starfieldController = new StarfieldController();

    public UniverseController() {
        this(new UniverseModel());
    }

    public UniverseController(UniverseModel universeModel) {
        this.universeModel = universeModel;
        this.playerController = new PlayerController(universeModel.getPlayer());
    }

    public void addAiController(AiController controller) {
        this.aiControllers.add(controller);
    }

    /**
     * Flusso di aggiornamento principale (Engine Tick) coordinato dal Controller.
     */
    public void updatev(double dt, GamenexInputs inputs, CameraModel cameraModel) {
        PlayerModel player = universeModel.getPlayer();
        if (playerController.getPlayer() != player) {
            playerController.setPlayer(player);
        }

        // 1. Processamento degli Input dell'utente sul Player
        if (player != null) {
            playerController.processInput(
                    inputs,
                    cameraModel.getViewportLeftX(),
                    cameraModel.getViewportTopY(),
                    dt
            );
            applyOrbitalGravity(player);
        }

        // 2. Aggiornamento delle Intelligenze Artificiali degli NPC
        for (AiController ai : aiControllers) {
            ai.aiUpdate(universeModel, dt);
        }

        // 3. Avanzamento sincronizzato del modello fisico (Risoluzione accoppiata)
        // Utilizziamo update(dt) per garantire che l'override del nostro modello venga eseguito
        universeModel.updatev(dt);

        // 4. Sincronizzazione MVC Post-Physics: Rimozione dei corpi morti prima del render visivo
        universeModel.processEntityCleanup();

        // 5. Allineamento molle e aggiornamento della Telecamera (Conversione Metri -> Pixel)
        if (player != null) {
            double targetX = UU.mToPx(player.getTransform().getTranslationX());
            double targetY = UU.mToPx(player.getTransform().getTranslationY());

            cameraModel.getSpringX().setTarget(targetX);
            cameraModel.getSpringY().setTarget(targetY);
        }

        cameraModel.getSpringX().update(dt);
        cameraModel.getSpringY().update(dt);
    }

    /**
     * Applica una forza orbitale per far ruotare i detriti e i corpi minori attorno al giocatore.
     */
    private void applyOrbitalGravity(PlayerModel player) {
        Vector2 pPos = player.getTransform().getTranslation();
        Vector2 pVel = player.getLinearVelocity();

        int bodyCount = universeModel.getBodyCount();

        for (int i = 0; i < bodyCount; i++) {
            Body body = universeModel.getBody(i);

            if (body == player || body.getMass().isInfinite()) {
                continue;
            }

            Vector2 bPos = body.getTransform().getTranslation();
            Vector2 radial = pPos.copy().subtract(bPos);
            double dist = radial.getMagnitude();

            if (dist < 0.01 || dist > UniverseSettings.SUCTION_RANGE_M) {
                continue;
            }

            double mass = body.getMass().getMass();
            radial.normalize();

            double grav = UniverseSettings.ORBITAL_G / (dist * dist);
            Vector2 radialForce = radial.copy().multiply(grav * mass);

            double vOrbit = Math.sqrt(UniverseSettings.ORBITAL_G / dist);
            Vector2 tangent = new Vector2(-radial.y, radial.x);

            Vector2 relVel = body.getLinearVelocity().copy().subtract(pVel);
            double currentTangential = relVel.dot(tangent);

            double error = vOrbit - currentTangential;
            Vector2 tangentialForce = tangent.multiply(error * UniverseSettings.CIRCULARIZE_GAIN * mass);

            body.applyForce(radialForce.add(tangentialForce));
        }
    }

    public UniverseModel getUniverseModel() { return universeModel; }
    public StarfieldController getStarfieldController() { return starfieldController; }
}