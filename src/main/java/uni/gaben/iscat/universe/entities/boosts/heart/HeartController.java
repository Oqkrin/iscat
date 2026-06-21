package uni.gaben.iscat.universe.entities.boosts.heart;

import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;

/**
 * Controller logico e comportamentale dell'entità Cuore / Consumabile (Heart Controller).
 * <p>
 * Gestisce il ciclo di vita del consumabile, integrando un callback di collisione per curare il giocatore
 * e una routine di aggiornamento che implementa un comportamento di "magnetismo" (Pursuit Steering).
 * Se il giocatore entra nel raggio d'azione, l'entità inizia a inseguirlo attivamente.
 * </p>
 */
public class HeartController extends Brain<HeartModel> {

    private final HeartModel heart;

    /** Flag di sicurezza per prevenire collezioni multiple (Double-Pickup) nello stesso frame. */
    private boolean collected = false;

    /**
     * Costruisce il controller del cuore e registra il listener di collisione per il consumo.
     * All'atto del contatto con il giocatore, ripristina istantaneamente il 50% ($\frac{1}{2}$)
     * della sua endurance massima e marca l'entità per la rimozione dal mondo.
     *
     * @param heart Il modello fisico {@link HeartModel} associato a questo controller.
     */
    public HeartController(HeartModel heart) {
        super(heart);
        this.heart = heart;

        // Pipeline di collisione reattiva dedicata ai consumabili
        this.heart.addOnCollision("consumable", otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !collected) {
                collected = true; // Satura immediatamente il flag di raccolta

                // Calcola il 50% della vita massima del giocatore e la applica
                double halfMaxLife = player.getMaxEndurance() / 2.0;
                player.restore(halfMaxLife);

                // Segnala al motore di rimuovere in sicurezza il consumabile dal ciclo
                heart.setShouldRemove(true);
            }
        });
    }

    /**
     * Aggiorna lo stato decisionale e i vettori di movimento (Steering Goals) del consumabile.
     * <p>
     * Calcola la distanza euclidea tra il cuore e l'entità giocatore:
     * </p>
     * <ul>
     * <li>Se la distanza è minore di 3 metri, attiva la forza di sterzata di inseguimento ({@code pursuit}).</li>
     * <li>Altrimenti, l'entità rimane ferma o fluttua sul posto nello stato di inattività ({@code idle}).</li>
     * </ul>
     */
    @Override
    public void update(UniverseModel universe, double dt) {
        AbstractPhysicalEntityModel player = universe.getPlayer();

        // Guardie di corto circuito: interrompe l'aggiornamento se l'entità è invalida o già raccolta
        if (heart == null || heart.shouldRemove() || collected || player == null) return;

        super.update(universe, dt);

        // Verifica del raggio di attivazione magnetica (Threshold = 3 metri)
        if (player.getTransform().getTranslation().distance(heart.getTransform().getTranslation()) < 3.0) {
            setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 0));
        } else {
            setSteeringGoal(SteeringGoal.idle());
        }
    }
}