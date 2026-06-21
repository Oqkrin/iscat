package uni.gaben.iscat.universe.entities.shooters;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;

import java.util.function.Consumer;

/**
 * Spara una raffica di proiettili a spirale.
 * Mantiene lo stato dell'angolo tra un attacco e l'altro per riprendere da dove era rimasto.
 */
public class SpiralPattern implements Pattern {

    private final int count;
    private final double angleStepRad;

    // Variabile di stato: tiene memoria dell'ultimo angolo utilizzato
    private double currentAngle = 0.0;

    /**
     * Crea il pattern a spirale continua.
     *
     * @param count        Numero di proiettili sparati a ogni attivazione.
     * @param angleStepDeg Di quanti gradi ruota la spirale tra un proiettile e il successivo.
     */
    public SpiralPattern(int count, double angleStepDeg) {
        this.count = count;
        this.angleStepRad = Math.toRadians(angleStepDeg);
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        if (count <= 0) return;

        Vector2 origin = shooter.getModel().getTransform().getTranslation();

        // Distanza frontale di sicurezza per lo spawn
        double forwardDistance = 0.1;
        if (shooter.getModel() instanceof AbstractPhysicalEntityModel aem) {
            forwardDistance = aem.getHeightMeters() / 2.0;
        }

        for (int i = 0; i < count; i++) {
            // Calcola la posizione di spawn basata sull'angolo corrente della spirale
            Vector2 spawnPos = new Vector2(
                    origin.x + Math.cos(currentAngle) * forwardDistance,
                    origin.y + Math.sin(currentAngle) * forwardDistance
            );

            // Spara il proiettile nella direzione corrente della spirale
            shooter.shoot(type, spawnPos, currentAngle, customizer);

            // Incrementa l'angolo per il prossimo proiettile della raffica (o del prossimo attacco)
            currentAngle += angleStepRad;

            // Normalizza l'angolo tra 0 e 2PI per evitare che cresca all'infinito
            if (currentAngle > Math.PI * 2) {
                currentAngle -= Math.PI * 2;
            }
        }
    }
}