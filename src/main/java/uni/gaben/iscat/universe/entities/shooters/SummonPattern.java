package uni.gaben.iscat.universe.entities.shooters;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entities.EntityState;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.UU;
import java.util.function.Consumer;

/**
 * Attacco che evoca nemici
 */
public class SummonPattern implements Pattern {

    /** Il numero totale di entità da evocare nell'ondata. */
    private final int count;

    /** L'identificatore univoco (ID del Database) della specie di nemico/entità da generare. */
    private final String enemyId;

    /** Il raggio della circonferenza di spawn espresso in pixel. */
    private final double spawnRadiusPx;

    /**
     * Costruisce un generatore di evocazioni radiali.
     *
     * @param count         Numero di entità da distribuire lungo l'anello di evocazione.
     * @param enemyId       Stringa identificativa del tipo di entità da richiedere al database di spawn.
     * @param spawnRadiusPx Distanza radiale dall'emettitore (in pixel) alla quale far comparire i minion.
     */
    public SummonPattern(int count, String enemyId, double spawnRadiusPx) {
        this.count = count;
        this.enemyId = enemyId;
        this.spawnRadiusPx = spawnRadiusPx;
    }

    /**
     * Esegue la mutazione dello stato dell'emettitore e avvia l'evocazione circolare uniforme.
     * <p>
     * Calcola la ripartizione trigonometrica dell'angolo giro $\Delta\theta = \frac{2\pi}{\text{count}}$,
     * proietta le coordinate dei punti di spawn nel sistema metrico e le riconverte in pixel prima di
     * inoltrare la richiesta all'istanza Singleton dell'{@link UniverseSpawner}.
     * </p>
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType pType, double angle, Consumer<ProjectileModel> customizer) {
        var model = shooter.getModel();

        // Transizione di stato visivo/logico: imposta l'entità in posa di attacco da evocazione
        if (model instanceof EntityModel entityModel) {
            entityModel.setEntityState(EntityState.SPAWN_ATTACK);
        }

        Vector2 originPos = model.getTransform().getTranslation();

        // Conversione del raggio operativo dall'unità pixel all'unità metrica del motore fisico
        double spawnRadiusM = UU.pxToM(spawnRadiusPx);
        double angleStep = (2.0 * Math.PI) / count;

        // Iterazione e posizionamento radiale delle entità evocate
        for (int i = 0; i < count; i++) {
            // Calcolo dell'angolo assoluto del punto di spawn corrente della corona
            double currentAngle = angle + (i * angleStep);

            // Proiezione geometrica vettoriale sul cerchio e immediata riconversione in pixel per lo spawner
            double spawnX = UU.mToPx(originPos.x + Math.cos(currentAngle) * spawnRadiusM);
            double spawnY = UU.mToPx(originPos.y + Math.sin(currentAngle) * spawnRadiusM);

            // Invocazione e allocazione sul ciclo di gioco tramite il factory globale
            UniverseSpawner.getInstance().spawn(enemyId, spawnX, spawnY);
        }
    }
}