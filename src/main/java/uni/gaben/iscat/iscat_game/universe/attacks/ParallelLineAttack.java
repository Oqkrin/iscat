package uni.gaben.iscat.iscat_game.universe.attacks;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;
import uni.gaben.iscat.iscat_game.utils.UU;

import java.util.function.Consumer;

/** spara n proiettili paralleli  */
public class ParallelLineAttack implements AttackPattern {

    private final int count;
    private final double spacingMeters;

    /**
     * @param count     Numero di proiettili paralleli da sparare.
     * @param spacingPx Distanza in pixel tra un proiettile e l'altro.
     */
    public ParallelLineAttack(int count, double spacingPx) {
        this.count = count;
        this.spacingMeters = UU.pxToM(spacingPx);
    }

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        if (count <= 0) return;

        Vector2 origin = shooter.getModel().getTransform().getTranslation();
        double perpAngle = angle + (Math.PI / 2.0); // Angolo perpendicolare per lo shifting laterale

        // Determina la distanza frontale di sicurezza basandosi sulla dimensione del modello
        double forwardDistance = 0.1;
        if (shooter.getModel() instanceof AbstractEntityModel aem) {
            forwardDistance = aem.getHeightMeters() / 2.0;
        }

        // Calcola la larghezza totale del fronte per centrare la linea rispetto all'entità
        double totalWidth = (count - 1) * spacingMeters;
        double startOffset = -totalWidth / 2.0;

        for (int i = 0; i < count; i++) {
            double currentOffset = startOffset + (i * spacingMeters);

            // Combina lo spostamento in avanti (forwardDistance) con lo spostamento laterale (currentOffset)
            Vector2 spawnPos = new Vector2(
                    origin.x + Math.cos(angle) * forwardDistance + Math.cos(perpAngle) * currentOffset,
                    origin.y + Math.sin(angle) * forwardDistance + Math.sin(perpAngle) * currentOffset
            );

            shooter.shoot(template, spawnPos, angle, customizer);
        }
    }
}