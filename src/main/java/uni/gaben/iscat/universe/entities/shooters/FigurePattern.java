package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.Shooter;

import java.util.function.Consumer;

public class FigurePattern implements Pattern {

    private final int count;
    public enum FigureType {
        NONE,
        CIRCLE,
        SQUARE,
        TRIANGLE,
        STAR
    }
    private FigureType type;

    /**
     * @param count Numero totale di proiettili che compongono la figura.
     * @param type  La forma geometrica dell'onda (CIRCLE, SQUARE, TRIANGLE, STAR).
     */
    public FigurePattern(int count, FigureType type) {
        this.count = count;
        this.type = type;
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType bulletType, double angle, Consumer<ProjectileModel> customizer) {
        if (count <= 0) return;

        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            // Angolo di direzione del proiettile attuale
            double currentAngle = angle + (i * angleStep);

            double geometricFactor = calculateGeometricFactor(currentAngle);

            Consumer<ProjectileModel> figureCustomizer = bullet -> {
                if (customizer != null) {
                    customizer.accept(bullet);
                }
                bullet.setTerminalVelocity(bullet.getTerminalVelocity() * geometricFactor);
            };

            shooter.shoot(bulletType, currentAngle, figureCustomizer);
        }
    }

    /**
     * Funzione matematica polare che determina la distanza dal centro
     * per mantenere i bordi della figura dritti o modellati.
     */
    private double calculateGeometricFactor(double angle) {
        switch (type) {
            case NONE:
                System.err.println("[FigurePattern] Devi selezionare una figura geometrica!");
                return 1.0;
            case SQUARE:
                return 1.0 / Math.max(Math.abs(Math.cos(angle)), Math.abs(Math.sin(angle)));
            case TRIANGLE:
                double normalizedAngle = ((angle % (2.0 * Math.PI)) + (2.0 * Math.PI)) % (2.0 * Math.PI);
                double triStep = 2.0 * Math.PI / 3.0;
                double localAngle = (normalizedAngle % triStep) - (triStep / 2.0);
                return 0.5 / Math.cos(localAngle);
            case STAR:
                double starFrequency = 5.0;
                double depth = 0.4;
                return 1.0 - depth * Math.abs(Math.sin((starFrequency * angle) / 2.0));
            case CIRCLE:
            default:
                return 1.0;
        }
    }
}