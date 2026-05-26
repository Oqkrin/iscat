package uni.gaben.iscat.iscat_game.lib.implementations.attacks;

import uni.gaben.iscat.iscat_game.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

import java.util.function.Consumer;

public class FigureAttack implements AttackPattern {

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
    public FigureAttack(int count, FigureType type) {
        this.count = count;
        this.type = type;
    }

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        if (count <= 0) return;

        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            // Angolo di direzione del proiettile attuale
            double currentAngle = angle + (i * angleStep);

            double geometricFactor = calculateGeometricFactor(currentAngle);

            Consumer<Projectile> figureCustomizer = bullet -> {
                if (customizer != null) {
                    customizer.accept(bullet);
                }
                bullet.setTerminalVelocity(bullet.getTerminalVelocity() * geometricFactor);
            };

            shooter.shoot(template, currentAngle, figureCustomizer);
        }
    }

    /**
     * Funzione matematica polare che determina la distanza dal centro
     * per mantenere i bordi della figura dritti o modellati.
     */
    private double calculateGeometricFactor(double angle) {
        switch (type) {
            case NONE:
                System.err.println("[FigureAttack] Devi selezionare una figura geometrica!");
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